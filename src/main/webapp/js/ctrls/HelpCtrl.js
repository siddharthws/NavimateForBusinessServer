
app.controller('HelpCtrl', function($scope, $location, $anchorScroll){
    $scope.scrollTo = function (id) {
        $location.hash(id)
        $anchorScroll()
    }
});