package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.ApiException
import navimateforbusiness.Constants
import navimateforbusiness.Role
import navimateforbusiness.Task
import navimateforbusiness.TaskStatus
import navimateforbusiness.User
import org.grails.web.json.JSONArray

class RepApiController {

    def getMyProfile() {
        def phoneNumber = request.getHeader("phoneNumber")

        // Check if the rep is registered. (Rep is registered from the dashboard)
        User rep = User.findByPhoneNumberAndRole(phoneNumber, Role.REP)
        if (!rep) {
            throw new ApiException("Rep not registered", Constants.HttpCodes.BAD_REQUEST)
        }

        // Return user information
        def resp = navimateforbusiness.Marshaller.serializeUser(rep)
        render resp as JSON
    }

    def getTasks() {
        def id = request.getHeader("id")
        User rep = User.findById(id)
        if (!rep) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }

        // Get Task List
        List<Task> tasks = Task.findAllByRep(rep)

        def resp = new JSONArray()
        tasks.each { task ->
            if (task.status == TaskStatus.OPEN) {
                resp.add(navimateforbusiness.Marshaller.serializeTaskForRep(task))
            }
        }
        render resp as JSON
    }
}
