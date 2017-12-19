package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.Constants
import navimateforbusiness.Data
import navimateforbusiness.Field
import navimateforbusiness.Lead
import navimateforbusiness.Role
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
            List<Lead> leads = Lead.findAllByManager(admin)
            leads.each {lead ->
                // Create Data object to store this lead's data in templated form
                Data data = new Data(account: admin.account, owner: admin)
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
}
