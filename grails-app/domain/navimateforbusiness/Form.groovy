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

    // Deprecated. To be removed in later versions
    String name
    String data

    static mappedBy = [
            task: 'forms'
    ]

    static constraints = {
        owner nullable: true
        task nullable: true
        submittedData nullable: true
        name nullable: true
        data nullable: true
    }

    static mapping = {
        autoTimestamp false
    }
}
