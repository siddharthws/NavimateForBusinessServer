package navimateforbusiness

import navimateforbusiness.util.Constants

class Tracking {
    static mapWith = "mongo"

    // Timestamp
    Date dateCreated
    Date lastUpdated

    Long accountId
    Long repId

    // Tracking Status
    int status = Constants.Tracking.ERROR_OFFLINE

    // Last tracking update for this rep
    Double lat = 0
    Double lng = 0
    Date locUpdateTime = new Date(0)

    // User speed
    float speed = 0.0f

    static constraints = {
    }

    static mapping = {
        accountId index:true
    }
}
