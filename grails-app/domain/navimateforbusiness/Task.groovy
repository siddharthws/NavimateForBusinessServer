package navimateforbusiness

import navimateforbusiness.enums.TaskStatus

class Task {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    // External ID for API access
    String extId

    // Public ID given by user
    String publicId

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

    // Task resolution time in hours
    double resolutionTimeHrs = -1

    // Task properties
    TaskStatus status = TaskStatus.OPEN
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
        publicId        nullable: true
    }

    static mapping = {
        autoTimestamp false
    }
}
