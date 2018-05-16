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
    User creator
    User rep
    Lead lead
    String leadid

    // Task properties
    navimateforbusiness.TaskStatus status = navimateforbusiness.TaskStatus.OPEN
    int period = 0

    // Templated data for task information
    Data templateData

    // Form template to be used
    Template formTemplate

    static mappedBy = []

    static constraints = {
        rep             nullable: true
        extId           nullable: true
        leadid          nullable: true
        lead            nullable: true
    }

    static mapping = {
        autoTimestamp false
    }
}
