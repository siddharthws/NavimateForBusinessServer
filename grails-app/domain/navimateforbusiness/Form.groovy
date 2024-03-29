package navimateforbusiness

import navimateforbusiness.enums.TaskStatus

class Form {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    // Removal flag
    boolean isRemoved = false

    static belongsTo = [
            account:    Account,
            owner:      User,
            task:       Task
    ]

    // Location of form submission
    double latitude     = 0
    double longitude    = 0
    String address      = ""

    // Form Submission Data
    Data submittedData

    // Task status while submitting this form
    TaskStatus taskStatus

    static mappedBy = []

    static constraints = {
        task        nullable: true
        taskStatus  nullable: true
    }

    static mapping = {
        autoTimestamp false
    }
}
