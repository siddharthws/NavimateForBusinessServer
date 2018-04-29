package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.Acra

class AppApiController {
    static final int MIN_APP_VERSION_CODE = 23

    def checkForUpdate() {
        int appVersionCode = request.JSON.versionCode
        if (!appVersionCode) {
            appVersionCode = 0
        }

        def resp = [updateRequired: (appVersionCode < MIN_APP_VERSION_CODE)]
        render resp as JSON
    }

    def acra() {
        // Get input ACRA JSON
        def acraJson = request.JSON

        // Save JSON in database
        Acra acra = new Acra(acraData: acraJson.toString())
        acra.save(failOnError: true, flush: true)

        //Send succes
        def resp = [success: true]
        render resp as JSON
    }
}
