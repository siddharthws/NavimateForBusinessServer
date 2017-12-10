package navimateforbusiness

class Value {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    static belongsTo = [
            account:    Account,
            field:      Field,
            data:       Data
    ]

    // Actual value stored as String
    String value

    static constraints = {
    }

    static mapping = {
    }
}
