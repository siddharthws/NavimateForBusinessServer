/**
 * Created by Siddharth on 05-12-2017.
 */
package navimateforbusiness

class JsonToDomain {
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
            Data data = Data(formJson.data, owner)
            form = new Form(owner: owner,
                            account: owner.account,
                            task: task,
                            dateCreated: date,
                            lastUpdated: date,
                            latitude: lat,
                            longitude: lng,
                            submittedData: data)
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
            template = new Template(owner: owner, account: owner.account, name: templateJson.name)
        }

        // Populate Field Objects
        if (!templateJson.fields || !templateJson.fields.size()) {
            throw new navimateforbusiness.ApiException( "No fields found in template !!!",
                    navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }
        def fields = []
        templateJson.fields.each { fieldJson ->
            fields.push(Field(fieldJson, template, owner.account))
        }
        template.fields = fields

        // Populate Data Object
        if (!templateJson.defaultData || !templateJson.defaultData.size()) {
            throw new navimateforbusiness.ApiException( "No data found in template !!!",
                    navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }
        Data data = Data(templateJson.defaultData, owner)
        if (!data) {
            return null
        }
        template.defaultData = data

        // Assign other properties
        template.account = owner.account
        template.owner = owner
        template.name = templateJson.name

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

    static Data Data(def dataJson, User owner) {
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
        }

        // Assign Template from json
        if (dataJson.templateId) {
            Template template = Template.findByIdAndAccount(dataJson.templateId, owner.account)
            if (template) {
                data.template = template
            }
        }

        // Assign values from json
        List<Value> values = []
        if (dataJson.values) {
            dataJson.values.each { valueJson ->
                values.add(Value(valueJson, data))
            }
        }
        data.values = values

        data
    }

    static Value Value(def valueJson, Data data) {
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
            // Check Valid Field
            if (valueJson.fieldId) {
                Field field = Field.findById(valueJson.fieldId)
                if (field) {
                    value = new Value(account: data.account, data: data, field: field)
                } else {
                    throw new   navimateforbusiness.ApiException("Field not found for new value",
                            navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
                }
            } else {
                throw new   navimateforbusiness.ApiException("No fieldId for new value",
                        navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
            }
        }

        // Assign Value from json
        if (valueJson.value != null) {
            value.value = valueJson.value
        }

        return value
    }
}
