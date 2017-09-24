package navimateforbusiness

import grails.gorm.transactions.Transactional
import grails.plugins.rest.client.RestBuilder

@Transactional
class FcmService {

    def notifyApp (fcmId) {
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
                log.error("Error while sending FCM : " + resp.json)
            }
        }
    }
}
