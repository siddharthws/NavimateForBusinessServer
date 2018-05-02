package navimateforbusiness.api

import grails.plugins.rest.client.RestBuilder
import navimateforbusiness.Role
import navimateforbusiness.Task

import grails.converters.JSON
import navimateforbusiness.User

class PortingApiController {

    def fixManagers() {
        // Iterate through all tasks
        Task.findAll().eachWithIndex { Task task, int i ->
            if (task.rep && task.account == task.rep.account && task.manager != task.rep.manager) {
                task.manager = task.rep.manager
                task.save(flush: true, failOnError: true)
                log.error(task.id + " : " + task.manager.name + " : " + task.rep.manager.name)
            }
        }

        def resp = [success: true]
        render resp as JSON
    }

    def fixFcms() {
        // Send ignorable notification to all reps to check if FCM is working
        def reps = User.findAllByRole(Role.REP)
        reps.eachWithIndex { User rep, int i ->
            if (rep.fcmId) {
                notifyApp(rep)
                sleep(100)
                log.error("Sent notification to rep " + i + " : " + reps.size())
            }
        }
    }


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
                        type = 10
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
}
