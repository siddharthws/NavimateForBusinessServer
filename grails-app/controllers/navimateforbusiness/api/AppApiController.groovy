package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.Acra

class AppApiController {
    static final int MIN_APP_VERSION_CODE       = 24
    static final int CURRENT_APP_VERSION_CODE   = 24

    def checkForUpdate() {
        int appVersionCode = request.JSON.versionCode
        if (!appVersionCode) {
            appVersionCode = 0
        }

        boolean bMandatoryUpdate = (appVersionCode < MIN_APP_VERSION_CODE)
        boolean bOptionalUpdate = (appVersionCode < CURRENT_APP_VERSION_CODE)

        def resp = [updateRequired: bOptionalUpdate || bMandatoryUpdate,
                    mandatoryUpdate: bMandatoryUpdate]
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
