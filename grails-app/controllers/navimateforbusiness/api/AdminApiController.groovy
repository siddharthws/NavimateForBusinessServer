package navimateforbusiness.api

import navimateforbusiness.ApiException
import navimateforbusiness.Constants
import navimateforbusiness.LeadM
import navimateforbusiness.SmsHelper
import navimateforbusiness.Task
import navimateforbusiness.TaskStatus
import navimateforbusiness.Template
import navimateforbusiness.User
import grails.converters.JSON

class AdminApiController {

    // Service dependencies
    def authService
    def userService
    def leadService
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

    def removeTeam() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get reps under this user
        def reps = userService.getRepsForUser(user)

        // Get Reps IDs to remove
        def ids = request.JSON.ids

        // Get reps to remove
        def removeReps = []
        ids.each {id -> removeReps.push(reps.find {it -> it.id == id})}

        // Remove selected reps
        removeReps.each {rep ->
            // Remove Rep's Manager & account
            rep.manager = null
            rep.account = null

            // Save rep
            rep.save(flush: true, failOnError: true)
        }

        def resp = [success: true]
        render resp as JSON
    }

    def editLeads() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Parse lead JSON to task objects
        def leads = []
        request.JSON.leads.each {leadJson ->
            // Parse to lead object and assign update & create time
            LeadM lead = leadService.fromJson(leadJson, user)

            leads.push(lead)
        }

        // Save tasks
        leads.each {it -> it.save(flush: true, failOnError: true)}

        // Return response
        def resp = [success: true]
        render resp as JSON
    }

    def removeLeads() {
        // Get user
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Iterate through IDs to be remove
        request.JSON.ids.each {id ->
            // Get lead with this id
            LeadM lead = leadService.getForUserById(user, id)
            if (!lead) {
                throw new ApiException("Lead not found...", Constants.HttpCodes.BAD_REQUEST)
            }

            // Remove lead
            leadService.remove(user, lead)
        }

        def resp = [success: true]
        render resp as JSON
    }

    def editTemplates() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Parse each template to respective object
        def templatesJson = request.JSON.templates
        def templates = []
        templatesJson.each {templateJson ->
            def template = templateService.fromJson(templateJson, user)
            templates.push(template)
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
        def reps = []
        openTasks.each {task ->
            if (!reps.contains(task.rep)) {
                reps.push(task.rep)
            }
        }

        // Send notifications to all reps
        reps.each {User rep ->
            fcmService.notifyApp(rep)
        }

        // return response
        def resp = [success: true]
        render resp as JSON
    }

    def removeTemplates() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get Templates from JSON
        request.JSON.ids.each {id ->
            // Get Template
            Template template = templateService.getForUserById(user, id)
            if (!template) {
                throw new navimateforbusiness.ApiException("Template not found...", Constants.HttpCodes.BAD_REQUEST)
            }

            // Remove Template
            templateService.remove(user, template)
        }

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
