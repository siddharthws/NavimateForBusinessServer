package navimateforbusiness

class LocationReport {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    static belongsTo = [
            account:    Account,
            owner:      User
    ]

    int status
    double latitude
    double longitude
    Date dateSubmitted

    static constraints = {
    }

    static mapping = {
    }
}
