package navimateforbusiness

class Field {

    // Timestamp
    Date dateCreated
    Date lastUpdated

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
