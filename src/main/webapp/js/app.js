var app = angular.module("navimate", [
  'ngRoute'
]);

app.config(['$locationProvider', function ($locationProvider) {
  $locationProvider.hashPrefix('');
}]);

app.config(function ($routeProvider) {
  $routeProvider
    .when('/register', {
      templateUrl: '../views/register.html',
      controller: 'AuthCtrl'
    })
    .when('/login', {
      templateUrl: '../views/login.html',
      controller: 'AuthCtrl'
    })
});