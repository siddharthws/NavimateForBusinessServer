app.controller("AuthCtrl", function ($scope, $rootScope, $http, $location, $localStorage) {

  $scope.register = function ()
  {
    $http({
        method:     'POST',
        url:        '/api/auth/register',
        data:       {
            name:           $scope.name,
            phoneNumber:    $scope.phoneNumber,
            password:       $scope.password
        }
    })
    .then(
        function (response) {
            console.log(response.data)
            $location.path("/login")
        },
        function (error) {
            console.log(error)
        }
    )
  }

  $scope.login = function ()
  {
    $http({
        method:     'POST',
        url:        '/api/auth/login',
        data:       {
            phoneNumber:    $scope.phoneNumber,
            password:       $scope.password
        }
    })
    .then(
        function (response) {
            $localStorage.accessToken = response.data.accessToken;
            $location.path("/dashboard")
        },
        function (error) {
            console.log(error)
        }
    )
  }

    $scope.logout = function ()
    {
     $http({
         method:     'GET',
         url:        '/api/auth/logout',
         headers:    {
             'X-Auth-Token':    $localStorage.accessToken
         }
     })
     .then(
         function (response) {
             $localStorage.accessToken = ""
             $location.path("/login")
         },
         function (error) {
             console.log(error)
         }
     )
    }
})