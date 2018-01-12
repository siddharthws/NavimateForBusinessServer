package navimateforbusiness

class ApiKey {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    static belongsTo = [
            account:    Account
    ]

    String key

    static constraints = {
    }

    static mapping = {
    }
}
