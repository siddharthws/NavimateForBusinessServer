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
            case navimateforbusiness.Role.CC:
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

    // Method to get all managers under a user
    List<User> getManagersForUser(User user) {
        List<User> managers = User.findAllByAccountAndRole(user.account, navimateforbusiness.Role.MANAGER)

        // Sort managers in increasing order of name
        managers.sort{it.name}

        managers
    }

    // Method to get all reps for a user
    def getRepForUserById(User user, Long id) {
        // Get all reps for user
        List<User> reps = getRepsForUser(user)

        // Find rep with this id
        User rep = reps.find {it -> it.id == id}

        rep
    }

    // Method to get all reps for a user by name
    def getRepForUserByName(User user, String name) {
        // Get all reps for user
        List<User> reps = getRepsForUser(user)

        // Find rep with this id
        User rep = reps.find {it -> it.name == name}

        rep
    }

    // Method to get manager under a user by ID
    def getManagerForUserById(User user, Long id) {
        // Get all manager for user
        List<User> managers = getManagersForUser(user)

        // Find manager with this id
        User manager = managers.find {it -> it.id == id}

        manager
    }

    // Method to get manager under a user by name
    def getManagerForUserByName(User user, String name) {
        // Get all manager for user
        List<User> managers = getManagersForUser(user)

        // Find manager with this id
        User manager = managers.find {it -> it.name == name}

        manager
    }

    /* ------------------------------------ Converter APIs ------------------------------------ */
    // User Object to / from JSON
    def toJson(User user) {
        return [
            id:             user.id,
            name:           user.name,
            role:           user.role.value,
            phone:          user.phone,
            countryCode:    user.countryCode,
            email:          user.email,
            about:          user.about,
            manager:        user.manager ? [name: user.manager.name, id: user.manager.id] :
                                           [name: user.account.admin.name, id: user.account.admin.id]
        ]
    }

    def repFromJson(def json, User user) {
        User rep

        // Update Country Code to default
        json.countryCode = json.countryCode ?: "91"

        // Get rep by phone number
        rep = User.findByCountryCodeAndPhone(json.countryCode, json.phone)

        // Ensure rep is present in this user's account
        if (rep && rep.account && rep.account != user.account) {
            throw new navimateforbusiness.ApiException("User with phone number " + json.phone + " belongs to another account..." )
        }

        // Get user by ID
        if (!rep && json.id) {
            rep = getRepForUserById(user, json.id)
            if (!rep) {
                throw new navimateforbusiness.ApiException("Invalid user id requested", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
            }
        }

        // Get manager to be assigned to task
        User manager
        if (!json.managerId || json.managerId == user.id) {
            manager = user
        } else {
            manager = getManagerForUserById(user, json.managerId)
        }

        if (!manager) {
            throw new navimateforbusiness.ApiException("Invalid manager assigned", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }

        // Create new rep if required
        if (!rep) {
            rep = new User(role: navimateforbusiness.Role.REP)
        }

        // Update params using JSON
        rep.account = user.account
        rep.manager = manager
        rep.name = json.name
        rep.phone = json.phone
        rep.about = json.about
        rep.countryCode = json.countryCode

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
