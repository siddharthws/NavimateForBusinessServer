package navimateforbusiness

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        /*------------------------- Authentication APIs --------------------------*/
        "/api/auth/validateRegistration" (controller: "AuthApi") { action = [POST: "validateRegistration"] }
        "/api/auth/register"        (controller: "AuthApi") { action = [POST: "register"] }
        "/api/auth/login"           (controller: "AuthApi") { action = [POST: "login"] }
        "/api/auth/logout"          (controller: "AuthApi") { action = [GET: "logout"] }
        "/api/auth/forgotPassword"  (controller: "AuthApi") { action = [POST: "forgotPassword"] }

        /*------------------------- Admin APIs --------------------------*/
        "/api/admin/accSettings"        (controller: "AdminApi") {action = [POST: "updateSettings"]}
        "/api/admin/team/edit"          (controller: "AdminApi") {action = [POST: "editTeam"]}
        "/api/admin/team/remove"        (controller: "AdminApi") {action = [POST: "removeTeam"]}
        "/api/admin/templates/edit"     (controller: "AdminApi") {action = [POST: "editTemplates"]}
        "/api/admin/templates/remove"   (controller: "AdminApi") {action = [POST: "removeTemplates"]}
        "/api/admin/team/import"        (controller: "AdminApi") {action = [POST: "importTeam"]}

        /*------------------------- CC APIs --------------------------*/
        "/api/cc/team/getManagers"   (controller: "CcApi") {action = [POST: "getManagers"]}

        /*------------------------- Manager APIs --------------------------*/
        // Template related APIs
        "/api/manager/templates/search"  (controller: "ManagerApi") {action = [POST: "searchTemplates"]}

        // Team related APIs
        "/api/manager/team/getByIds"     (controller: "ManagerApi") {action = [POST: "getTeamById"]}
        "/api/manager/team/search"       (controller: "ManagerApi") {action = [POST: "searchTeam"]}
        "/api/manager/team/getTable"     (controller: "ManagerApi") {action = [POST: "getTeamTable"]}
        "/api/manager/team/getIds"       (controller: "ManagerApi") {action = [POST: "getRepIds"]}
        "/api/manager/team/export"       (controller: "ManagerApi") {action = [POST: "exportTeam"]}
        "/api/manager/team/searchReps"   (controller: "ManagerApi") {action = [POST: "searchReps"]}
        "/api/manager/team/searchNonReps"(controller: "ManagerApi") {action = [POST: "searchNonReps"]}

        // Lead Related APIs
        "/api/manager/leads/getByIds"    (controller: "ManagerApi") {action = [POST: "getLeadsById"]}
        "/api/manager/leads/getTable"    (controller: "ManagerApi") {action = [POST: "getLeadTable"]}
        "/api/manager/leads/getIds"      (controller: "ManagerApi") {action = [POST: "getLeadIds"]}
        "/api/manager/leads/edit"        (controller: "ManagerApi") {action = [POST: "editLeads"]}
        "/api/manager/leads/remove"      (controller: "ManagerApi") {action = [POST: "removeLeads"]}
        "/api/manager/leads/search"      (controller: "ManagerApi") {action = [POST: "searchLeads"]}
        "/api/manager/leads/export"      (controller: "ManagerApi") {action = [POST: "exportLeads"]}
        "/api/manager/leads/import"      (controller: "ManagerApi") {action = [POST: "importLeads"]}

        // Task Related APIs
        "/api/manager/tasks/getByIds"    (controller: "ManagerApi") {action = [POST: "getTasksById"]}
        "/api/manager/tasks/getTable"    (controller: "ManagerApi") {action = [POST: "getTaskTable"]}
        "/api/manager/tasks/getIds"      (controller: "ManagerApi") {action = [POST: "getTaskIds"]}
        "/api/manager/tasks/edit"        (controller: "ManagerApi") {action = [POST: "editTasks"]}
        "/api/manager/tasks/export"      (controller: "ManagerApi") {action = [POST: "exportTasks"]}
        "/api/manager/tasks/close"       (controller: "ManagerApi") {action = [POST: "closeTasks"]}
        "/api/manager/tasks/stopRenewal" (controller: "ManagerApi") {action = [POST: "stopTaskRenewal"]}
        "/api/manager/tasks/remove"      (controller: "ManagerApi") {action = [POST: "removeTasks"]}
        "/api/manager/tasks/search"      (controller: "ManagerApi") {action = [POST: "searchTasks"]}
        "/api/manager/tasks/import"      (controller: "ManagerApi") {action = [POST: "importTasks"]}

        // Form Related APIs
        "/api/manager/forms/getByIds"    (controller: "ManagerApi") {action = [POST: "getFormsById"]}
        "/api/manager/forms/getTable"    (controller: "ManagerApi") {action = [POST: "getFormTable"]}
        "/api/manager/forms/getIds"      (controller: "ManagerApi") {action = [POST: "getFormIds"]}
        "/api/manager/forms/export"      (controller: "ManagerApi") {action = [POST: "exportForms"]}
        "/api/manager/forms/remove"      (controller: "ManagerApi") {action = [POST: "removeForms"]}

        // Template related APIs
        "/api/manager/templates/getAll"  (controller: "ManagerApi") {action = [POST: "getTemplates"]}

        //Product Related APIs
        "/api/manager/products/edit"     (controller: "ManagerApi") {action = [POST: "editProduct"]}
        "/api/manager/products/getByIds" (controller: "ManagerApi") {action = [POST: "getProductsById"]}
        "/api/manager/products/getTable" (controller: "ManagerApi") {action = [POST: "getProductTable"]}
        "/api/manager/products/getIds"   (controller: "ManagerApi") {action = [POST: "getProductIds"]}
        "/api/manager/products/remove"   (controller: "ManagerApi") {action = [POST: "removeProduct"]}
        "/api/manager/products/search"   (controller: "ManagerApi") {action = [POST: "searchProducts"]}
        "/api/manager/products/import"   (controller: "ManagerApi") {action = [POST: "importProducts"]}
        "/api/manager/products/export"   (controller: "ManagerApi") {action = [POST: "exportProducts"]}

        /*------------------------- User APIs to be removed --------------------------*/
        "/api/users/changePassword" (controller: "UserApi") {action = [POST: "changePassword"]}
        "/api/users/locationReport" (controller: "UserApi") {action = [GET: "getLocationReport"]}

        /*------------------------- Rep APIs --------------------------*/
        "/api/reps/appStart"        (controller: "RepApi") {action = [GET: "appStart"]}
        "/api/reps/register"        (controller: "RepApi") {action = [POST: "register"]}
        "/api/reps/submitForm"      (controller: "RepApi") {action = [POST: "submitForm"]}
        "/api/reps/sync"            (controller: "RepApi") {action = [POST: "sync"]}
        "/api/reps/sync/tasks"      (controller: "RepApi") {action = [POST: "syncTasks"]}
        "/api/reps/sync/leads"      (controller: "RepApi") {action = [POST: "syncLeads"]}
        "/api/reps/sync/templates"  (controller: "RepApi") {action = [POST: "syncTemplates"]}
        "/api/reps/sync/forms"      (controller: "RepApi") {action = [POST: "syncForms"]}
        "/api/reps/sync/fields"     (controller: "RepApi") {action = [POST: "syncFields"]}
        "/api/reps/sync/data"       (controller: "RepApi") {action = [POST: "syncData"]}
        "/api/reps/sync/values"     (controller: "RepApi") {action = [POST: "syncValues"]}
        "/api/reps/product/id"      (controller: "RepApi") {action = [POST: "getProductById"]}
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
        "/api/port/fixManagers"            (controller: "PortingApi") {action = [GET: "fixManagers"]}
        "/api/port/fixFcms"                (controller: "PortingApi") {action = [GET: "fixFcms"]}
        "/api/port/fixIsRemoved"           (controller: "PortingApi") {action = [GET: "fixIsRemoved"]}
        "/api/port/publicLeads"            (controller: "PortingApi") {action = [GET: "publicLeads"]}
        "/api/port/fixDates"               (controller: "PortingApi") {action = [GET: "fixSubmitDates"]}
        "/api/port/refreshLocReport"       (controller: "PortingApi") {action = [GET: "refreshLocReport"]}
        "/api/port/taskResolveTime"        (controller: "PortingApi") {action = [GET: "taskResolveTime"]}
        "/api/port/taskPublicId"           (controller: "PortingApi") {action = [GET: "taskPublicId"]}
        "/api/port/isRemoveUserFix"        (controller: "PortingApi") {action = [GET: "isRemoveUserFix"]}
        "/api/port/fixNumberField"         (controller: "PortingApi") {action = [GET: "fixNumberField"]}
        "/api/port/mongoObjects"           (controller: "PortingApi") {action = [GET: "mongoObjects"]}
        "/api/port/productTemplates"       (controller: "PortingApi") {action = [GET: "createProductTemplates"]}
        "/api/port/fixProductField"        (controller: "PortingApi") {action = [GET: "fixProductField"]}
        "/api/port/fixAxisAccount"         (controller: "PortingApi") {action = [GET: "fixAxisAccount"]}

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
        "/api/acra/clear"           (controller: "AppApi") {action = [GET: "clearAcra"]}
        "/api/acra/get"             (controller: "AppApi") {action = [GET: "getAcra"]}

        // Photo Related APIs
        "/api/photos/upload"        (controller: "PhotoApi") {action = [POST: "upload"]}
        "/api/photos/uploadFile"    (controller: "PhotoApi") {action = [POST: "uploadFile"]}
        "/api/photos/get"           (controller: "PhotoApi") {action = [GET: "get"]}

        "500"(controller: "Utils", action: "handleError")
        "404"(controller: "Utils", action: "handle404")
        "/"(uri: '/static/index.html')
    }
}
