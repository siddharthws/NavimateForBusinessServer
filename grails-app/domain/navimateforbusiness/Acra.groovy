package navimateforbusiness

class Acra {

    // Timestamp
    Date dateCreated
    Date lastUpdated

    static belongsTo = [
    ]

    String versionName
    String phone
    String appId
    String stacktrace
    String acraData

    static constraints = {
        versionName nullable: true
        appId nullable: true
        stacktrace nullable: true
        phone nullable: true
    }

    static mapping = {
    }
}
