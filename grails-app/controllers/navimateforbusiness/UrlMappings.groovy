package navimateforbusiness

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        /*------------------------- Authentication APIs --------------------------*/
        "/api/auth/register"        (controller: "AuthApi") { action = [POST: "register"] }
        "/api/auth/login"           (controller: "AuthApi") { action = [POST: "login"] }
        "/api/auth/logout"          (controller: "AuthApi") { action = [GET: "logout"] }
        "/api/auth/email"           (controller: "AuthApi") { action = [POST: "email"] }
        "/api/auth/forgotPassword"  (controller: "AuthApi") { action = [POST: "forgotPassword"] }

        /*------------------------- Admin APIs --------------------------*/
        "/api/admin/accSettings"        (controller: "AdminApi") {action = [POST: "updateSettings"]}
        "/api/admin/team/edit"          (controller: "AdminApi") {action = [POST: "editTeam"]}
        "/api/admin/leads/edit"         (controller: "AdminApi") {action = [POST: "editLeads"]}
        "/api/admin/templates/edit"     (controller: "AdminApi") {action = [POST: "editTemplates"]}
        "/api/admin/removeTemplates"    (controller: "AdminApi") {action = [POST: "removeTemplates"]}
        "/api/admin/team/import"        (controller: "AdminApi") {action = [POST: "importTeam"]}

        /*------------------------- CC APIs --------------------------*/
        "/api/cc/team/getManagers"   (controller: "CcApi") {action = [POST: "getManagers"]}

        /*------------------------- Manager APIs --------------------------*/
        // Team related APIs
        "/api/manager/team/getByIds"     (controller: "ManagerApi") {action = [POST: "getTeamById"]}
        "/api/manager/team/search"       (controller: "ManagerApi") {action = [POST: "searchTeam"]}
        "/api/manager/team/getTable"     (controller: "ManagerApi") {action = [POST: "getTeamTable"]}
        "/api/manager/team/remove"       (controller: "ManagerApi") {action = [POST: "removeReps"]}
        "/api/manager/team/getIds"       (controller: "ManagerApi") {action = [POST: "getRepIds"]}
        "/api/manager/team/export"       (controller: "ManagerApi") {action = [POST: "exportTeam"]}

        // Lead Related APIs
        "/api/manager/leads/getByIds"    (controller: "ManagerApi") {action = [POST: "getLeadsById"]}
        "/api/manager/leads/getTable"    (controller: "ManagerApi") {action = [POST: "getLeadTable"]}
        "/api/manager/leads/getIds"      (controller: "ManagerApi") {action = [POST: "getLeadIds"]}
        "/api/manager/leads/search"      (controller: "ManagerApi") {action = [POST: "searchLeads"]}
        "/api/manager/leads/export"      (controller: "ManagerApi") {action = [POST: "exportLeads"]}
        "/api/manager/leads/import"      (controller: "ManagerApi") {action = [POST: "importLeads"]}

        // Task Related APIs
        "/api/manager/tasks/getByIds"    (controller: "ManagerApi") {action = [POST: "getTasksById"]}
        "/api/manager/tasks/getTable"    (controller: "ManagerApi") {action = [POST: "getTaskTable"]}
        "/api/manager/tasks/getIds"      (controller: "ManagerApi") {action = [POST: "getTaskIds"]}
        "/api/manager/tasks/edit"        (controller: "ManagerApi") {action = [POST: "editTasks"]}
        "/api/manager/tasks/export"      (controller: "ManagerApi") {action = [POST: "exportTasks"]}

        // Form Related APIs
        "/api/manager/forms/getTable"    (controller: "ManagerApi") {action = [POST: "getFormTable"]}
        "/api/manager/forms/getIds"      (controller: "ManagerApi") {action = [POST: "getFormIds"]}
        "/api/manager/forms/export"      (controller: "ManagerApi") {action = [POST: "exportForms"]}

        // Template related APIs
        "/api/manager/templates/getAll"  (controller: "ManagerApi") {action = [POST: "getTemplates"]}

        /*------------------------- User APIs to be removed --------------------------*/
        "/api/users/changePassword" (controller: "UserApi") {action = [POST: "changePassword"]}
        "/api/users/lead/remove"    (controller: "UserApi") {action = [POST: "removeLeads"]}
        "/api/users/task/close"     (controller: "UserApi") {action = [POST: "closeTasks"]}
        "/api/users/task/remove"    (controller: "UserApi") {action = [POST: "removeTasks"]}
        "/api/users/task/stoprenew" (controller: "UserApi") {action = [POST: "stopTaskRenewal"]}
        "/api/users/locationReport" (controller: "UserApi") {action = [GET: "getLocationReport"]}

        /*------------------------- Rep APIs --------------------------*/
        "/api/reps/appStart"        (controller: "RepApi") {action = [GET: "appStart"]}
        "/api/reps/register"        (controller: "RepApi") {action = [POST: "register"]}
        "/api/reps/submitForm"      (controller: "RepApi") {action = [POST: "submitForm"]}
        "/api/reps/sync/tasks"      (controller: "RepApi") {action = [POST: "syncTasks"]}
        "/api/reps/sync/leads"      (controller: "RepApi") {action = [POST: "syncLeads"]}
        "/api/reps/sync/templates"  (controller: "RepApi") {action = [POST: "syncTemplates"]}
        "/api/reps/sync/forms"      (controller: "RepApi") {action = [POST: "syncForms"]}
        "/api/reps/sync/fields"     (controller: "RepApi") {action = [POST: "syncFields"]}
        "/api/reps/sync/data"       (controller: "RepApi") {action = [POST: "syncData"]}
        "/api/reps/sync/values"     (controller: "RepApi") {action = [POST: "syncValues"]}
        "/api/reps/fcm"             (controller: "RepApi") {action = [POST: "updateFcm"]}
        "/api/reps/name"            (controller: "RepApi") {action = [POST: "updateName"]}
        "/api/reps/otp"             (controller: "RepApi") {action = [POST: "sendOtpSms"]}
        "/api/reps/locationReport"  (controller: "RepApi") {action = [POST: "syncLocationReport"]}
        "/api/reps/addTask"         (controller: "RepApi") {action = [POST: "addTask"]}
        "/api/reps/addLead"         (controller: "RepApi") {action = [POST: "addLead"]}

        /*------------------------- External APIs --------------------------*/
        "/api/ext/sync/managers"        (controller: "ExtApi") {action = [POST: "syncManagers"]}
        "/api/ext/sync/reps"            (controller: "ExtApi") {action = [POST: "syncReps"]}
        "/api/ext/sync/leads"           (controller: "ExtApi") {action = [POST: "syncLeads"]}
        "/api/ext/sync/tasks"           (controller: "ExtApi") {action = [POST: "syncTasks"]}
        "/api/ext/report/forms"         (controller: "ExtApi") {action = [GET: "getFormReport"]}

        /*------------------------- Porting APIs --------------------------*/
        "/api/port/fixDates"            (controller: "PortingApi") {action = [GET: "fixDates"]}
        "/api/port/mongoLeads"          (controller: "PortingApi") {action = [GET: "mongoLeads"]}
        "/api/port/taskLeads"           (controller: "PortingApi") {action = [GET: "taskLeads"]}

        /*------------------------- Other APIs --------------------------*/
        // Tracking related APIs
        "/api/track/start"          (controller: "TrackingApi") {action = [POST: "start"]}
        "/api/track/refresh"        (controller: "TrackingApi") {action = [POST: "refresh"]}
        "/api/track/stop"           (controller: "TrackingApi") {action = [POST: "stop"]}
        "/api/track/data"           (controller: "TrackingApi") {action = [GET: "getData", POST: "postData"]}

        // Google related APIs
        "/api/googleapis/autocomplete"      (controller: "GoogleApi") {action = [GET: "autocomplete"]}
        "/api/googleapis/geocode"           (controller: "GoogleApi") {action = [GET: "geocode"]}
        "/api/googleapis/geocode/reverse"   (controller: "GoogleApi") {action = [GET: "reverseGeocode"]}

        // App related APIs
        "/api/app/update"           (controller: "AppApi") {action = [POST: "checkForUpdate"]}
        "/api/app/acra"             (controller: "AppApi") {action = [POST: "acra"]}

        // Photo Related APIs
        "/api/photos/upload"        (controller: "PhotoApi") {action = [POST: "upload"]}
        "/api/photos/get"           (controller: "PhotoApi") {action = [GET: "get"]}

        "500"(controller: "Utils", action: "handleError")
        "404"(controller: "Utils", action: "handle404")
        "/"(uri: '/static/index.html')
    }
}
