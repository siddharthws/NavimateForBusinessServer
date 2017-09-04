package navimateforbusiness

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/api/auth/register" (controller: "AuthApi") { action = [POST: "register"] }
        "/api/auth/login" (controller: "AuthApi") { action = [POST: "login"] }
        "/api/auth/logout" (controller: "AuthApi") { action = [GET: "logout"] }

        // User Info APIs
        "/api/users/me"         (controller: "UserApi") {action = [GET: "getMyProfile", POST: "updateMyProfile"]}
        "/api/users/team"       (controller: "UserApi") {action = [GET: "getTeam"]}
        "/api/users/lead"       (controller: "UserApi") {action = [GET: "getLead"]}
        "/api/users/task"       (controller: "UserApi") {action = [GET: "getTask"]}
        "/api/users/form"       (controller: "UserApi") {action = [GET: "getForm"]}

        // Lead Related APIs
        "/api/leads/upload"         (controller: "LeadApi") {action = [POST: "upload"]}

        "500"(controller: "Utils", action: "handleError")
        "404"(controller: "Utils", action: "handle404")
    }
}
