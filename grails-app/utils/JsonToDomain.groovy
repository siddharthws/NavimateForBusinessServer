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
            Task task = formJson.taskId ? Task.findById(formJson.taskId) : null
            double lat = formJson.latitude
            double lng = formJson.longitude
            Date date = new Date(formJson.timestamp)
            Template template = Template.findById(formJson.data.templateId)
            Data data = Data(formJson.data, owner, template)

            // Get Taks status form close task boolean
            navimateforbusiness.TaskStatus submitStatus = null
            if (task!= null) {
                submitStatus = formJson.closeTask ? navimateforbusiness.TaskStatus.CLOSED : navimateforbusiness.TaskStatus.OPEN
            }

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
