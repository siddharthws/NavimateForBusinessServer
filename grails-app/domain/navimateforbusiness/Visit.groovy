package navimateforbusiness

class Visit {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    static belongsTo = [
            user: User,
            task: Task
    ]

    static constraints = {
    }

    static mapping = {
    }
}
