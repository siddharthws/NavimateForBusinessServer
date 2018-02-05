package navimateforbusiness.api


import grails.converters.JSON
import navimateforbusiness.Constants
import navimateforbusiness.Data
import navimateforbusiness.JsonToDomain
import navimateforbusiness.Task
import navimateforbusiness.TaskStatus
import navimateforbusiness.Template
import org.grails.web.json.JSONArray
import grails.converters.JSON

class AdminApiController {

    // Service dependencies
    def authService
    def userService

    def updateAccountSettings() {
        // Get user
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Update account settings
        userService.updateAccountSettings(user, request.JSON)

        // Send success response
        def resp = [success: true]
        render resp as JSON
    }

    def saveTemplate() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Parse to Template Object
        Template template = JsonToDomain.Template(request.JSON.template, user)

        if (!template.id) {
            // For new templates, save needs to be called twice
            Data defaultData = template.defaultData

            // Save template (default data will become null due to nullable constraint)
            template.save(flush: true, failOnError: true)

            // Save template with default data
            template.defaultData = defaultData
            defaultData.template = template
            template.save(flush: true, failOnError: true)
        } else {
            // Save Template Object
            template.save(flush: true, failOnError: true)
        }

        // Get open tasks that are affected due to this template
        def openTasks = []
        switch (template.type) {
            case Constants.Template.TYPE_FORM :
                openTasks = Task.findAllByFormTemplateAndStatus(template, TaskStatus.OPEN)
                break
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

        // return resposne
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
