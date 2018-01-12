package navimateforbusiness

class User {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    // External ID for API access
    String extId

    // Contact Information
    String name
    String phoneNumber
    String password
    String email
    String fcmId

    // Account Information
    navimateforbusiness.UserStatus status
    navimateforbusiness.Role role
    static belongsTo = [
            account: Account,
            manager: User
    ]

    // Data
    static hasMany = [
            reps:           User,
            leads:          Lead,
            createdTasks:   Task,
            assignedTasks:  Task,
            forms:          Form,
            visits:         Visit
    ]

    static constraints = {
        account         nullable: true
        password        nullable: true
        email           nullable: true
        phoneNumber     nullable: true
        fcmId           nullable: true
        manager         nullable: true
        extId           nullable: true
    }

    static mapping = {
        table 'nvm_user'            // table name 'user' is not allowed
    }
}
