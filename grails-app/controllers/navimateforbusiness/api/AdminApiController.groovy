package navimateforbusiness.api

import navimateforbusiness.util.Constants
import navimateforbusiness.LeadM
import navimateforbusiness.util.ApiException
import navimateforbusiness.util.SmsHelper
import navimateforbusiness.Template
import navimateforbusiness.User
import grails.converters.JSON

class AdminApiController {

    // Service dependencies
    def authService
    def userService
    def leadService
    def taskService
    def formService
    def templateService
    def fcmService
    def tableService
    def importService

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

        // Send FCM / SMS to all new users
        team.each {rep ->
            if (!fcmService.notifyUser(rep, Constants.Notifications.TYPE_ACCOUNT_ADDED)) {
                SmsHelper.SendSms('+' + rep.countryCode + rep.phone, user.name + " has added you to navimate. Join on https://play.google.com/store/apps/details?id=com.biz.navimate")
            }
        }

        // Return response
        def resp = [success: true]
        render resp as JSON
    }

    def removeTeam() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Remove selected reps
        request.JSON.ids.each {id ->
            User rep = userService.getRepForUserById(user, id)

            // Remove all tasks and forms of this rep
            def tasks = taskService.getAllForUserByFilter(user, [rep: [ids: [rep.id]]])
            tasks.each { taskService.remove(user, it) }

            def forms = formService.getForUser(rep)
            forms.each { formService.remove(user, it) }

            // Remove Rep's Manager & account
            rep.manager = null
            rep.account = null
            rep.save(flush: true, failOnError: true)

            // Send notification to app
            fcmService.notifyUser(rep, Constants.Notifications.TYPE_ACCOUNT_REMOVED)
        }

        def resp = [success: true]
        render resp as JSON
    }

    def editTemplates() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Parse each template to respective object
        def templatesJson = request.JSON.templates
        def templates = []
        def reps = []
        templatesJson.each {templateJson ->
            def template = templateService.fromJson(templateJson, user)
            templates.push(template)
            reps.addAll(templateService.getAffectedReps(user, template))
        }

        templates.each {template -> template.save(flush: true, failOnError: true)}

        // Collect FCM Ids of affected reps & notify each rep
        fcmService.notifyUsers(reps, Constants.Notifications.TYPE_TEMPLATE_UPDATE)

        // return response
        def resp = [success: true]
        render resp as JSON
    }

    def removeTemplates() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get Templates from JSON
        def reps = []
        request.JSON.ids.each {id ->
            // Get Template
            Template template = templateService.getForUserById(user, id)
            if (!template) {
                throw new ApiException("Template not found...", Constants.HttpCodes.BAD_REQUEST)
            }

            // Remove Template
            reps.addAll(templateService.getAffectedReps(user, template))
            templateService.remove(user, template)
        }

        // Collect FCM Ids of affected reps & notify each rep
        fcmService.notifyUsers(reps, Constants.Notifications.TYPE_TEMPLATE_UPDATE)

        def resp = [success: true]
        render resp as JSON
    }

    def importTeam() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get file
        def file = request.getFile('importFile')

        // Get table from file
        def table = tableService.parseExcel(file)

        // Validate all columns
        importService.validateTeam(table.columns, table.rows)

        // Get team JSON for each row
        def teamJson = []
        table.rows.eachWithIndex {row, i -> teamJson.push(importService.parseTeamRow(table.columns, row, i, user))}

        // Parse each JSON object into Team object
        def team = []
        teamJson.each {repJson -> team.push(userService.repFromJson(repJson, user))}

        team.each {rep ->rep.save(flush: true, failOnError: true)}

        def resp = [success: true]
        render resp as JSON
    }
}
