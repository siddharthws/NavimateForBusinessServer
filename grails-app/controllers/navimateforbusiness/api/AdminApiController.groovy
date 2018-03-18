package navimateforbusiness.api


import navimateforbusiness.Constants
import navimateforbusiness.SmsHelper
import navimateforbusiness.Task
import navimateforbusiness.TaskStatus
import navimateforbusiness.Template
import navimateforbusiness.User
import org.grails.web.json.JSONArray
import grails.converters.JSON

class AdminApiController {

    // Service dependencies
    def authService
    def userService
    def templateService
    def fcmService

    def updateSettings() {
        // Get user
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Update account settings
        userService.updateAccountSettings(user, request.JSON)

        // Send success response
        def resp = [success: true]
        render resp as JSON
    }

    def editTeam() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Parse user JSON to user objects
        List<User> team = []
        request.JSON.team.each {userJson ->
            team.push(userService.repFromJson(userJson, user))
        }

        // Save user objects
        team.each {it -> it.save(flush: true, failOnError: true)}

        // Send SMS to all new users
        team.each {rep ->
            if (!rep.fcmId) {
                SmsHelper.SendSms('+' + rep.countryCode + rep.phone, user.name + " has added you to navimate. Join on https://play.google.com/store/apps/details?id=com.biz.navimate")
            }
        }

        // Return response
        def resp = [success: true]
        render resp as JSON
    }

    def editTemplates() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Parse each template to respective object
        def templatesJson = request.JSON.templates
        def templates = []
        templatesJson.each {templateJson ->
            templates.push(templateService.fromJson(templateJson, user))
        }

        def openTasks = []
        templates.each {template ->
            // Save template
            template.save(flush: true, failOnError: true)

            // Get open tasks that are affected due to this template
            switch (template.type) {
                case Constants.Template.TYPE_FORM :
                    openTasks.addAll(Task.findAllByFormTemplateAndStatus(template, TaskStatus.OPEN))
                    break
            }
        }

        // Collect FCM Ids of affected reps
        def fcms = []
        openTasks.each {task ->
            if (!fcms.contains(task.rep.fcmId)) {
                fcms.push(task.rep.fcmId)
            }
        }

        // Send notifications to all reps
        fcms.each {fcm ->
            fcmService.notifyApp(fcm)
        }

        // return response
        def resp = [success: true]
        render resp as JSON
    }

    def removeTemplates() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get Templates from JSON
        JSONArray templateIdsJson = request.JSON.templateIds
        templateIdsJson.each {templateId ->
            // Get Template
            Template template = Template.findByAccountAndId(user.account, templateId)
            if (!template) {
                throw new navimateforbusiness.ApiException("Template not found...", Constants.HttpCodes.BAD_REQUEST)
            }

            // Remove Template
            template.isRemoved = true
            template.save(flush: true, failOnError: true)
        }

        def resp = [success: true]
        render resp as JSON
    }
}
