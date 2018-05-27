package navimateforbusiness

import navimateforbusiness.enums.Visibility

class Lead {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    // Visibility of Lead
    Visibility visibility

    // External ID for API access
    String extId

    // Remove Flag
    boolean isRemoved = false

    // Lead Name
    String name

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
