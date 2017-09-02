package navimateforbusiness

class Task {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    // Associated Users
    static belongsTo = [
            account:    Account,
            manager:    User
    ]
    User rep

    // Data
    Lead lead
    navimateforbusiness.TaskStatus status = TaskStatus.OPEN
    Form template
    static hasMany = [
            forms:      Form,
            visits:     Visit
    ]

    static mappedBy = [
            manager:            'createdTasks',
            rep:                'assignedTasks'
    ]

    static constraints = {
    }

    static mapping = {
    }
}
