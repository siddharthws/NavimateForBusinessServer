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
}

