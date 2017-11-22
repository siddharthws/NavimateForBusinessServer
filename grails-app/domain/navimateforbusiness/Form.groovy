package navimateforbusiness

import grails.converters.JSON

class Form {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    String name
    String data

    // Location of form submission
    double latitude     = 0
    double longitude    = 0
    String address      = ""

    static belongsTo = [
            account:    Account,
            owner:      User,
            task:       Task
    ]

    static mappedBy = [
            task: 'forms'
    ]

    static constraints = {
        task nullable: true
    }

    static mapping = {
    }
}
