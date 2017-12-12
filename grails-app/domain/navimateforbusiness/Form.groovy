package navimateforbusiness

class Form {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    static belongsTo = [
            account:    Account,
            owner:      User,
            task:       Task
    ]

    // Location of form submission
    double latitude     = 0
    double longitude    = 0
    String address      = ""

    // Form Submission Data
    Data submittedData

    static mappedBy = [
            task: 'forms'
    ]

    static constraints = {
    }

    static mapping = {
        autoTimestamp false
    }
}
