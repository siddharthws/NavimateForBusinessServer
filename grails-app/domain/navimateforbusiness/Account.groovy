package navimateforbusiness

class Account {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    // Company Name
    String          name

    // List of users
    User admin
    static hasMany = [
            managers: User,
            reps: User
    ]

    static constraints = {
        admin nullable: true
    }

    static mapping = {
    }
}
