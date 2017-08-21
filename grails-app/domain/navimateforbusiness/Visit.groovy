package navimateforbusiness

class Visit {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    static belongsTo = [
            account:    Account,
            rep:        User,
            task:       Task
    ]

    static constraints = {
    }

    static mapping = {
    }
}
