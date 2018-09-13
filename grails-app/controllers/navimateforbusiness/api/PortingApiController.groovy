package navimateforbusiness.api

import com.mongodb.client.FindIterable
import navimateforbusiness.Account
import navimateforbusiness.Data
import navimateforbusiness.Field
import navimateforbusiness.Form
import navimateforbusiness.LeadM
import grails.plugins.rest.client.RestBuilder
import navimateforbusiness.LocReport
import navimateforbusiness.LocSubmission
import navimateforbusiness.enums.Role
import navimateforbusiness.Task
import navimateforbusiness.FormM
import navimateforbusiness.TaskM

import grails.converters.JSON
import navimateforbusiness.User
import navimateforbusiness.Template
import navimateforbusiness.enums.TaskStatus
import navimateforbusiness.enums.Visibility
import navimateforbusiness.util.Constants
import navimateforbusiness.util.ApiException
import org.grails.web.json.JSONObject

import static com.mongodb.client.model.Filters.eq

class PortingApiController {

    def leadService
    def templateService
    def taskService
    def formService
    def reportService
    def fieldService
    def authService
    def workflowService

    def portZbcWorkflow() {
        // Get all ZBC forms
        Account acc = Account.findById(131927)
        def forms = formService.getAllForUserByFilter(acc.admin, [:])
        forms.each {FormM form ->
            workflowService.execZbcFormSubmissionWf(User.findById(form.ownerId), form)
        }
    }

    def fixAxisAccount() {
        // Update Account Name
        def acc = Account.findById(146071)
        acc.name = "Axis Infoline"
        acc.save(flush: true, failOnError: true)

        // Change accounts of managers registered for above account
        def man1 = User.findById(146088)
        def man2 = User.findById(146089)
        def man3 = User.findById(146090)
        def man4 = User.findById(146091)
        man1.account = acc
        man2.account = acc
        man3.account = acc
        man4.account = acc
        man1.save(flush: true, failOnError: true)
        man2.save(flush: true, failOnError: true)
        man3.save(flush: true, failOnError: true)
        man4.save(flush: true, failOnError: true)

        // Change emails of duplicate accounts
        def dup1 = User.findById(146092)
        def dup2 = User.findById(146093)
        dup1.email = "utr1@axis.com"
        dup2.email = "utr2@axis.com"
        dup1.save(flush: true, failOnError: true)
        dup2.save(flush: true, failOnError: true)
    }

    def fixProductField() {
        // Get all product fields
        def fields = Field.findAllByType(Constants.Template.FIELD_TYPE_PRODUCT)
        fields.each {def field ->
            switch (field.template.type) {
                case Constants.Template.TYPE_TASK:
                    // Iterate through all tasks
                    TaskM.findAll().each { TaskM it ->
                        if (it["$field.id"]) {
                            def valueJson = new JSONObject(it["$field.id"])
                            if (valueJson.id) {
                                it["$field.id"] = valueJson.id
                            } else {
                                it["$field.id"] = null
                            }

                            it.save(flush: true, failOnError: true)
                        }
                    }
                    break

                case Constants.Template.TYPE_LEAD:
                    // Iterate through all tasks
                    LeadM.findAll().each { LeadM it ->
                        if (it["$field.id"]) {
                            def valueJson = new JSONObject(it["$field.id"])
                            if (valueJson.id) {
                                it["$field.id"] = valueJson.id
                            } else {
                                it["$field.id"] = null
                            }

                            it.save(flush: true, failOnError: true)
                        }
                    }
                    break
            }
        }
    }

    def taskPublicId() {
        Task.findAll().each {
            it.publicId = String.valueOf(it.id)
            it.save(flush: true, failOnError: true)
        }
    }

