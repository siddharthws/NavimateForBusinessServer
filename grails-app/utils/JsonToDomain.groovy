/**
 * Created by Siddharth on 05-12-2017.
 */
package navimateforbusiness

class JsonToDomain {
    static Lead Lead(def leadJson, User owner) {
        navimateforbusiness.Lead lead = null
        // Check for existing objects
        if (leadJson.id) {
            lead = Lead.findByIdAndManager(leadJson.id, owner)

            // Throw exception if invalid id / owner
            if (!lead) {
                throw new navimateforbusiness.ApiException("Invalid Lead Id for this user",
                        navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
            }
        } else {
            // Create new lead
            lead = new Lead(manager: owner,
                    account: owner.account,
                    isRemoved: false)
        }

        // Update lead info from JSON received
        lead.latitude = leadJson.latitude
        lead.longitude = leadJson.longitude
        lead.address = leadJson.address
        lead.title = leadJson.title

        Template template = Template.findById(leadJson.templateId)
        Data data = Data(leadJson.templateData, owner, template)
        lead.templateData = data

        lead
    }

    static Form Form(def formJson, User owner) {
        navimateforbusiness.Form form = null
        // Check for existing objects
        if (formJson.id) {
            form = Form.findByIdAndOwner(formJson.id, owner)

            // Throw exception if invalid id / owner
            if (!form) {
                throw new navimateforbusiness.ApiException("Invalid Form Id for this user",
                        navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
            }
        } else {
            Task task = Task.findById(formJson.taskId)
            double lat = formJson.latitude
            double lng = formJson.longitude
            Date date = new Date(formJson.timestamp)
            Template template = Template.findById(formJson.data.templateId)
            Data data = Data(formJson.data, owner, template)

            // Get Taks status form close task boolean
            navimateforbusiness.TaskStatus submitStatus = formJson.closeTask ? navimateforbusiness.TaskStatus.CLOSED : navimateforbusiness.TaskStatus.OPEN

            // Create form object
            form = new Form(owner: owner,
                            account: owner.account,
                            task: task,
                            latitude: lat,
                            longitude: lng,
                            submittedData: data,
                            taskStatus: submitStatus)
            form.dateCreated = date
            form.lastUpdated = date
        }

        form
    }

    static Template Template(def templateJson, User owner) {
        if (!templateJson) {
            throw new navimateforbusiness.ApiException( "No template data found !!!",
                    navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }

        // Get template object
        navimateforbusiness.Template template = null
        if (templateJson.id) {
            template = navimateforbusiness.Template.findByIdAndOwner(templateJson.id, owner)

            // Throw exception if invalid id / owner
            if (!template) {
                throw new navimateforbusiness.ApiException("Invalid Template Id for this user",
                        navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
            }
        } else {
            template = new Template(owner: owner, account: owner.account, type: templateJson.type)
        }

        // Assign Name
        template.name = templateJson.name

        // Get Fields Array
        List<Field> fields = []
        templateJson.fields.each { fieldJson ->
            fields.push(Field(fieldJson, template, owner.account))
        }
        template.fields = fields

        // Populate Data Object
        template.defaultData = Data(templateJson.defaultData, owner, template)

        template
    }

    static Field Field (def fieldJson, Template template, Account account) {
        if (!fieldJson || !fieldJson.type || !fieldJson.title) {
            throw new navimateforbusiness.ApiException( "Invalid Field in template !!!",
                    navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }

        // Get Field from Id
        Field field
        if (fieldJson.id) {
            field = Field.findById(fieldJson.id)
        } else {
            field = new Field()
        }

        // Assign properties
        field.title = fieldJson.title
        field.type = fieldJson.type
        field.template = template
        field.account = account

        field
    }

    static Data Data(def dataJson, User owner, Template template) {
        if (!dataJson) {
            throw new navimateforbusiness.ApiException( "No data found !!!",
                    navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }

        navimateforbusiness.Data data = null
        // Check for existing objects
        if (dataJson.id) {
            data = Data.findByIdAndOwner(dataJson.id, owner)

            // Throw exception if invalid id / owner
            if (!data) {
                throw new navimateforbusiness.ApiException("Invalid Data Id for this user",
                            navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
            }
        } else {
            data = new Data(owner: owner, account: owner.account)
            data.template = template
        }

        // Assign values from json
        List<Value> values = []
        if (dataJson.values) {
            dataJson.values.eachWithIndex { valueJson, i ->
                Field field = null
                if (valueJson.fieldId) {
                    field = Field.findById(valueJson.fieldId)
                } else {
                    field = data.template.fields.getAt(i)
                }
                values.add(Value(valueJson, data, field))
            }
        }
        data.values = values

        data
    }

    static Value Value(def valueJson, Data data, Field field) {
        navimateforbusiness.Value value = null

        // Check for existing objects
        if (valueJson.id) {
            value = Value.findById(valueJson.id)

            // Throw exception if invalid id / owner
            if (!value) {
                throw new   navimateforbusiness.ApiException("Invalid Value Id for this user",
                            navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
            }
        } else {
            value = new Value(account: data.account, data: data, field: field)
        }

        // Assign Value from json
        if (valueJson.value != null) {
            value.value = valueJson.value
        }

        return value
    }
}
