package navimateforbusiness

class Lead {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    // Contact Info
    String title
    String description
    String phone
    String email

    // Location
    double latitude
    double longitude
    String address

    static belongsTo = [
            account: Account,
            manager: User
    ]

    static constraints = {
        manager         nullable: true
        title           nullable: true
        email           nullable: true
        latitude        nullable: true
        longitude       nullable: true
    }

    static mapping = {
    }
}
