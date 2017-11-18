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
    int period
    static hasMany = [
            forms:      Form,
            visits:     Visit
    ]

    static mappedBy = [
            manager:            'createdTasks',
            rep:                'assignedTasks'
    ]

    static constraints = {
        manager nullable: true
        rep nullable: true
        lead nullable: true
    }

    static mapping = {
        period defaultValue: 0
    }
}
