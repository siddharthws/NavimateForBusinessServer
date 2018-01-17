package navimateforbusiness

class Data {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    static belongsTo = [
            account:    Account,
            owner:      User
    ]

    // Template used for data submission
    Template template

    static hasMany = [
            values:    Value
    ]

    static mappedBy = [
    ]

    static constraints = {
        template nullable: true
    }

    static mapping = {
    }
}
