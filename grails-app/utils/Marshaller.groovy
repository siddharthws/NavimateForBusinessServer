package navimateforbusiness

import grails.converters.JSON

class Marshaller {

    static def serializeUser(User user) {
        return [
                id: user.id,
                name: user.name,
                phoneNumber: user.phoneNumber,
                email: user.email
        ]
    }

    static def serializeLead(Lead lead){
        return [
                id:             lead.id,
                title:          lead.title,
                description:    lead.description,
                phoneNumber:    lead.phone,
                email:          lead.email,
                address:        lead.address,
                latitude:       lead.latitude,
                longitude:      lead.longitude
        ]
    }

    static def serializeTask(Task task) {
        return [
                id:         task.id,
                lead:       task.lead.title,
                rep:        task.rep.name,
                period:     task.period,
                status:     task.status.name()
        ]
    }

    static def serializeTaskForRep(Task task) {
        return [
                id:         task.id,
                lead:       serializeLead(task.lead),
                template:   serializeForm(task.template)
        ]
    }

    static def serializeForm(Form form) {
        return [
                id:     form.id,
                name:   form.name,
                data:   JSON.parse(form.data)
        ]
    }

    static def serializeTrackObj(navimateforbusiness.TrackingObject trackObj) {
        long currentTime = System.currentTimeMillis()
        int lastUpdated = 0
        if (currentTime > trackObj.lastUpdated) {
            lastUpdated = (currentTime - trackObj.lastUpdated) / 1000
        }

        return [
                name:           trackObj.rep.name,
                id:             trackObj.rep.id,
                latitude:       trackObj.position.latitude,
                longitude:      trackObj.position.longitude,
                lastUpdated:    lastUpdated,
                speed:          trackObj.speed,
                status:         trackObj.status
        ]
    }
}

