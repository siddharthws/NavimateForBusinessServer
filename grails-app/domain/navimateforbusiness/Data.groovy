package navimateforbusiness

class Data {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    static belongsTo = [
            account:    Account,
            owner:      User,
            template:   Template
    ]

    static hasMany = [
            values:    Value
    ]

    static mappedBy = [
            template:            'none'
    ]

    static constraints = {
    }

    static mapping = {
    }
}
