package navimateforbusiness

import grails.gorm.transactions.Transactional

@Transactional
class EmailService {

    def sendMail(String address, String subject, String message) {
        // Send mail
        sendMail {
            to address
            subject subject
            text message
        }
    }
}
