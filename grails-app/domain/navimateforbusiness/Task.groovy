package navimateforbusiness

class Task {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    // External ID for API access
    String extId

    //Remove Flags
    boolean isRemoved = false

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

    // Templated data for task information
    Data templateData

    // Form template to be used
    Template formTemplate

    // Data
    static hasMany = [
            forms:      Form,
            visits:     Visit
    ]

    static mappedBy = [
            manager:            'createdTasks',
            rep:                'assignedTasks'
    ]

    static constraints = {
        manager         nullable: true
        rep             nullable: true
        lead            nullable: true
        extId           nullable: true
        templateData    nullable: true
    }

    static mapping = {
        period defaultValue: 0
    }
}
