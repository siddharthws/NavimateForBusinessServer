var app = angular.module("navimate", [
  'ui.router',
  'ngStorage',
  'angular-js-xlsx'
]);

app.config(['$locationProvider', function ($locationProvider) {
    $locationProvider.hashPrefix('');
}]);

app.config(function ($stateProvider, $urlRouterProvider) {

    // Configure URL mapping for non existent URLs
  $urlRouterProvider.when('/dashboard', '/dashboard/team/manage')
  $urlRouterProvider.when('/dashboard/team', '/dashboard/team/manage')
  $urlRouterProvider.when('/dashboard/tasks', '/dashboard/tasks/manage')
  $urlRouterProvider.when('/dashboard/forms', '/dashboard/forms/manage')

  $stateProvider
      // Homepage Mappings
    .state('home', {
      abstract: true,
      url: '',
      templateUrl: '../views/home.html'
    })
    .state('home.features', {
        url: '',
        templateUrl: '../views/home/features.html'
    })
    .state('home.login', {
        url: '/login',
        templateUrl: '../views/home/login.html',
        controller: 'LoginCtrl'
    })
    .state('home.register', {
        url: '/register',
        templateUrl: '../views/home/register.html',
        controller: 'RegisterCtrl'
    })
      // Dashboard mappings
    .state('dashboard', {
        abstract: true,
        url: '/dashboard',
        templateUrl: '../views/dashboard.html',
        controller: 'DashboardCtrl'
    })
    .state('dashboard.team', {
        abstract: true,
        url: '/team'
    })
    .state('dashboard.team.manage', {
        url: '/manage',
        templateUrl: '../views/dashboard/team/manage.html',
        controller: 'TeamManageCtrl'
    })
    .state('dashboard.team.report', {
        url: '/report',
        templateUrl: '../views/dashboard/team/report.html',
        controller: 'TeamReportCtrl'
    })
    .state('dashboard.tasks', {
        abstract: true,
        url: '/tasks'
    })
    .state('dashboard.tasks.manage', {
        url: '/manage',
        templateUrl: '../views/dashboard/tasks/manage.html',
        controller: 'TaskManageCtrl'
    })
    .state('dashboard.tasks.report', {
        url: '/report',
        templateUrl: '../views/dashboard/tasks/report.html',
        controller: 'TaskReportCtrl'
    })
    .state('dashboard.forms', {
        abstract: true,
        url: '/forms'
    })
    .state('dashboard.forms.manage', {
        url: '/manage',
        templateUrl: '../views/dashboard/forms/manage.html',
        controller: 'FormManageCtrl'
    })
    .state('dashboard.forms.edit', {
        url: '/edit',
        templateUrl: '../views/dashboard/forms/editor.html',
        controller: 'FormEditorCtrl'
    })
});