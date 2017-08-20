package navimateforbusiness

class Task {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    // Associated Users
    static belongsTo = [
            manager: User
    ]
    User rep

    // Data
    Lead lead
    navimateforbusiness.TaskStatus status = TaskStatus.OPEN
    Form formTemplate
    static hasMany = [
            submittedForms:     Form,
            visits:             Visit
    ]

    static constraints = {
        submittedForms  nullable: true
        visits          nullable: true
    }

    static mapping = {
    }
}
