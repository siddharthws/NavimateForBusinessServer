/**
 * Created by Siddharth on 11-09-2017.
 */

app.service('ToastService', function($mdToast) {

    // API to show toast on screen
    this.toast = function (message)
    {
        $mdToast.show (
            $mdToast.simple()
                .textContent(message)
                .position('bottom center')
                .hideDelay(3000)
        )
    }
})