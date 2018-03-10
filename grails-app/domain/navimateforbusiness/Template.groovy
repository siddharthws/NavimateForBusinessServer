package navimateforbusiness

class Template {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    //Remove Flags
    boolean isRemoved = false

    static belongsTo = [
            account:    Account,
            owner:      User
    ]

    // Name and type of template
    int type
    String name

    static hasMany = [
            fields:             Field
    ]

    static constraints = {
    }

    static mapping = {
    }
}
