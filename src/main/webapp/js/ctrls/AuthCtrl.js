app.controller("AuthCtrl", function ($scope, $rootScope, $http, $location) {
  $scope.register = function () {
    var payload = {
      name: $scope.name,
      phoneNumber: $scope.phoneNumber,
      password: $scope.password
    }
    $http.post("/api/authApi/register", payload)
      .then(function (response) {
        console.log(response.data)
        $location.path("/login")
      }, function (error) {
        console.log(error)
      })
  }
})