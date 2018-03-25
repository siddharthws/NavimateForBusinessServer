package navimateforbusiness

class User {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    // External ID for API access
    String extId

    // Contact Information
    String name
    String phone
    String countryCode
    String password
    String email
    String fcmId

    // Account Information
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
        phone           nullable: true
        countryCode     nullable: true
        fcmId           nullable: true
        manager         nullable: true
        extId           nullable: true
    }

    static mapping = {
        table 'nvm_user'            // table name 'user' is not allowed
    }
}
