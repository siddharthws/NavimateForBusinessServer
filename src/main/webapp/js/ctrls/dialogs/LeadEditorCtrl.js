/**
 * Created by Siddharth on 15-09-2017.
 */

// Controller for Alert Dialog
app.controller('LeadEditorCtrl', function ($scope, $rootScope, $mdDialog, $http, $localStorage, ToastService, GoogleApiService, ExcelService, LeadDataService, TemplateDataService, leads) {

    /* ------------------------------- Scope APIs -----------------------------------*/
    $scope.add = function () {
        // Add a lead to current center of map with first template's default data
        var lead = {
            latitude: 0,
            longitude: 0
        }

        // Add lead to leads array
        $scope.leads.push(lead)

        // Add template data to the lead
        $scope.updateTemplate($scope.leads.length - 1, 0)

        // Simulate a click on the new item
        $scope.listItemClick($scope.leads.indexOf(lead))

        // Scroll to bottom
        scrollList($scope.leads.length)
    }

    $scope.updateTemplate = function (leadIdx, templateIdx) {
        var lead = $scope.leads[leadIdx]
        var template = $scope.templates[templateIdx]

        // Ignore if same template is selected
        if (lead.templateId == template.id) return

        // Create template data object from given template's default data
        var templateData = {}
        templateData.values = []
        template.defaultData.values.forEach(function (value) {
            var field = $rootScope.getFieldById(value.fieldId)

            if (field.type == Constants.Template.FIELD_TYPE_CHECKLIST ||
                field.type == Constants.Template.FIELD_TYPE_RADIOLIST) {
                templateData.values.push({
                    fieldId: value.fieldId,
                    value: JSON.parse(JSON.stringify(value.value))
                })
            } else {
                templateData.values.push({
                    fieldId: value.fieldId,
                    value: value.value
                })
            }
        })

        // Update lead's template data
        lead.templateData = templateData
        lead.templateId = template.id
    }

    $scope.getTemplateById = TemplateDataService.getTemplateById
    $scope.getFieldById = TemplateDataService.getFieldById

    $scope.listItemClick = function (idx) {
        // Select this lead
        $scope.selectedLead = $scope.leads[idx]

        // Refresh Address Search
        $scope.searchResults = []

        // Scroll list ot place this item on top
        scrollList(idx)

        // Center map on selected lead marker
        $scope.$broadcast(Constants.Events.MAP_CENTER, {latitude: $scope.selectedLead.latitude, longitude: $scope.selectedLead.longitude})
    }

    $scope.remove = function(lead) {
        var idx = $scope.leads.indexOf(lead)

        if (idx >= 0) {
            $scope.leads.splice(idx, 1)
        }
    }

    $scope.searchAddress = function () {
        // Perform place search only for valid text
        if ($scope.selectedLead.address) {
            // Set waiting flag
            $scope.bSearchWaiting = true

            // Invalidate current results
            $scope.searchResults = []

            // Get results from Google API
            GoogleApiService.autoComplete($scope.selectedLead.address,
                function (results) {
                    // Update results
                    $scope.searchResults = results

                    // Stop Waiting flag
                    $scope.bSearchWaiting = false
                },
                function () {
                    // Stop Waiting flag
                    $scope.bSearchWaiting = false
                })
        }
    }

    $scope.searchResultClick = function (searchResult) {
        // Update selected lead info
        $scope.selectedLead.address = searchResult.address
        $scope.selectedLead.latitude = 0
        $scope.selectedLead.longitude = 0

        // Get latlng for this place
        GoogleApiService.addressToLatlng(searchResult.address,
                function (lat, lng) {
                    $scope.selectedLead.latitude = lat
                    $scope.selectedLead.longitude = lng

                    // Center map
                    $scope.$broadcast(Constants.Events.MAP_CENTER, {latitude: lat, longitude: lng})
                })
    }

    $scope.save = function () {
        // Validate entered leads
        if (validate()) {
            $rootScope.showWaitingDialog("Please wait while leads are added...")
            // Data Validated. Save in database
            $http({
                method:     'POST',
                url:        '/api/users/lead',
                headers:    {
                    'X-Auth-Token':    $localStorage.accessToken
                },
                data:       {
                    leads:           $scope.leads
                }
            })
            .then(
                function (response) {
                    $rootScope.hideWaitingDialog()
                    // Hide dialog and show toast
                    $mdDialog.hide()
                    ToastService.toast("Leads added succesfully...")

                    //Re-sync Lead data since new member has been added
                    LeadDataService.sync()
                },
                function (error) {
                    $rootScope.hideWaitingDialog()
                    ToastService.toast("Unable to update leads !!!")
                    console.log(error)
                })
        }
    }

    $scope.isLeadValid = function (lead) {
        return (lead.title && lead.latitude && lead.longitude && lead.address)
    }

    // Marker drag events
    $scope.markerDragged = function(e, lead) {
        // Update lead's latitude and longitude
        lead.latitude   = e.latLng.lat()
        lead.longitude  = e.latLng.lng()
        lead.address = ""

        // Get address using reverse geocoding
        GoogleApiService.latlngToAddress(
            lead.latitude, lead.longitude,
            function (address) { // Result Callback
                // Assign address to lead
                lead.address = address
            })
    }

    // Excel related APIs
    $scope.excelRead = function (workbook) {
        $rootScope.showWaitingDialog("Please wait while we are reading from excel file...")
        ExcelService.excelRead(workbook).then(
            function (response) {
                $rootScope.hideWaitingDialog()

                //Adding leads from excel
                response.data.forEach(function (lead) {
                    $scope.leads.push(lead);
                })

                // Remove all empty leads from array
                for (var i = ($scope.leads.length - 1); i >= 0; i--) {
                    var lead = $scope.leads[i]
                    if (!lead.title && !lead.description && !lead.phoneNumber && !lead.address) {
                        $scope.leads.splice(i, 1)
                    }
                }

            },
            function (error) {
                $rootScope.hideWaitingDialog()
                ToastService.toast("Unable to parse uploaded file. Please make sure file is as per template.")
            }
        )
    }

    $scope.excelError = function (e) {
        ToastService.toast("Invalid file for uploading leads...")
    }
    
    $scope.uploadTemplate = function () {
        ExcelService.leadUploadTemplate()
    }

    $scope.cancel = function () {
        $mdDialog.hide()
    }

    /* ------------------------------- Script APIs -----------------------------------*/
    function validate() {
        // Reset Error flag
        $scope.bShowError = false

        // Check for empty leads
        if ($scope.leads.length == 0) {
            ToastService.toast("Please add some leads to save...")
            return false
        }

        // Validate leads
        for (var  i = 0; i < $scope.leads.length; i++) {
            var lead = $scope.leads[i]

            if (!$scope.isLeadValid(lead)) {
                // Assert error flag
                $scope.bShowError = true

                // Scroll to this item
                scrollList(i)

                // Show error toast
                ToastService.toast("Please fill all fields in leads...")

                // Return
                return false
            }
        }

        return true
    }

    function scrollList(index) {
        // Get list and list item
        var list = $('.dialog-container .list-group')[0]
        var items = $(list).find('.list-group-item')
        var scrollingOffset = 0

        // Get Scrolling offset depending on index
        if (index >= items.length) {
            // Scroll to bottom
            scrollingOffset = $(document).height()
        } else if (index > 0) {
            // get relative position
            scrollingOffset = $(items[index]).offset().top - $(items[0]).offset().top
        }

        // Animate the list
        $(list).animate({scrollTop: scrollingOffset})
    }

    /* ------------------------------- INIT -----------------------------------*/
    // Init objects
    $scope.leads = []
    $scope.selectedLead = {}
    $scope.bShowError = false

    $scope.searchResults = []
    $scope.bSearchWaiting = false

    // Init Map Parameters
    $scope.mapParams = {}
    $scope.mapParams.markers = []

    // Init Templates
    $scope.templates = TemplateDataService.cache.data.leads

    // Set event listeners
    $scope.$on(Constants.Events.MAP_MARKER_CLICK, function (event, params) {
        // Perform List click action
        $scope.listItemClick(params.idx)
    })

    // Init leads
    if (leads) {
        // Assign the passed leads & mark the first one as selected
        $scope.leads = leads
        $scope.selectedLead = $scope.leads[0]
    }
    else {
     $scope.add()
    }

    // Add markers using parameters
    $scope.mapParams.markers  = $scope.leads
})
