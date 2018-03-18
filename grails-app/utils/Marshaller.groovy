package navimateforbusiness

import grails.converters.JSON

class Marshaller {

    static def serializeUser(User user) {
        return [
                id: user.id,
                name: user.name,
                phoneNumber: "+" + user.countryCode + " " + user.phone,
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
                cId:        task.account.id + String.format("%08d", task.id),
                lead:       task.lead.title,
                rep:        task.rep.name,
                period:     task.period,
                status:     task.status.name()
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

