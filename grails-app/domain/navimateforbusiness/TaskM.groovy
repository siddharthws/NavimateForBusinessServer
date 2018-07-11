package navimateforbusiness

import navimateforbusiness.enums.TaskStatus

class TaskM {
    static mapWith = "mongo"

    Long accountId

    // Timestamp
    Date dateCreated
    Date lastUpdated

    // TODO : Remove : ID from old task database
    Long oldId

    // Task ID
    String id

    // Publicly visible Task ID
    String publicId

    // External ID for API access
    String extId

    //Remove Flags
    boolean isRemoved = false

    // Task properties
    Long creatorId
    Long managerId
    Long repId
    LeadM lead

    TaskStatus status = TaskStatus.OPEN
    int period = 0

    // Task resolution time in hours
    double resolutionTimeHrs = -1

    // Task template to be used
    Long templateId

    // Form template to be used
    Long formTemplateId

    static constraints = {
        repId           nullable: true
        extId           nullable: true
        oldId           nullable: true
    }

    static mapping = {
        // index by accoutn and template
        accountId     index:true
        templateId    index:true

        // Disable Auto timestamping
        autoTimestamp false
    }
}
