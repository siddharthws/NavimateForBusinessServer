package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.ApiException
import navimateforbusiness.Constants
import navimateforbusiness.Form
import navimateforbusiness.Lead
import navimateforbusiness.Role
import navimateforbusiness.SmsHelper
import navimateforbusiness.Task
import navimateforbusiness.User
import navimateforbusiness.UserStatus
import org.grails.web.json.JSONArray

import javax.xml.bind.Marshaller

class UserApiController {

    def authService
    def taskService

    def getMyProfile() {
        def accessToken = request.getHeader("X-Auth-Token")
        if (!accessToken) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }
        def user = authService.getUserFromAccessToken(accessToken)
        render navimateforbusiness.Marshaller.serializeUser(user) as JSON
    }

    def updateMyProfile() {
        def input = request.JSON
        //TODO:
    }

    def getTeam() {
        def accessToken = request.getHeader("X-Auth-Token")
        if (!accessToken) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }
        def user = authService.getUserFromAccessToken(accessToken)

        // Get Team List
        List<User> team = User.findAllByManager(user)

        def resp = new JSONArray()
        team.each { member ->
            resp.add(navimateforbusiness.Marshaller.serializeUser(member))
        }
        render resp as JSON
    }

    def addRep() {
        def accessToken = request.getHeader("X-Auth-Token")
        if (!accessToken) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }
        def user = authService.getUserFromAccessToken(accessToken)

        // Check if rep is already registered in this manager's team
        User rep = User.findByPhoneNumberAndManager(request.JSON.phoneNumber, user)
        if (rep) {
            rep.name = request.JSON.name
            rep.email = request.JSON.email
            rep.manager = user
            rep.account = user.account
        }
        else {
            // Create New Rep Object
            rep = new User(
                    account: user.account,
                    manager: user,
                    name: request.JSON.name,
                    email: request.JSON.email,
                    phoneNumber: request.JSON.phoneNumber,
                    role: Role.REP,
                    status: UserStatus.INACTIVE)

            // Send SMS to new user
            SmsHelper.SendSms(rep.phoneNumber, user.name + " has added you to navimate. Join on https://play.google.com/store/apps/details?id=com.navimate.business")
        }
        rep.save(flush: true, failOnError: true)

        def resp = [success: true]
        render resp as JSON
    }

    def removeReps() {
        def accessToken = request.getHeader("X-Auth-Token")
        if (!accessToken) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }
        def user = authService.getUserFromAccessToken(accessToken)

        // Get Reps from JSON
        JSONArray repsJson = JSON.parse(request.JSON.reps)
        repsJson.each {repJson ->
            User rep = User.findById(repJson.id)
            if (!rep) {
                throw new ApiException("Rep not found", Constants.HttpCodes.BAD_REQUEST)
            }

            // Remove Rep's Manager
            rep.manager = null
            rep.save(flush: true, failOnError: true)
        }

        def resp = [success: true]
        render resp as JSON
    }

    def getLead() {
        def accessToken = request.getHeader("X-Auth-Token")
        if (!accessToken) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }
        def user = authService.getUserFromAccessToken(accessToken)

        // Get Lead List
        List<Lead> leads = Lead.findAllByManager(user)

        def resp = new JSONArray()
        leads.each { lead ->
            resp.add(navimateforbusiness.Marshaller.serializeLead(lead))
        }
        render resp as JSON
    }

    def getTask() {
        def accessToken = request.getHeader("X-Auth-Token")
        if (!accessToken) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }
        def user = authService.getUserFromAccessToken(accessToken)

        // Get Form List
        List<Task> tasks = Task.findAllByManager(user)

        def resp = new JSONArray()
        tasks.each { task ->
            resp.add(navimateforbusiness.Marshaller.serializeTask(task))
        }
        render resp as JSON
    }

    def addTasks() {
        def accessToken = request.getHeader("X-Auth-Token")
        if (!accessToken) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }
        def user = authService.getUserFromAccessToken(accessToken)

        // Update tasks using service
        JSONArray tasksJson = JSON.parse(request.JSON.tasks)
        taskService.addTasks(user, tasksJson)

        // return resposne
        def resp = [success: true]
        render resp as JSON
    }

    def getForm() {
        def accessToken = request.getHeader("X-Auth-Token")
        if (!accessToken) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }
        def user = authService.getUserFromAccessToken(accessToken)

        // Get Form List
        List<Form> forms = Form.findAllByOwner(user)

        def resp = new JSONArray()
        forms.each { form ->
            resp.add(navimateforbusiness.Marshaller.serializeForm(form))
        }
        render resp as JSON
    }
}
