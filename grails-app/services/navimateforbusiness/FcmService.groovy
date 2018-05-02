package navimateforbusiness

import grails.gorm.transactions.Transactional
import grails.plugins.rest.client.RestBuilder

@Transactional
class FcmService {

    def notifyApp (User user) {
        def bSent = false

        String fcmId = user?.fcmId
        if (fcmId) {
            // Send request to FCM server
            def request = new RestBuilder(connectTimeout:1000, readTimeout:20000)
            def resp = request.post("https://fcm.googleapis.com/fcm/send") {
                header 'Content-Type', 'application/json'
                header 'Authorization', 'key=AAAA2Ezvs_Y:APA91bFgkZh16BJHk0IZlkwXQAAJb6HwNC4svq0Q8knmkiyXTZOR9xGZ-QARpKUIDuuIJEvZq9UMfOPfYnUtpawLCr9Vrju32mk8dBYatBu3Rp7mSeuDb3pKfUr9ZOc6dTTdckYmd7hq'
                json {
                    data = {
                        type = 1
                    }
                    to = fcmId
                }
            }

            // Validate response
            if (!resp.json.success) {
                log.error("Error while sending FCM : " + resp.json + " Removing FCM ID = " + user.fcmId + " For " + user.id)
                user.fcmId = ""
                user.save(flush: true, failOnError: true)
            } else {
                bSent = true
            }
        }

        bSent
    }

    def trackApp (fcmId) {
        if (fcmId) {
            // Send request to FCM server
            def request = new RestBuilder(connectTimeout:1000, readTimeout:20000)
            def resp = request.post("https://fcm.googleapis.com/fcm/send") {
                header 'Content-Type', 'application/json'
                header 'Authorization', 'key=AAAA2Ezvs_Y:APA91bFgkZh16BJHk0IZlkwXQAAJb6HwNC4svq0Q8knmkiyXTZOR9xGZ-QARpKUIDuuIJEvZq9UMfOPfYnUtpawLCr9Vrju32mk8dBYatBu3Rp7mSeuDb3pKfUr9ZOc6dTTdckYmd7hq'
                json {
                    data = {
                        type = 2
                    }
                    to = fcmId
                }
            }

            // Validate response
            if (!resp.json.success) {
                log.error("Error while sending track FCM : " + resp.json)
            }
        }
    }
}
