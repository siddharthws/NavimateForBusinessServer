package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.ApiException
import navimateforbusiness.Constants
import navimateforbusiness.Lead
import navimateforbusiness.Task
import navimateforbusiness.TaskStatus
import navimateforbusiness.User

class UserApiController {

    def authService
    def taskService
    def fcmService
    def reportService
    def leadService

    def changePassword() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Compare current password
        if (!request.JSON.oldPassword.equals(user.password)) {
            throw new ApiException("Password Validation Failed...", Constants.HttpCodes.BAD_REQUEST)
        }

        // Update password
        user.password = request.JSON.newPassword
        user.save(flush: true, failOnError: true)

        def resp = [success: true]
        render resp as JSON
    }

    /*
     * Remove lead function receives a JSON object from the frontend containing leads to be removed.
     * If lead is not found an error will be displayed else it will remove the Manager associated with the lead.
     * It will check if the lead has any task assigned, if the task's status is OPEN then it will
     * send notification to respective representative and change the status to CLOSED.
     * Sends a notification to all representatives after closing the tasks.
     */
    def removeLeads() {
        // Get user
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get all leads of this user
        def leads = leadService.getForUser(user)

        // Iterate through IDs to be remove
        request.JSON.ids.each {id ->
            // Get lead with this id
            Lead lead = leads.find {it -> it.id == id}
            if (!lead) {
                throw new ApiException("Lead not found...", Constants.HttpCodes.BAD_REQUEST)
            }

            // Update and save lead
            lead.isRemoved = true
            lead.save(flush: true, failOnError: true)
        }

        def resp = [success: true]
        render resp as JSON
    }

    /*
     * Stop task renewal function gets a JSON object of tasks.
     * It validates the task, if the task is not found error will be displayed else it sets
     * the renewal period of the task to zero.
     */
    def stopTaskRenewal() {
        // Get user
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get all tasks of this user
        def tasks = taskService.getForUser(user)

        // Iterate through all tasks that need to be stopped for renewal
        request.JSON.ids.each {id ->
            // Get task with this id
            Task task = tasks.find {it -> it.id == id}
            if (!task) {
                throw new ApiException("Task not found...", Constants.HttpCodes.BAD_REQUEST)
            }

            // Update Task Period to 0
            task.period = 0
            task.save(flush: true, failOnError: true)
        }

        // Send back response
        def resp = [success: true]
        render resp as JSON
    }

    /*
     * Close task function gets a JSON object of tasks to be closed from the frontend.
     * It validates the tasks, if not found error is displayed.
     * If the task is found then checks the status of the task. If task's status is OPEN then the
     * assigned representatives are notified and the tasks status is updated to CLOSED.
     * A notification again is sent to all the representatives regarding the closed tasks.
     */
    def closeTasks() {
        // Get user
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get all tasks of this user
        def tasks = taskService.getForUser(user)
        log.error("IDs = " + request.JSON.ids)

        // Iterate through IDs to be remove
        def fcms = []
        request.JSON.ids.each {id ->
            // Get task with this id
            Task task = tasks.find {it -> it.id == id}
            if (!task) {
                throw new ApiException("Task not found...", Constants.HttpCodes.BAD_REQUEST)
            }

            // Save rep's fcm to be used later
            if (task.status == TaskStatus.OPEN) {
                if (!fcms.contains(task.rep.fcmId)) {
                    fcms.push(task.rep.fcmId)
                }
            }

            // Update and save task
            task.status = TaskStatus.CLOSED
            task.save(flush: true, failOnError: true)
        }

        // Send notifications to all reps
        fcms.each {fcm ->
            fcmService.notifyApp(fcm)
        }

        def resp = [success: true]
        render resp as JSON
    }

    def removeTasks() {
        // Get user
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get all tasks of this user
        def tasks = taskService.getForUser(user)

        // Iterate through IDs to be remove
        def fcms = []
        request.JSON.ids.each {id ->
            // Get task with this id
            Task task = tasks.find {it -> it.id == id}
            if (!task) {
                throw new ApiException("Task not found...", Constants.HttpCodes.BAD_REQUEST)
            }

            // Save rep's fcm to be used later
            if (task.status == TaskStatus.OPEN) {
                if (!fcms.contains(task.rep.fcmId)) {
                    fcms.push(task.rep.fcmId)
                }
            }

            // Update and save task
            task.isRemoved = true
            task.status = TaskStatus.CLOSED
            task.save(flush: true, failOnError: true)
        }

        // Send notifications to all reps
        fcms.each {fcm ->
            fcmService.notifyApp(fcm)
        }

        def resp = [success: true]
        render resp as JSON
    }

    def getLocationReport() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get and validate params
        long repId = params.long('repId')
        String date = params.selectedDate

        // Get rep from params
        def rep = User.findByAccountAndId(user.account, repId)

        // Get Report for this rep
        def resp = reportService.getLocationReport(rep, date)

        // Send response
        render resp as JSON
    }
}
