package navimateforbusiness

import grails.gorm.transactions.Transactional

@Transactional
class UserService {
    /* ------------------------------------ Dependencies ------------------------------------ */
    /* ------------------------------------ Getter APIs ------------------------------------ */
    // Method to get all reps for a user
    def getRepsForUser(User user) {
        List<User> reps

        switch (user.role) {
            case navimateforbusiness.Role.ADMIN:
                // Get all reps of company
                reps = User.findAllByAccountAndRole(user.account, navimateforbusiness.Role.REP)
                break
            case navimateforbusiness.Role.MANAGER:
                // Get all reps under this manager
                reps = User.findAllByAccountAndRoleAndManager(user.account, navimateforbusiness.Role.REP, user)
                break
        }

        // Sort reps in increasing order of name
        reps.sort{it.name}

        // Return tasks
        reps
    }

    // Method to get all reps for a user
    def getRepForUserById(User user, Long id) {
        // Get all reps for user
        List<User> reps = getRepsForUser(user)

        // Find rep with this id
        User rep = reps.find {it -> it.id == id}

        rep
    }

    /* ------------------------------------ Public APIs ------------------------------------ */
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

    /* ------------------------------------ Private APIs ------------------------------------ */
}
