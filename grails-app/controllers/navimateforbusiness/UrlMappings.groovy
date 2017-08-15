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

        "500"(controller: "Utils", action: "handleError")
        "404"(controller: "Utils", action: "handle404")
    }
}
