package navimateforbusiness

import grails.gorm.transactions.Transactional

@Transactional
class UserService {

    /* ------------------------------------ Settings ------------------------------------ */
    // API to update account settings
    def updateAccountSettings(User user, def settingsJson) {
        // Validate JSON
        if (!settingsJson.startHr || !settingsJson.endHr) {
            throw new navimateforbusiness.ApiException("Invalid input", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }

        // Validate hours
        if (((settingsJson.startHr < 0) || (settingsJson.startHr > 23)) ||
            ((settingsJson.endHr < 0) || (settingsJson.endHr > 23))) {
            throw new navimateforbusiness.ApiException("Invalid time of day", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }

        // Update account settings for this user
        def accSettings = AccountSettings.findByAccount(user.account)
        accSettings.startHr = settingsJson.startHr
        accSettings.endHr = settingsJson.endHr
        accSettings.save(failOnError: true, flush: true)
    }

    /* ------------------------------------ End -------------------------------------- */
}
