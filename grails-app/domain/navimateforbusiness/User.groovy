package navimateforbusiness

class User {

    String name
    String phoneNumber
    String password
    String email
    navimateforbusiness.Status status = navimateforbusiness.Status.ACTIVE
    String fcmId

    Date dateCreated
    Date lastModified

    navimateforbusiness.Role role

    static belongsTo = [
            account: Account
    ]

    static constraints = {
        email nullable: true
        fcmId nullable: true
    }

    static mapping = {
        table 'nvm_user'
    }
}
