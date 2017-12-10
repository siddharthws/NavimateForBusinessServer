package navimateforbusiness

class Template {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    static belongsTo = [
            account:    Account,
            owner:      User
    ]

    // Name and type of template
    int type
    String name

    // Default Data of the template
    Data defaultData

    static hasMany = [
            fields:             Field
    ]

    static constraints = {
        defaultData nullable: true
    }

    static mapping = {
    }
}
