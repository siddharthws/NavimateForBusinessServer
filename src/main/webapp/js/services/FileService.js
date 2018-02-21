/**
 * Created by Siddharth on 26-02-2018.
 */


app.service("FileService", function ($filter) {
    /*------------------------------------ INIT --------------------------------*/
    var vm  = this

    /*------------------------------------ Public APIs --------------------------------*/
    // Method to download file onto local system
    vm.download = function(response, filename) {
        var contentType = response.headers('Content-Type')
        var linkElement = document.createElement('a');

        // Add timestamp to file
        filename += '_' + $filter('date')(new Date(), "_dd_MM_yy_HHmm") + '.xls'

        try {
            // Create blob element to save to file
            var blob = new Blob([response.data], {type: contentType})
            var url = window.URL.createObjectURL(blob)

            // Create fake link element
            linkElement.setAttribute('href', url);
            linkElement.setAttribute("download", filename);
            var clickEvent = new MouseEvent("click", {
                "view": window,
                "bubbles": true,
                "cancelable": false

            });
            linkElement.dispatchEvent(clickEvent)
        } catch (ex) {
            console.log("Exception while downloading file : " + ex)
        }
    }

    /*------------------------------------ Private APIs --------------------------------*/

})