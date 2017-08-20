package navimateforbusiness

import grails.converters.JSON

class Form {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    String name
    JSON   dataJson

    static belongsTo = [
            user: User,
            task: Task
    ]

    static constraints = {
    }

    static mapping = {
    }
}
