package navimateforbusiness

class User {

    String name
    String phoneNumber
    String password
    String email
    navimateforbusiness.Status status = navimateforbusiness.Status.ACTIVE
    String fcmId
    navimateforbusiness.Role role

    Date dateCreated
    Date lastUpdated

    static belongsTo = [
            account: Account
    ]

    static constraints = {
        email nullable: true
        fcmId nullable: true
        account nullable: true
    }

    static mapping = {
        table 'nvm_user'
    }
}
