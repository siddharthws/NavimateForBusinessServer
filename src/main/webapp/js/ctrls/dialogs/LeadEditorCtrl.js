/**
 * Created by Siddharth on 15-09-2017.
 */

// Controller for Alert Dialog
app.controller('LeadEditorCtrl', function ($scope, $rootScope, $mdDialog, $http, $localStorage, ToastService, GoogleApiService, ExcelService, leads, leadUpdateCb) {

    /* ------------------------------- Scope APIs -----------------------------------*/
    $scope.add = function () {
        // Add a lead to current center of map
        var lead = {
            latitude: googleMap.getCenter().lat(),
            longitude: googleMap.getCenter().lng()
        }

        $scope.leads.push(lead)

        // Simulate a click on the new item
        $scope.listItemClick(lead)

        // Scroll to bottom
        scrollList($scope.leads.length)
    }

    $scope.listItemClick = function (lead) {
        // Select this lead
        $scope.selectedLead = lead

        // Refresh Address Search
        $scope.searchResults = []

        // Center map on this lead
        if (googleMap){
            googleMap.panTo(new google.maps.LatLng(lead.latitude, lead.longitude))

        }
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

                    // Recenter map on the selected lead
                    googleMap.panTo(new google.maps.LatLng(lat, lng))
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
                    leads:           JSON.stringify($scope.leads)
                }
            })
            .then(
                function (response) {
                    $rootScope.hideWaitingDialog()
                    // Hide dialog and show toast
                    $mdDialog.hide()
                    ToastService.toast("Leads added succesfully...")

                    // Trigger Callback
                    leadUpdateCb()
                },
                function (error) {
                    $rootScope.hideWaitingDialog()
                    ToastService.toast("Unable to update leads !!!")
                    console.log(error)
                })
        }
    }

    $scope.isLeadValid = function (lead) {
        return (lead.title && lead.phoneNumber && lead.latitude && lead.longitude && lead.address)
    }

    // Map Init Callback from ngMap
    $scope.mapInitialized = function (map) {
        // Set map object
        googleMap = map

        if (!$scope.leads.length) {
            // Add atleast one lead if leads are empty
            $scope.add()
        } else {
            // Center map on added leads
            var bounds = new google.maps.LatLngBounds()
            $scope.leads.forEach(function (lead) {
                bounds.extend(new google.maps.LatLng(lead.latitude, lead.longitude))
            })
            googleMap.fitBounds(bounds)
        }

        // Trigger resize event (Hack since map is not loaded correctly second time)
        google.maps.event.trigger(googleMap, 'resize')

        // Run angular digest cycle since this is async callback
        $scope.$apply()
    }

    // API to get marker icon based on conditions
    $scope.getMarkerIcon = function (lead) {
        // Blue marker for selected lead
        if ($scope.selectedLead == lead) {
            return {
                url: "/static/images/marker_selected.png",
                scaledSize: [40, 40]
            }
        }
        // Default green icon
        else {
            return {
                url: "/static/images/marker_default.png",
                scaledSize: [40, 40]
            }
        }
    }

    // Marker Click Events
    $scope.markerClicked = function(e, lead) {
        // Perform a list item click
        $scope.listItemClick(lead)

        // Scroll list ot place this item on top
        scrollList($scope.leads.indexOf(lead))
    }

    // Marker drag events
    $scope.markerDragged = function(e, lead) {
        // Update lead's latitude and longitude
        lead.latitude   = e.latLng.lat()
        lead.longitude  = e.latLng.lng()

        // Get address using reverse geocoding
        GoogleApiService.latlngToAddress(
            lead.latitude, lead.longitude,
            function (address) { // Callback
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
                response.data.forEach(function (lead) {
                    //Adding leads from excel
                    $scope.leads.push(lead);
                })

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

    $scope.mapCenter = [21, 79]
    $scope.mapZoom   = 4
    var googleMap = null
    var markerIconDefault, markerIconSelected, markerIconError = null

    if (leads) {
        // Assign the passed leads & mark the first one as selected
        $scope.leads = leads
        $scope.selectedLead = $scope.leads[0]
    }
})
