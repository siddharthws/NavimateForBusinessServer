package navimateforbusiness

class Account {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    // Company Name
    String          name
    String          photoName

    // List of users
    User admin
    static hasMany = [
            managers: User,
            reps: User
    ]

    static constraints = {
        admin nullable: true
        photoName nullable: true
    }

    static mapping = {
    }
}
