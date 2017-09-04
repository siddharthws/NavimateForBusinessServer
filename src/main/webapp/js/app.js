var app = angular.module("navimate", [
  'ui.router',
  'ngStorage',
  'angular-js-xlsx',
  'ngMaterial',
  'ngMap'
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
        url: '',
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
    .state('dashboard.team', {
        abstract: true,
        url: '/team',
        templateUrl: '../views/dashboard/team.html',
        controller: 'TeamCtrl'
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
    .state('dashboard.lead', {
        abstract: true,
        url: '/leads',
        templateUrl: '../views/dashboard/leads.html',
        controller: 'LeadCtrl'
    })
    .state('dashboard.lead.manage', {
        url: '/manage',
        templateUrl: '../views/dashboard/leads/manage.html',
        controller: 'LeadManageCtrl'
    })
    .state('dashboard.lead.report', {
        url: '/report',
        templateUrl: '../views/dashboard/leads/report.html',
        controller: 'LeadReportCtrl'
    })
    .state('dashboard.tasks', {
        abstract: true,
        url: '/tasks',
        templateUrl: '../views/dashboard/tasks.html',
        controller: 'TaskCtrl'
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
        url: '/forms',
        templateUrl: '../views/dashboard/forms.html',
        controller: 'FormCtrl'
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