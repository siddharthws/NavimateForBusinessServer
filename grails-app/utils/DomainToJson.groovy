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
                formTemplateId:     task.template.id,
                period:             task.period,
                status:             task.status.name()
        ]
    }

    static def Form(Form form) {
        return [
                id:                 form.id,
                ver:                form.version,
                name:               form.name,
                data:               JSON.parse(form.data)
        ]
    }
}
