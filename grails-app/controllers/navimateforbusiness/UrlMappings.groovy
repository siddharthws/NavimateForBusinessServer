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
        "/api/users/me"             (controller: "UserApi") {action = [GET: "getMyProfile", POST: "updateMyProfile"]}
        "/api/users/team"           (controller: "UserApi") {action = [GET: "getTeam",      POST: "addRep"]}
        "/api/users/team/remove"    (controller: "UserApi") {action = [POST: "removeReps"]}
        "/api/users/lead"           (controller: "UserApi") {action = [GET: "getLead",      POST: "editLeads"]}
        "/api/users/lead/remove"    (controller: "UserApi") {action = [POST: "removeLeads"]}
        "/api/users/task"           (controller: "UserApi") {action = [GET: "getTask",      POST: "addTasks"]}
        "/api/users/task/close"     (controller: "UserApi") {action = [POST: "closeTasks"]}
        "/api/users/form"           (controller: "UserApi") {action = [GET: "getForm",      POST: "editForm"]}

        // Rep APIs
        "/api/reps/me"              (controller: "RepApi") {action = [GET: "getMyProfile"]}
        "/api/reps/task"            (controller: "RepApi") {action = [GET: "getTasks", POST: "submitForm"]}

        // Google API access
        "/api/googleapis/autocomplete"      (controller: "GoogleApi") {action = [GET: "autocomplete"]}
        "/api/googleapis/geocode"           (controller: "GoogleApi") {action = [GET: "geocode"]}
        "/api/googleapis/geocode/reverse"   (controller: "GoogleApi") {action = [GET: "reverseGeocode"]}

        // Report Related APIs
        "/api/reports/team"     (controller: "ReportApi") {action = [GET: "getTeamReport"]}
        "/api/reports/lead"     (controller: "ReportApi") {action = [GET: "getLeadReport"]}

        // Lead Related APIs
        "/api/leads/upload"         (controller: "LeadApi") {action = [POST: "upload"]}

        "500"(controller: "Utils", action: "handleError")
        "404"(controller: "Utils", action: "handle404")
    }
}
