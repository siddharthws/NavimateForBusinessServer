package navimateforbusiness

class Account {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    // Company Name
    String          companyName

    // List of users
    static hasMany = [
            users: User
    ]

    static constraints = {
    }

    static mapping = {
    }
}
