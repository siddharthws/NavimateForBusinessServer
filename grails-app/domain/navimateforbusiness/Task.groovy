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
    Lead lead

    // Task properties
    navimateforbusiness.TaskStatus status = TaskStatus.OPEN
    int period

    // Form template to be used
    Template formTemplate

    // Data
    static hasMany = [
            forms:      Form,
            visits:     Visit
    ]

    // Deprecated. To be removed
    Form template

    static mappedBy = [
            manager:            'createdTasks',
            rep:                'assignedTasks'
    ]

    static constraints = {
        manager nullable: true
        rep nullable: true
        lead nullable: true
        formTemplate nullable: true
        template nullable: true
    }

    static mapping = {
        period defaultValue: 0
    }
}
