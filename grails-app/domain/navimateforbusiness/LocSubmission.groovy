package navimateforbusiness

class LocSubmission {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    static belongsTo = [
            account:    Account,
            report:     LocReport
    ]

    // Date / Time of submission
    Date submitDate

    // User Information
    int status
    String latlngString
    float speed
    int battery

    // Mapping to index in roads array
    int roadsIdx

    static constraints = {
        roadsIdx nullable: true
    }

    static mapping = {
    }
}
