package navimateforbusiness.api

import grails.converters.JSON
import navimateforbusiness.Acra

class AppApiController {
    static final int MIN_APP_VERSION_CODE       = 28
    static final int CURRENT_APP_VERSION_CODE   = 28

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

        // Set individual params
        acra.stacktrace = acraJson.STACK_TRACE
        acra.versionName = acraJson.BUILD_CONFIG.VERSION_NAME
        acra.appId = acraJson.CUSTOM_DATA?.id
        acra.phone = acraJson.PHONE_MODEL

        // Save ACRA Object
        acra.save(failOnError: true, flush: true)

        //Send succes
        def resp = [success: true]
        render resp as JSON
    }

    def getAcra() {
        def acras = Acra.findAll ()

        def resp = [count: acras.size(), acras: []]
        acras.each {Acra acra ->
            resp.acras.push([
                versionName : acra.versionName,
                phone       : acra.phone,
                appId       : acra.appId,
                stacktrace  : acra.stacktrace
            ])
        }
        render resp as JSON
    }

    // Remove all ACRA entries
    def clearAcra() {
        Acra.findAll().each {it ->
            it.delete(flush: true)
        }
    }
}