    def mongoObjects() {
        // Convert tasks
        def tasks = Task.findAll()
        tasks.eachWithIndex {Task it, int i ->
            log.error("performing task conversion for " + i + " out of " + tasks.size())

            // Get mongo lead for this task
            LeadM lead = leadService.getForUserByFilter(it.account.admin, [ids: [it.leadid], includeRemoved: true])
            if (!lead) {throw new ApiException("Lead with id = " + it.leadid + " not found in task " + it.id)}

            TaskM task = new TaskM(
                    accountId:          it.account.id,
                    dateCreated:        it.dateCreated,
                    lastUpdated:        it.lastUpdated,
                    oldId:              it.id,
                    extId:              it.extId,
                    publicId:           it.publicId ?: String.valueOf(it.id),
                    isRemoved:          it.isRemoved,
                    creatorId:          it.creator.id,
                    managerId:          it.manager.id,
                    repId:              it.rep ? it.rep.id : null,
                    lead:               lead,
                    status:             it.status,
                    period:             it.period,
                    resolutionTimeHrs:  it.resolutionTimeHrs,
                    templateId:         it.templateData.template.id,
                    formTemplateId:     it.formTemplate.id
            )

            // Add field values
            it.templateData.values.each {value ->
                if (value.value) {
                    task[String.valueOf(value.fieldId)] = fieldService.parseValue(value.field, value.value)
                } else {
                    task[String.valueOf(value.fieldId)] = 0
                }
            }

            // Save task
            task.save(failOnError: true, flush: true)
        }

        // Convert Forms
        def forms = Form.findAll()
        forms.eachWithIndex {Form it, int i ->
            log.error("performing form conversion for " + i + " out of " + forms.size())

            // Find task with old ID
            TaskM task = null
            if (it.task) {
                task = TaskM.find(eq("oldId", it.task.id))[0]
                if (!task) {throw new ApiException("Task with oldId = " + it.task.id + " not found")}
            }

            FormM form = new FormM(
                    accountId:      it.account.id,
                    dateCreated:    it.dateCreated,
                    lastUpdated:    it.lastUpdated,
                    oldId:          it.id,
                    isRemoved:      it.isRemoved,
                    ownerId:        it.owner.id,
                    task:           task,
                    latitude:       it.latitude,
                    longitude:      it.longitude,
                    taskStatus:     it.taskStatus,
                    templateId:     it.submittedData.template.id
            )

            // Add distance info
            form.distanceKm = formService.getDistance(it.account.admin, form)

            // Add field values
            it.submittedData.values.each {value ->
                if (value.value) {
                    form[String.valueOf(value.fieldId)] = fieldService.parseValue(value.field, value.value)
                } else {
                    form[String.valueOf(value.fieldId)] = 0
                }
            }

            // Save task
            form.save(failOnError: true, flush: true)
        }

        // return response
        def resp = [success: true]
        render resp as JSON
    }

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

    def fixNumberField() {
        // Find All number fields
        def fields = Field.findAllByType(Constants.Template.FIELD_TYPE_NUMBER)
        LeadM.findAll().each {LeadM lead ->
            boolean bSave = false

            // Check for number fields
            fields.each {Field field ->
                if (lead["$field.id"] != null) {
                    lead["$field.id"] = Double.parseDouble(lead["$field.id"])
                    bSave = true
                }
            }

            // Save lead
            if (bSave) {
                lead.save(flush: true, failOnError: true)
            }
        }
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

    def refreshLocReport() {
        LocReport.findAll().each {report ->
            reportService.refreshLocReport(report)
        }
    }

    def taskResolveTime() {
        def tasks = Task.findAll()
        tasks.each {Task task ->
            // Get all forms submitted for this task
            def forms = Form.findAllByTask(task)

            // Get oldest form with status CLOSED
            def closeForms = forms.findAll {it.taskStatus == TaskStatus.CLOSED}
            if (closeForms) {
                closeForms = closeForms.sort {it.dateCreated}
                def closeTimeMs = closeForms[0].dateCreated.time

                // Calculate resolution time and save task
                def elapsedTimeMs = closeTimeMs - task.dateCreated.time
                double elapsedTimeHrs = (double) elapsedTimeMs / (double) (1000 * 60 * 60)
                task.resolutionTimeHrs = Constants.round(elapsedTimeHrs, 2)
            } else {
                task.resolutionTimeHrs = -1
            }
            task.lastUpdated = new Date()
            task.save(flush: true, failOnError: true)
        }
    }

    def isRemoveUserFix() {
        // Remove all forms whose reps are rmeoved
        Form.findAll().each {Form form ->
            if (!form.owner.account || form.owner.account.id != form.account.id) {
                formService.remove(form.account.admin, form)
            }
        }

        // Remove all takss whose reps are removed
        Task.findAll().each {Task task ->
            if (task.rep && (!task.rep.account || task.rep.account.id != task.account.id)) {
                taskService.remove(task.account.admin, task)
            }
        }
    }

    def createProductTemplates() {
        Account.findAll().each { Account acc ->
            // Create product template
            def template = authService.createDefaultProductTemplate(acc.admin)

            // Save template
            template.save(flush: true, failOnError: true)
        }
    }
}
