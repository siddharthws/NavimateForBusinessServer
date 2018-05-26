package navimateforbusiness

class LocReport {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    static belongsTo = [
            account:    Account,
            owner:      User
    ]

    // Date of submission of report (does not include time)
    Date submitDate

    // Encoded polyline for getting roads information
    String encPolyline = ""

    // Total distance travelled in report
    long distance = 0

    static constraints = {
        encPolyline nullable: true
    }

    static mapping = {
    }
}
