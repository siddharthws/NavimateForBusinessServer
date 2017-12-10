package navimateforbusiness

import grails.converters.JSON

/**
 * Created by Siddharth on 05-12-2017.
 */
class DomainToJson {

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
                description:    lead.description,
                phoneNumber:    lead.phone,
                email:          lead.email,
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

    static def Form(Form form) {
        return [
                id:                 form.id,
                ver:                form.version,
                name:               form.name
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
