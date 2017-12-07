package navimateforbusiness

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/api/auth/register"        (controller: "AuthApi") { action = [POST: "register"] }
        "/api/auth/login"           (controller: "AuthApi") { action = [POST: "login"] }
        "/api/auth/logout"          (controller: "AuthApi") { action = [GET: "logout"] }
        "/api/auth/email"           (controller: "AuthApi") { action = [POST: "email"] }
        "/api/auth/forgotPassword"  (controller: "AuthApi") { action = [POST: "forgotPassword"] }

        // User Info APIs
        "/api/users/changePassword" (controller: "UserApi") {action = [POST: "changePassword"]}
        "/api/users/team"           (controller: "UserApi") {action = [GET: "getTeam",      POST: "addRep"]}
        "/api/users/team/remove"    (controller: "UserApi") {action = [POST: "removeReps"]}
        "/api/users/lead"           (controller: "UserApi") {action = [GET: "getLead",      POST: "editLeads"]}
        "/api/users/lead/remove"    (controller: "UserApi") {action = [POST: "removeLeads"]}
        "/api/users/task"           (controller: "UserApi") {action = [GET: "getTask",      POST: "addTasks"]}
        "/api/users/task/close"     (controller: "UserApi") {action = [POST: "closeTasks"]}
        "/api/users/task/remove"    (controller: "UserApi") {action = [POST: "removeTasks"]}
        "/api/users/task/stoprenew" (controller: "UserApi") {action = [POST: "stopTaskRenewal"]}
        "/api/users/form"           (controller: "UserApi") {action = [GET: "getForm",      POST: "editForm"]}
        "/api/reports/team"         (controller: "UserApi") {action = [GET: "getTeamReport"]}
        "/api/reports/lead"         (controller: "UserApi") {action = [GET: "getLeadReport"]}
        "/api/leads/upload"         (controller: "UserApi") {action = [POST: "uploadLeads"]}

        // App APIs
        "/api/app/update"           (controller: "AppApi") {action = [POST: "checkForUpdate"]}

        // Rep APIs
        "/api/reps/profile"         (controller: "RepApi") {action = [POST: "getMyProfile"]}
        "/api/reps/register"        (controller: "RepApi") {action = [POST: "register"]}
        "/api/reps/submitForm"      (controller: "RepApi") {action = [POST: "submitForm"]}
        "/api/reps/sync/tasks"      (controller: "RepApi") {action = [POST: "syncTasks"]}
        "/api/reps/sync/leads"      (controller: "RepApi") {action = [POST: "syncLeads"]}
        "/api/reps/sync/templates"  (controller: "RepApi") {action = [POST: "syncTemplates"]}
        "/api/reps/fcm"             (controller: "RepApi") {action = [POST: "updateFcm"]}
        "/api/reps/otp"             (controller: "RepApi") {action = [POST: "sendOtpSms"]}

        // Photo APIs
        "/api/photos/upload"        (controller: "PhotoApi") {action = [POST: "upload"]}
        "/api/photos/get"           (controller: "PhotoApi") {action = [GET: "get"]}

        // Tracking APIs
        "/api/track/start"          (controller: "TrackingApi") {action = [POST: "start"]}
        "/api/track/refresh"        (controller: "TrackingApi") {action = [POST: "refresh"]}
        "/api/track/stop"           (controller: "TrackingApi") {action = [POST: "stop"]}
        "/api/track/data"           (controller: "TrackingApi") {action = [GET: "getData", POST: "postData"]}

        // Google API access
        "/api/googleapis/autocomplete"      (controller: "GoogleApi") {action = [GET: "autocomplete"]}
        "/api/googleapis/geocode"           (controller: "GoogleApi") {action = [GET: "geocode"]}
        "/api/googleapis/geocode/reverse"   (controller: "GoogleApi") {action = [GET: "reverseGeocode"]}

        "500"(controller: "Utils", action: "handleError")
        "404"(controller: "Utils", action: "handle404")

        "/"(uri: '/static/index.html')
    }
}
