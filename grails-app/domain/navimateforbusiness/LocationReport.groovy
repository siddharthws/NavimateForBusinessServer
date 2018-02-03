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
    long timestamp

    static constraints = {
    }

    static mapping = {
    }
}
