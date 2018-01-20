package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.Constants
import navimateforbusiness.Data
import navimateforbusiness.Field
import navimateforbusiness.Lead
import navimateforbusiness.Role
import navimateforbusiness.Task
import navimateforbusiness.Template
import navimateforbusiness.User
import navimateforbusiness.Value

class PortingApiController {

    def leadIsRemoved() {
        List<Lead> invalidLeads = Lead.findAllByManager(null)
        log.error("No. of invalid leads = " + invalidLeads.size())

        // Change Data of each lead
        invalidLeads.each {lead ->
            lead.manager = lead.account.admin
            lead.isRemoved = true
            lead.save(flush: true, failOnError: true)
        }

        def resp = [success: true]
        render resp as JSON
    }

    def taskIsRemoved() {
        List<Task> invalidTasks = Task.findAllByManager(null)
        log.error("No. of invalid tasks = " + invalidTasks.size())

        // Change Data of each task
        invalidTasks.each {task ->
            task.manager = task.account.admin
            task.rep = User.findByManagerAndRole(task.account.admin, Role.REP)
            task.lead = Lead.findByAccountAndManager(task.account, task.account.admin)
            task.isRemoved = true
            task.save(flush: true, failOnError: true)
        }

        def resp = [success: true]
        render resp as JSON
    }

    def leadTemplating () {
        // Create a Default Lead Template for every admin
        List<User> admins = User.findAllByRole(Role.ADMIN)
        admins.each {admin ->
            Template template = createLeadTemplate(admin)

            // Save template to DB
            Data defaultData = template.defaultData
            template.save(flush: true, failOnError: true)
            template.defaultData = defaultData
            template.save(flush: true, failOnError: true)

            // Get all leads for this admin
            List<Lead> leads = Lead.findAllByAccount(admin.account)
            leads.each {lead ->
                // Create Data object to store this lead's data in templated form
                Data data = new Data(account: admin.account, owner: lead.manager)
                data.template = template
                for (Field field : template.fields) {
                    if (field.title == "Description") {
                        data.addToValues(new Value(account: admin.account, field: field, value: lead.description))
                    } else if (field.title == "Phone") {
                        data.addToValues(new Value(account: admin.account, field: field, value: lead.phone))
                    } else if (field.title == "Email") {
                        data.addToValues(new Value(account: admin.account, field: field, value: lead.email))
                    }
                }

                // Save Lead's template data
                lead.templateData = data
                lead.save(failOnError: true, flush: true)
            }
            log.error("Lead template create for " + admin.name + " with " + leads.size() + " leads")
        }

        def resp = [success: true]
        render resp as JSON
    }

    def taskTemplating () {
        // Create a Default Lead Template for every admin
        List<User> admins = User.findAllByRole(Role.ADMIN)
        admins.each {admin ->
            Template template = createTaskTemplate(admin)

            // Save template to DB
            Data defaultData = template.defaultData
            template.save(flush: true, failOnError: true)
            template.defaultData = defaultData
            template.save(flush: true, failOnError: true)

            // Get all tasks for this admin's account
            List<Task> tasks = Task.findAllByAccount(admin.account)
            tasks.each {task ->
                // Create Data object to store this task's data in templated form
                Data data = new Data(account: admin.account, owner: task.manager)
                data.template = template
                for (Field field : template.fields) {
                    if (field.title == "Description") {
                        data.addToValues(new Value(account: admin.account, field: field, value: ""))
                    }
                }

                // Save Lead's template data
                task.templateData = data
                task.save(failOnError: true, flush: true)
            }
            log.error("Task template create for " + admin.name + " with " + tasks.size() + " tasks")
        }

        def resp = [success: true]
        render resp as JSON
    }

    Template createLeadTemplate(User admin) {
        // Create a default Lead template
        Template template = new Template(account: admin.account, owner: admin, name: "Default", type: Constants.Template.TYPE_LEAD)

        // Create default data for this template
        Data data = new Data(account: admin.account, owner: admin, template: template)

        // Create Fields for the template
        Field descField     = new Field(account: admin.account, type: Constants.Template.FIELD_TYPE_TEXT, title: "Description", bMandatory: false)
        Field phoneField    = new Field(account: admin.account, type: Constants.Template.FIELD_TYPE_TEXT, title: "Phone", bMandatory: false)
        Field emailField    = new Field(account: admin.account, type: Constants.Template.FIELD_TYPE_TEXT, title: "Email", bMandatory: false)

        // Create Values for the fields
        Value descValue = new Value(account: admin.account, field: descField, value: "")
        Value phoneValue = new Value(account: admin.account, field: phoneField, value: "")
        Value emailValue = new Value(account: admin.account, field: emailField, value: "")

        // Add fields to template
        template.addToFields(descField)
        template.addToFields(phoneField)
        template.addToFields(emailField)

        // Add default data to template
        data.addToValues(descValue)
        data.addToValues(phoneValue)
        data.addToValues(emailValue)
        template.defaultData = data

        template
    }

    Template createTaskTemplate(User admin) {
        // Create a default Task template
        Template template = new Template(account: admin.account, owner: admin, name: "Default", type: Constants.Template.TYPE_TASK)

        // Create default data for this template
        Data data = new Data(account: admin.account, owner: admin, template: template)

        // Create Fields for the template
        Field descField     = new Field(account: admin.account, type: Constants.Template.FIELD_TYPE_TEXT, title: "Description", bMandatory: false)

        // Create Values for the fields
        Value descValue = new Value(account: admin.account, field: descField, value: "")

        // Add fields to template
        template.addToFields(descField)

        // Add default data to template
        data.addToValues(descValue)
        template.defaultData = data

        template
    }

    def dataTemplates() {
        // Get list of data and templates
        List<Data> datas = Data.findAll()
        List<Template> templates = Template.findAll()

        // Iterate through all data objects
        datas.each {data ->
            // Get template used for this data through submitted fields
            Template template = data.values.getAt(0).field.template

            // Assign template to data and save
            data.template = template
            data.save(failOnError: true, flush: true)
        }
    }
}
