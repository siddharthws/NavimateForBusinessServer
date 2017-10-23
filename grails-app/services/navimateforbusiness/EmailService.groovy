package navimateforbusiness

import grails.gorm.transactions.Transactional

@Transactional
class EmailService {

    def sendMail(String address, String sub, String message) {
        // Send mail
        sendMail {
            to address
            subject sub
            text message
        }
    }
}
