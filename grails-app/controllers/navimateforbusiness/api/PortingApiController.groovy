package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.Account
import navimateforbusiness.AccountSettings

class PortingApiController {

    def accountSettings() {
        // Iterate through all accounts
        def accounts = Account.findAll()
        accounts.each {account ->
            // Create and save settings object for this account
            AccountSettings accSettings = new AccountSettings(  account: account,
                                                                startHr: 10,
                                                                endHr: 18)
            accSettings.save(flush: true, failOnError: true)
        }

        // Send response
        def resp = [success: true]
        render resp as JSON
    }
}
