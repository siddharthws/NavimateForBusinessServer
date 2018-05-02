package navimateforbusiness

import grails.gorm.transactions.Transactional
import grails.plugins.rest.client.RestBuilder

@Transactional
class FcmService {
    /*------------------------ Dependencies --------------------*/
    /*------------------------ Public methods --------------------*/
    def notifyUsers (List<User> users, int type) {
        // Remove duped and invalid users from the list
        def usersActual = []
        users.each {it ->
            if (it?.fcmId && !usersActual.contains(it)) {
                usersActual.push(it)
            }
        }

        // notify each user
        usersActual.each {User it -> notifyUser(it, type)}
    }

    def notifyUser (User user, int type) {
        String fcmId = user?.fcmId
        if (fcmId) {
            if (notify(fcmId, type)) {
                return true
            } else {
                // Reset user's FCM ID since it is invalid
                user.fcmId = ""
                user.save(flush: true, failOnError: true)
            }
        }

        return false
    }

    /*------------------------ Private methods --------------------*/
    private def notify (String fcmId, int notifType) {
        def bSent = false

        if (fcmId) {
            // Send request to FCM server
            def request = new RestBuilder(connectTimeout:1000, readTimeout:20000)
            def resp = request.post("https://fcm.googleapis.com/fcm/send") {
                header 'Content-Type', 'application/json'
                header 'Authorization', 'key=AAAA2Ezvs_Y:APA91bFgkZh16BJHk0IZlkwXQAAJb6HwNC4svq0Q8knmkiyXTZOR9xGZ-QARpKUIDuuIJEvZq9UMfOPfYnUtpawLCr9Vrju32mk8dBYatBu3Rp7mSeuDb3pKfUr9ZOc6dTTdckYmd7hq'
                json {
                    data = {
                        type = notifType
                    }
                    to = fcmId
                }
            }

            // Validate response
            if (!resp.json.success) {
                log.error("Error while sending FCM : " + resp.json + " to FCM ID = " + fcmId)
            } else {
                bSent = true
            }
        }

        bSent
    }
}
