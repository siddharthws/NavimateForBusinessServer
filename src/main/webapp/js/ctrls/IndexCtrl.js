/**
 * Created by Siddharth on 04-10-2017.
 */

app.controller('IndexCtrl', function ($scope, $rootScope) {
    // Attach Constants
    $rootScope.Constants = Constants
    $rootScope.Statics = Statics

    // Data object for waiting dialog
    $scope.waiting = {
        bShow: false,
        message: ""
    }

    // APIs to show / hide dialogs
    $rootScope.showWaitingDialog = function (message) {
        $scope.waiting.message = message
        $scope.waiting.bShow = true
    }

    $rootScope.hideWaitingDialog = function () {
        $scope.waiting.bShow = false
    }

    // APi to move an element in the array
    Array.prototype.move = function (old_index, new_index) {
        if (new_index >= this.length) {
            var k = new_index - this.length;
            while ((k--) + 1) {
                this.push(undefined);
            }
        }
        this.splice(new_index, 0, this.splice(old_index, 1)[0]);
    }

    // API to check for dupes in an array
    Array.prototype.hasDupes = function () {
        var copiedArray = this.concat().sort(); // use whatever sort you want
        for (var i = 0; i < copiedArray.length -1; i++)
        {
            if ((copiedArray[i+1] == copiedArray[i]) && (copiedArray[i].length))
            {
                return true;
            }
        }
        return false;
    }
})
