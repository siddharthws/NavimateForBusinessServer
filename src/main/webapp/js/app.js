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

// Http Response interceptor to parse certain types of error codes
app.factory('httpResponseInterceptor', ['$q', '$injector',
    function($q, $injector) {
        return {
            response: function(responseData) {
                return responseData;
            },
            responseError: function error(response) {
                switch (response.status) {
                    case 401:
                        $injector.get('$state').go('login')
                        break;
                    default:
                        console.log("Unknown error from server : " + response.status)
                }

                return $q.reject(response);
            }
        };
    }
]);

app.config(['$locationProvider', function ($locationProvider) {
    $locationProvider.hashPrefix('');
}]);

app.config(function ($stateProvider, $urlRouterProvider, $httpProvider) {
    // Configure Http Response Interceptor
    $httpProvider.interceptors.push('httpResponseInterceptor');

    // Configure URL mapping for non existent URLs
  $urlRouterProvider.when('', '/')
  $urlRouterProvider.when('/help', '/help/topics')
  $urlRouterProvider.when('/dashboard', '/dashboard/team/manage')
  $urlRouterProvider.when('/dashboard/team', '/dashboard/team/manage')
  $urlRouterProvider.when('/dashboard/tasks', '/dashboard/tasks/manage')
  $urlRouterProvider.when('/dashboard/forms', '/dashboard/forms/manage')

  $stateProvider
      // Homepage Mappings
    .state('home', {
        url: '/',
        templateUrl: '/static/views/home.html',
        controller: 'HomeCtrl'
    })
    .state('login', {
        url: '/',
        templateUrl: '/static/views/home.html',
        controller: 'HomeCtrl',
        params: {
            login: true
        }
    })
    .state('legal', {
        url: '/legal',
        templateUrl: '/static/views/legal.html'
    })
      // Help Mappings
    .state('help',{
        abstract: true,
        url: '/help',
        templateUrl: '/static/views/help.html',
        controller: 'HelpCtrl'
    })
    .state('help.topics',{
        url: '/topics',
        templateUrl: '/static/views/help/topics.html',
    })
    .state('help.app-about',{
        url: '/app-about',
        templateUrl: '/static/views/help/app/about.html',
    })
    .state('help.app-opt-route',{
        url: '/app-opt-route',
        templateUrl: '/static/views/help/app/opt-route.html',
    })
    .state('help.app-submitform',{
        url: '/app-submitform',
        templateUrl: '/static/views/help/app/submitform.html',
    })
    .state('help.app-viewtask',{
        url: '/app-viewtask',
         templateUrl: '/static/views/help/app/viewtask.html',
    })
    .state('help.form-about',{
        url: '/form-about',
        templateUrl: '/static/views/help/form/about.html',
    })
    .state('help.form-updatedef',{
        url: '/form-updatedef',
        templateUrl: '/static/views/help/form/updatedef.html',
    })
    .state('help.lead-about',{
        url: '/lead-about',
        templateUrl: '/static/views/help/lead/about.html',
    })
    .state('help.lead-add',{
        url: '/lead-add',
        templateUrl: '/static/views/help/lead/add.html',
    })
    .state('help.lead-edit',{
        url: '/lead-edit',
        templateUrl: '/static/views/help/lead/edit.html',
    })
    .state('help.lead-remove',{
        url: '/lead-remove',
        templateUrl: '/static/views/help/lead/remove.html',
    })
    .state('help.register-fieldrep',{
        url: '/register-fieldrep',
        templateUrl: '/static/views/help/register/fieldrep.html',
    })
    .state('help.register-manage',{
        url: '/register-manage',
        templateUrl: '/static/views/help/register/manage.html',
    })
    .state('help.reports-about',{
        url: '/reports-about',
        templateUrl: '/static/views/help/reports/about.html',
    })
    .state('help.reports-export',{
        url: '/reports-export',
        templateUrl: '/static/views/help/reports/export.html',
    })
    .state('help.reports-filter',{
        url: '/reports-filter',
        templateUrl: '/static/views/help/reports/filter.html',
    })
    .state('help.reports-lead',{
        url: '/reports-lead',
        templateUrl: '/static/views/help/reports/lead.html',
    })
    .state('help.reports-team',{
        url: '/reports-team',
        templateUrl: '/static/views/help/reports/team.html',
    })
    .state('help.task-about',{
        url: '/task-about',
        templateUrl: '/static/views/help/task/about.html',
    })
    .state('help.task-close',{
        url: '/task-close',
        templateUrl: '/static/views/help/task/close.html',
    })
    .state('help.task-create',{
        url: '/task-create',
        templateUrl: '/static/views/help/task/create.html',
    })
    .state('help.team-about',{
        url: '/team-about',
        templateUrl: '/static/views/help/team/about.html',
    })
    .state('help.team-add',{
        url: '/team-add',
        templateUrl: '/static/views/help/team/add.html',
    })
    .state('help.team-remove',{
        url: '/team-remove',
        templateUrl: '/static/views/help/team/remove.html',
    })
      // Dashboard mappings
    .state('dashboard', {
        abstract: true,
        url: '/dashboard',
        templateUrl: '/static/views/dashboard.html',
        controller: 'DashboardCtrl'
    })
    .state('dashboard.team-manage', {
        url: '/team/manage',
        templateUrl: '/static/views/dashboard/team/manage.html',
        controller: 'TeamManageCtrl'
    })
    .state('dashboard.team-report', {
        url: '/team/report',
        templateUrl: '/static/views/dashboard/team/report.html',
        controller: 'TeamReportCtrl'
    })
    .state('dashboard.leads-manage', {
        url: '/leads/manage',
        templateUrl: '/static/views/dashboard/leads/manage.html',
        controller: 'LeadManageCtrl'
    })
    .state('dashboard.leads-report', {
        url: '/leads/report',
        templateUrl: '/static/views/dashboard/leads/report.html',
        controller: 'LeadReportCtrl'
    })
    .state('dashboard.tasks-manage', {
        url: '/tasks/manage',
        templateUrl: '/static/views/dashboard/tasks/manage.html',
        controller: 'TaskManageCtrl'
    })
    .state('dashboard.forms-manage', {
        url: '/forms/manage',
        templateUrl: '/static/views/dashboard/forms/manage.html',
        controller: 'FormManageCtrl'
    })
});