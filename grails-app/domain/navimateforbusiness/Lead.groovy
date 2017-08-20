package navimateforbusiness

class Lead {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    String companyName
    String contactName
    String contactPhone
    double latitude
    double longitude

    static belongsTo = [
            manager: User
    ]

    static constraints = {
    }

    static mapping = {
    }
}
