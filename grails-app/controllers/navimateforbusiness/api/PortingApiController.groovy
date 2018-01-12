package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.Account
import navimateforbusiness.ApiKey

class PortingApiController {

    def authService

    def index() { }

    def portApiKey() {
        // Get all Accounts
        def accounts = Account.findAll()
        def resp = []

        // Iterate through accounts
        accounts.each {account ->
            // Create Api Key Object for this account
            ApiKey apiKey = new ApiKey(key: authService.generateApiKey(), account: account)
            apiKey.save(flush: true, failOnError: true)

            // Feed to response
            resp += [accId: account.id, key: apiKey.key]
        }

        // Return success response
        render resp as JSON
    }
}
