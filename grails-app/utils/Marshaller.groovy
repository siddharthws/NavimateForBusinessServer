package navimateforbusiness

import navimateforbusiness.User

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
                company:        lead.company,
                name:           lead.name,
                phoneNumber:    lead.phone,
                email:          lead.email,
                address:        lead.address,
                latitude:       lead.latitude,
                longitude:      lead.longitude
        ]
    }

    static def serializeForm(Form form) {
        return [
                id:     form.id,
                name:   form.name,
                data:   form.data
        ]
    }
}

