package navimateforbusiness

import grails.converters.JSON
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONException
import org.grails.web.json.JSONObject

/**
 * Created by Siddharth on 05-12-2017.
 */
class DomainToJson {

    // Methods to validate JSON Strings
    static boolean isJsonValid(String string) {
        isJsonObjectValid(string) || isJsonArrayValid(string)
    }

    static boolean isJsonObjectValid(String string) {
        try {
            new JSONObject(string)
        } catch (JSONException ex1) {
            return false
        }

        return true
    }

    static boolean isJsonArrayValid(String string) {
        try {
            new JSONArray(string)
        } catch (JSONException ex1) {
            return false
        }

        return true
    }

    static def User(User user) {
        return [
                id:             user.id,
                ver:            user.version,
                name:           user.name,
                phoneNumber:    user.phoneNumber,
                email:          user.email,
                role:           user.role
        ]
    }

    static def Lead(Lead lead) {
        return [
                id:             lead.id,
                ver:            lead.version,
                title:          lead.title,
                templateId:     lead.templateData.template.id,
                dataId:         lead.templateData.id,
                address:        lead.address,
                latitude:       lead.latitude,
                longitude:      lead.longitude
        ]
    }

    static def Task(Task task) {
        return [
                id:                 task.id,
                ver:                task.version,
                managerId:          task.manager.id,
                repId:              task.rep.id,
                leadId:             task.lead.id,
                formTemplateId:     task.formTemplate.id,
                period:             task.period,
                status:             task.status.name()
        ]
    }

    static def Template(Template template) {
        // Create Field Ids array
        def fieldIds = []
        template.fields.each {field ->
            fieldIds.push(field.id)
        }

        return [
                id:                 template.id,
                ver:                template.version,
                name:               template.name,
                defaultDataId:      template.defaultData.id,
                fieldIds:           fieldIds
        ]
    }

    static def Field(Field field) {
        return [
                id:                 field.id,
                ver:                field.version,
                title:              field.title,
                type:               field.type,
                isMandatory:        field.bMandatory
        ]
    }

    static def Data(Data data) {
        // Create value Ids array
        def valueIds = []
        data.values.each {value ->
            valueIds.push(value.id)
        }
        valueIds.sort(true)

        return [
                id:                 data.id,
                ver:                data.version,
                valueIds:           valueIds
        ]
    }

    static def Value(Value value) {
        return [
                id:                 value.id,
                ver:                value.version,
                value:              value.value,
                fieldId:            value.field.id
        ]
    }
}
