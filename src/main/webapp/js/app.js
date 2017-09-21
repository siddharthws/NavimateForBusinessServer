var app = angular.module("navimate", [
  'ui.router',
  'ngStorage',
  'angular-js-xlsx',
  'ngMaterial',
  'ngMap',
  'ngTable',
  'angularjs-dropdown-multiselect',
  'ngFileSaver',
  'angular.filter'
]);

app.config(['$locationProvider', function ($locationProvider) {
    $locationProvider.hashPrefix('');
}]);

app.config(function ($stateProvider, $urlRouterProvider) {

    // Configure URL mapping for non existent URLs
  $urlRouterProvider.when('', '/')
  $urlRouterProvider.when('/dashboard', '/dashboard/team/manage')
  $urlRouterProvider.when('/dashboard/team', '/dashboard/team/manage')
  $urlRouterProvider.when('/dashboard/tasks', '/dashboard/tasks/manage')
  $urlRouterProvider.when('/dashboard/forms', '/dashboard/forms/manage')

  $stateProvider
      // Homepage Mappings
    .state('home', {
        url: '/',
        templateUrl: '../views/home.html',
        controller: 'HomeCtrl'
    })
      // Dashboard mappings
    .state('dashboard', {
        abstract: true,
        url: '/dashboard',
        templateUrl: '../views/dashboard.html',
        controller: 'DashboardCtrl'
    })
    .state('dashboard.team-manage', {
        url: '/team/manage',
        templateUrl: '../views/dashboard/team/manage.html',
        controller: 'TeamManageCtrl'
    })
    .state('dashboard.team-report', {
        url: '/team/report',
        templateUrl: '../views/dashboard/team/report.html',
        controller: 'TeamReportCtrl'
    })
    .state('dashboard.leads-manage', {
        url: '/leads/manage',
        templateUrl: '../views/dashboard/leads/manage.html',
        controller: 'LeadManageCtrl'
    })
    .state('dashboard.leads-report', {
        url: '/leads/report',
        templateUrl: '../views/dashboard/leads/report.html',
        controller: 'LeadReportCtrl'
    })
    .state('dashboard.tasks-manage', {
        url: '/tasks/manage',
        templateUrl: '../views/dashboard/tasks/manage.html',
        controller: 'TaskManageCtrl'
    })
    .state('dashboard.forms-manage', {
        url: '/forms/manage',
        templateUrl: '../views/dashboard/forms/manage.html',
        controller: 'FormManageCtrl'
    })
});