package navimateforbusiness

class Field {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    //Remove Flags
    boolean isRemoved = false

    static belongsTo = [
            account:    Account,
            template:   Template
    ]

    // Field Properties
    int type
    String title
    boolean bMandatory

    static constraints = {
    }

    static mapping = {
    }
}
