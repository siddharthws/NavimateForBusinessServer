package navimateforbusiness

class User {

    // Timestamp
    Date dateCreated
    Date lastUpdated

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
            superior: User
    ]

    // Data
    static hasMany = [
            subordinates:   User,
            leads:          Lead,
            tasks:          Task,
            forms:          Form,
            visits:         Visit
    ]

    static constraints = {
        password        nullable: true
        email           nullable: true
        fcmId           nullable: true
        account         nullable: true
        subordinates    nullable: true
        leads           nullable: true
        tasks           nullable: true
        forms           nullable: true
        visits          nullable: true
    }

    static mapping = {
        table 'nvm_user'            // table name 'user' is not allowed
    }
}
