package navimateforbusiness

class Lead {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    // External ID for API access
    String extId

    // Remove Flag
    boolean isRemoved = false

    // Lead Name
    String title

    // Location
    double latitude
    double longitude
    String address

    // Templated Data
    Data templateData

    static belongsTo = [
            account: Account,
            manager: User
    ]

    static constraints = {
        extId           nullable: true
    }

    static mapping = {
    }
}
