package navimateforbusiness

import grails.converters.JSON

class Form {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    String name
    String data

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
