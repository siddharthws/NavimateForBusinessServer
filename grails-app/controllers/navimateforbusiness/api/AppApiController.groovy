package navimateforbusiness.api

import grails.converters.JSON

class AppApiController {
    static final int MIN_APP_VERSION_CODE = 13

    def checkForUpdate() {
        int appVersionCode = request.JSON.versionCode
        if (!appVersionCode) {
            appVersionCode = 0
        }

        def resp = [updateRequired: (appVersionCode < MIN_APP_VERSION_CODE)]
        render resp as JSON
    }
}
