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

        // User Info APIs
        "/api/users/team"           (controller: "UserApi") {action = [GET: "getTeam",      POST: "addRep"]}
        "/api/users/team/remove"    (controller: "UserApi") {action = [POST: "removeReps"]}
        "/api/users/lead"           (controller: "UserApi") {action = [GET: "getLead",      POST: "editLeads"]}
        "/api/users/lead/remove"    (controller: "UserApi") {action = [POST: "removeLeads"]}
        "/api/users/task"           (controller: "UserApi") {action = [GET: "getTask",      POST: "addTasks"]}
        "/api/users/task/close"     (controller: "UserApi") {action = [POST: "closeTasks"]}
        "/api/users/form"           (controller: "UserApi") {action = [GET: "getForm",      POST: "editForm"]}
        "/api/reports/team"         (controller: "UserApi") {action = [GET: "getTeamReport"]}
        "/api/reports/lead"         (controller: "UserApi") {action = [GET: "getLeadReport"]}
        "/api/leads/upload"         (controller: "UserApi") {action = [POST: "uploadLeads"]}

        // Rep APIs
        "/api/reps/profile"         (controller: "RepApi") {action = [POST: "getMyProfile"]}
        "/api/reps/task"            (controller: "RepApi") {action = [GET: "getTasks", POST: "submitForm"]}
        "/api/reps/fcm"             (controller: "RepApi") {action = [POST: "updateFcm"]}
        "/api/reps/otp"             (controller: "RepApi") {action = [POST: "sendOtpSms"]}

        // Google API access
        "/api/googleapis/autocomplete"      (controller: "GoogleApi") {action = [GET: "autocomplete"]}
        "/api/googleapis/geocode"           (controller: "GoogleApi") {action = [GET: "geocode"]}
        "/api/googleapis/geocode/reverse"   (controller: "GoogleApi") {action = [GET: "reverseGeocode"]}

        "500"(controller: "Utils", action: "handleError")
        "404"(controller: "Utils", action: "handle404")

        "/"(uri: '/static/index.html')
    }
}
