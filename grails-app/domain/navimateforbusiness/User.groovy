package navimateforbusiness

import navimateforbusiness.enums.Role

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
    String about

    // Account Information
    Role role
    static belongsTo = [
            account: Account,
            manager: User
    ]

    // Data
    static hasMany = [
            reps:           User
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
        about           nullable: true
    }

    static mapping = {
        table 'nvm_user'            // table name 'user' is not allowed
    }
}
