package navimateforbusiness

import navimateforbusiness.enums.TaskStatus

class FormM {
    static mapWith = "mongo"

    Long accountId

    // Timestamp
    Date dateCreated
    Date lastUpdated

    // Form ID
    String id

    // TODO : Remove : ID from old form database
    Long oldId

    //Remove Flag
    boolean isRemoved = false

    // Form properties
    Long ownerId
    TaskM task

    // Location of form submission
    double latitude     = 0
    double longitude    = 0
    double distanceKm   = -1

    // Task status while submitting this form
    TaskStatus taskStatus

    // Form template used for submission
    Long templateId

    static constraints = {
        task            nullable: true
        taskStatus      nullable: true
        oldId           nullable: true
    }

    static mapping = {
        // index by account and template
        accountId     index:true
        templateId    index:true

        // Disable Auto timestamping
        autoTimestamp false
    }
}
