package navimateforbusiness.api

import com.mongodb.client.FindIterable
import navimateforbusiness.Account
import navimateforbusiness.Data
import navimateforbusiness.Form
import navimateforbusiness.LeadM
import grails.plugins.rest.client.RestBuilder
import navimateforbusiness.LocReport
import navimateforbusiness.LocSubmission
import navimateforbusiness.enums.Role
import navimateforbusiness.Task

import grails.converters.JSON
import navimateforbusiness.User
import navimateforbusiness.Template
import navimateforbusiness.enums.Visibility
import navimateforbusiness.util.Constants

import static com.mongodb.client.model.Filters.eq

class PortingApiController {

    def fixManagers() {
        // Iterate through all tasks
        Task.findAll().eachWithIndex { Task task, int i ->
            if (task.rep && task.account == task.rep.account && task.manager != task.rep.manager) {
                task.manager = task.rep.manager
                task.save(flush: true, failOnError: true)
                log.error(task.id + " : " + task.manager.name + " : " + task.rep.manager.name)
            }
        }

        def resp = [success: true]
        render resp as JSON
    }

    def fixFcms() {
        // Send ignorable notification to all reps to check if FCM is working
        def reps = User.findAllByRole(Role.REP)
        reps.eachWithIndex { User rep, int i ->
            if (rep.fcmId) {
                notifyApp(rep)
                sleep(100)
                log.error("Sent notification to rep " + i + " : " + reps.size())
            }
        }
    }


    def notifyApp (User user) {
        def bSent = false

        String fcmId = user?.fcmId
        if (fcmId) {
            // Send request to FCM server
            def request = new RestBuilder(connectTimeout:1000, readTimeout:20000)
            def resp = request.post("https://fcm.googleapis.com/fcm/send") {
                header 'Content-Type', 'application/json'
                header 'Authorization', 'key=AAAA2Ezvs_Y:APA91bFgkZh16BJHk0IZlkwXQAAJb6HwNC4svq0Q8knmkiyXTZOR9xGZ-QARpKUIDuuIJEvZq9UMfOPfYnUtpawLCr9Vrju32mk8dBYatBu3Rp7mSeuDb3pKfUr9ZOc6dTTdckYmd7hq'
                json {
                    data = {
                        type = 10
                    }
                    to = fcmId
                }
            }

            // Validate response
            if (!resp.json.success) {
                log.error("Error while sending FCM : " + resp.json + " Removing FCM ID = " + user.fcmId + " For " + user.id)
                user.fcmId = ""
                user.save(flush: true, failOnError: true)
            } else {
                bSent = true
            }
        }

        bSent
    }

    // Post is Removed so that all dependencies are satisfied
    def templateService
    def taskService
    def leadService
    def formService
    def fixIsRemoved() {
        // Hack
        def data = Data.findById(6129)
        data.template = Template.findById(6218)
        data.save(flush: true, failOnError: true)

        // Find all removed templates and remove them through service
        Template.findAllByIsRemoved(true).eachWithIndex { Template template, int i ->
            templateService.remove(template.account.admin, template)
            log.error("Removing template num " + i)
        }

        // Find all removed leads and remove them through service
        FindIterable fi = LeadM.find(eq("isRemoved", true))
        fi.eachWithIndex {LeadM lead, int i ->
            leadService.remove(Account.findById(lead.accountId).admin, lead)
            log.error("Removing lead num " + i)
        }

        // Find all removed tasks and remove them through service
        Task.findAllByIsRemoved(true).eachWithIndex { Task task, int i ->
            taskService.remove(task.account.admin, task)
            log.error("Removing task num " + i)
        }

        // Find all removed forms and remove them through service
        def forms = Form.findAll()
        forms.each {form ->
            // Mark Form for removal if its template has been removed
            if (form.submittedData.template.isRemoved) {
                formService.remove(form.account.admin, form)
            }
        }
    }

    def publicLeads() {
        FindIterable fi = LeadM.find(eq("visibility", Visibility.PRIVATE.name()))
        fi.each {LeadM it ->
            it.visibility = Visibility.PUBLIC
            it.save(flush: true, failOnError: true)
        }
    }

    def fixSubmitDates() {
        LocSubmission.findAll().each {
            it.submitDate = Constants.Date.IST(it.submitDate)
            it.save(flush: true, failOnError: true)
        }
    }

    def reportService
    def refreshLocReport() {
        LocReport.findAll().each {report ->
            reportService.refreshLocReport(report)
        }
    }
}
