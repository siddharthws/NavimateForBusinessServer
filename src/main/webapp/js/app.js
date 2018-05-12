var app = angular.module("navimate", [
  'ui.router',
  'ngStorage',
  'angular-js-xlsx',
  'ngMaterial',
  'ngMap',
  'ngTable',
  'ngCookies',
  'angularjs-dropdown-multiselect',
  'ngFileSaver',
  'angular.filter',
  'chart.js'
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
                        $injector.get('$state').go('home.login')
                        break;
                    default:
                        console.log("Unknown error from server : " + response.status)
                }

                return $q.reject(response);
            }
        };
    }
]);

// Filter to convert form byte array to base string
app.filter('bytetobase', function () {
    return function (buffer) {
        var binary = '';
        var bytes = new Uint8Array(buffer);
        var len = bytes.byteLength;
        for (var i = 0; i < len; i++) {
            binary += String.fromCharCode(bytes[i]);
        }
        return window.btoa(binary);
    };
});

app.config(['$locationProvider', function ($locationProvider) {
    $locationProvider.hashPrefix('');
}]);

app.config(function ($stateProvider, $urlRouterProvider, $httpProvider, $compileProvider) {
    $compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|file|ftp|blob):|data:image\//);

    // Configure Http Response Interceptor
    $httpProvider.interceptors.push('httpResponseInterceptor');

    // Configure URL mapping for non existent URLs
  $urlRouterProvider.when('', '/home/login')
  $urlRouterProvider.when('/', '/home/login')
  $urlRouterProvider.when('/home', '/home/login')
  $urlRouterProvider.when('/dashboard', '/dashboard/team/manage')
  $urlRouterProvider.when('/dashboard/team', '/dashboard/team/manage')
  $urlRouterProvider.when('/dashboard/tasks', '/dashboard/tasks/manage')
  $urlRouterProvider.when('/dashboard/forms', '/dashboard/forms/manage')

  $stateProvider
      // Homepage Mappings
    .state('home', {
        url: '/home',
        abstract: true,
        templateUrl: '/static/views/home.html',
        controller: 'HomeCtrl as vm'
    })
    .state('home.login', {
        url: '/login',
        templateUrl: '/static/views/home/login.html',
        controller: 'LoginCtrl as vm'
    })
    .state('home.forgotPassword', {
        url: '/forgotPassword',
        templateUrl: '/static/views/home/forgotPassword.html',
        controller: 'ForgotPasswordCtrl as vm'
    })
    .state('home.register', {
        url: '/register',
        templateUrl: '/static/views/home/register.html',
        controller: 'RegisterCtrl as vm'
    })
    .state('home.otp', {
        url: '/otp',
        templateUrl: '/static/views/home/otp.html',
        controller: 'OtpCtrl as vm'
    })
    .state('legal', {
        url: '/legal',
        templateUrl: '/static/views/legal.html'
    })
      // Help Mappings
    .state('help',{
        url: '/help',
        templateUrl: '/static/views/help.html',
        controller: 'HelpCtrl'
    })
    /*.state('help.topics',{
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
    .state('help.lead-manage',{
        url: '/lead-manage',
        templateUrl: '/static/views/help/lead/manage.html',
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
    .state('help.task-manage',{
        url: '/task-manage',
        templateUrl: '/static/views/help/task/manage.html',
    })
    .state('help.team-about',{
        url: '/team-about',
        templateUrl: '/static/views/help/team/about.html',
    })
    .state('help.team-manage',{
        url: '/team-manage',
        templateUrl: '/static/views/help/team/manage.html',
    })*/
      // Dashboard mappings
    .state('dashboard-loading', {
        url: '/loading',
        templateUrl: '/static/views/dashboard/loading.html',
        controller: 'DashboardLoadingCtrl as vm'
    })
    .state('dashboard', {
        abstract: true,
        url: '/dashboard',
        templateUrl: '/static/views/dashboard.html',
        controller: 'DashboardCtrl as vm'
    })
    .state('dashboard.team-manage', {
        url: '/team/manage',
        templateUrl: '/static/views/dashboard/team/manage.html',
        controller: 'TeamManageCtrl as $ctrl'
    })
    .state('dashboard.leads-manage', {
        url: '/leads/manage',
        templateUrl: '/static/views/dashboard/leads/manage.html',
        controller: 'LeadManageCtrl as $ctrl'
    })
    .state('dashboard.tasks-manage', {
        url: '/tasks/manage',
        templateUrl: '/static/views/dashboard/tasks/manage.html',
        controller: 'TaskManageCtrl as $ctrl'
    })
    .state('dashboard.reports-submission', {
        url: '/submission',
        templateUrl: '/static/views/dashboard/reports/submission.html',
        controller: 'SubmissionReportCtrl as $ctrl'
    })
    .state('dashboard.reports-movement', {
        url: '/movement',
        templateUrl: '/static/views/dashboard/reports/movement.html',
        controller: 'MovementReportCtrl as $ctrl'
    })
    .state('dashboard.templates-form', {
        url: '/templates/form',
        templateUrl: '/static/views/dashboard/templates/form.html',
        controller: 'FormTemplatesCtrl as $ctrl'
    })
    .state('dashboard.templates-lead', {
        url: '/templates/lead',
        templateUrl: '/static/views/dashboard/templates/lead.html',
        controller: 'LeadTemplatesCtrl as $ctrl'
    })
    .state('dashboard.templates-task', {
        url: '/templates/task',
        templateUrl: '/static/views/dashboard/templates/task.html',
        controller: 'TaskTemplatesCtrl as $ctrl'
    })
    .state('dashboard.company-settings', {
        url: '/company/settings',
        templateUrl: '/static/views/dashboard/company/settings.html',
        controller: 'CompanySettingsCtrl as $ctrl'
      })
    .state('photos', {
          url: '/photos?name',
          templateUrl: '/static/views/photos.html',
          controller: 'PhotosCtrl'
    })
});

app.directive('stringToNumber', function() {
    return {
        require: 'ngModel',
        link: function(scope, element, attrs, ngModel) {
            ngModel.$parsers.push(function(value) {
                return '' + value;
            });
            ngModel.$formatters.push(function(value) {
                return parseFloat(value);
            });
        }
    };
});