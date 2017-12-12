package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.Data
import navimateforbusiness.Field
import navimateforbusiness.Form
import navimateforbusiness.Task
import navimateforbusiness.Template
import navimateforbusiness.User
import navimateforbusiness.Value
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

class PortingController {

    def port1() {
        portFormTemplates()

        def resp = [success: true]
        render resp as JSON
    }

    def port2() {
        removeAdminForms()

        def resp = [success: true]
        render resp as JSON
    }

    def portFormTemplates () {
        // Get list of admins and reps
        List<User> admins = User.findAllByRole(navimateforbusiness.Role.ADMIN)
        List<User> reps = User.findAllByRole(navimateforbusiness.Role.REP)

        // Get all old templates submitted by managers
        List<Form> oldTemplates = []
        admins.each {admin ->
            oldTemplates.addAll(Form.findAllByOwner(admin))
        }
        log.error(oldTemplates.size() + " old templates present for " + admins.size() + " no. of admins")

        // Get all old form submissions submitted by rep
        List<Form> oldSubmissions = []
        reps.each {rep ->
            oldSubmissions.addAll(Form.findAllByOwner(rep))
        }
        log.error(oldSubmissions.size() + " old submissions present for " + reps.size() + " no. of reps")

        // Create template for each old template
        oldTemplates.each {oldTemplate ->
            portTemplateToNewInfra(oldTemplate)
        }
        log.error(Template.findAll().size() + " templates created for " + admins.size() + " no. of admins")

        // Create data object for each old form submission
        oldSubmissions.each {oldSubmission ->
            portSubmissionToNewInfra(oldSubmission)
        }
        log.error(Data.findAll().size() + " entries created for " + reps.size() + " no. of reps")

        // Remove user mappings of all old templates
        oldTemplates.each {oldTemplate ->
            // Remove from user forms
            User owner = oldTemplate.owner
            owner.removeFromForms(oldTemplate)
            owner.save(failOnError: true, flush: true)
        }

        // Update Checklist and radio list objects
        List<Value> values = Value.findAll()
        values.each {value ->
            // Update Checklist field
            if ((value.field.type == navimateforbusiness.Constants.Template.FIELD_TYPE_CHECKLIST) && (value.value.length())) {
                def valueJson = JSON.parse(value.value)

                // Convert to options array
                JSONArray newValueJson = new JSONArray()
                valueJson.options.eachWithIndex { option, i ->
                    newValueJson.push(new JSONObject([
                            name: option,
                            selection: valueJson.selection[i]
                    ]))
                }

                value.value = newValueJson.toString()
            } else if ((value.field.type == navimateforbusiness.Constants.Template.FIELD_TYPE_RADIOLIST) && (value.value.length())) {
                JSONObject valueJson = JSON.parse(value.value)

                // Convert selection to index
                int selectionIdx = 0
                valueJson.options.eachWithIndex { option, i ->
                    if (option == valueJson.selection) {
                        selectionIdx = i
                    }
                }

                valueJson.selection = selectionIdx
                value.value = valueJson.toString()
            }

            // Save Value
            value.save(failOnError: true, flush: true)
        }
    }

    def removeAdminForms() {
        List<User> admins = User.findAllByRole(navimateforbusiness.Role.ADMIN)

        // Get all old templates submitted by managers
        List<Form> oldTemplates = []
        admins.each {admin ->
            oldTemplates.addAll(Form.findAllByOwner(admin))
        }

        // Remove all old templates form table
        oldTemplates.each {oldTemplate ->
            // Delete old Template
            oldTemplate.delete(flush: true, failOnError: true)
        }
    }

    def portTemplateToNewInfra(Form oldTemplate) {
        // Create Template object from form object
        Template newTemplate = getTemplateFromForm(oldTemplate)

        // Save template
        Data defaultData = newTemplate.defaultData
        newTemplate.save(failOnError: true, flush: true)
        newTemplate.defaultData = defaultData
        newTemplate.save(failOnError: true, flush: true)

        // Update task references of form template
        List<Task> tasks = Task.findAllByTemplate(oldTemplate)
        tasks.each {task ->
            task.formTemplate = newTemplate
            task.template = null
            task.save(failOnError: true, flush: true)
        }
    }

    def portSubmissionToNewInfra(Form oldSubmission) {
        // Create Data Object from form
        Data data = getDataFromForm(oldSubmission, oldSubmission.task.formTemplate)

        if (data.values.size()) {
            // Save Data Object
            data.save(failOnError: true, flush: true)

            // Update old submissions data
            oldSubmission.submittedData = data
            oldSubmission.save(failOnError: true, flush: false)
        } else {
            // Delete old form
            log.error("Deleting form with ID : " + oldSubmission.id)
            oldSubmission.delete(flush: true, failOnError: true)
        }

    }

    def getTemplateFromForm(Form form) {
        Template template = new Template()

        template.name = form.name
        template.type = navimateforbusiness.Constants.Template.TYPE_FORM
        template.owner = form.owner
        template.account = form.account
        template.fields = getFieldsFromString(form.data, template)
        template.defaultData = getDataFromForm(form, template)

        template
    }

    def getDataFromForm(Form form, Template template) {
        Data data = new Data()

        data.account = form.owner.account
        data.owner = form.owner
        data.template = template

        // Parse to list of values
        def dataJson = JSON.parse(form.data)
        dataJson.eachWithIndex {fieldJson, i ->
            // Get field type and title
            String title = fieldJson.title
            int type = getTypeFromString(fieldJson.type)

            // Find most suitable field in this template
            Field bestField = null
            for (int fieldIdx = 0; fieldIdx < template.fields.size(); fieldIdx++) {
                Field field = template.fields[fieldIdx]
                if ((field.type == type) && (field.title == title)) {
                    bestField = field
                    break
                } else if (field.type == type) {
                    bestField = field
                }
            }

            // Port data only if best field found
            if (!bestField) {
                log.error("Field not found for data : " + fieldJson + " Template : " + form.id + " belonging to admin " + template.owner.name)
            } else {
                data.addToValues(
                        new Value(
                                account:     form.owner.account,
                                field:       template.fields[i],
                                data:        data,
                                value:       String.valueOf(fieldJson.value)))
            }

        }

        data
    }

    def getFieldsFromString(String data, Template template) {
        List<Field> fields = []

        // Parse to Json
        def dataJson = JSON.parse(data)
        dataJson.each {fieldJson ->
            int type = getTypeFromString(fieldJson.type)

            // Create field object
            fields.push(new Field(
                    type:           type,
                    title:          fieldJson.title,
                    bMandatory:     false,
                    account:        template.account,
                    template:       template
            ))
        }

        fields
    }

    def getTypeFromString(String name) {
        if (name.equals('text')) {
            return navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT
        } else if (name.equals('number')) {
            return navimateforbusiness.Constants.Template.FIELD_TYPE_NUMBER
        } else if (name.equals('checkList')) {
            return navimateforbusiness.Constants.Template.FIELD_TYPE_CHECKLIST
        } else if (name.equals('radioList')) {
            return navimateforbusiness.Constants.Template.FIELD_TYPE_RADIOLIST
        } else if (name.equals('photo')) {
            return navimateforbusiness.Constants.Template.FIELD_TYPE_PHOTO
        } else if (name.equals('signature')) {
            return navimateforbusiness.Constants.Template.FIELD_TYPE_SIGN
        }

        return navimateforbusiness.Constants.Template.FIELD_TYPE_NONE
    }
}
