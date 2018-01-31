package navimateforbusiness

class AccountSettings {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    static belongsTo = [
            account:    Account
    ]

    int startHr, endHr

    static constraints = {
    }

    static mapping = {
    }
}
