package navimateforbusiness

class Lead {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    // External ID for API access
    String extId

    // Lead Name
    String title

    // Location
    double latitude
    double longitude
    String address

    // Templated Data
    Data templateData

    // Deprecated
    String description
    String phone
    String email

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
        extId           nullable: true
        templateData    nullable: true
    }

    static mapping = {
    }
}
