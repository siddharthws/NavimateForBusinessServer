package navimateforbusiness

class Account {

    String name
    User admin

    static hasMany = [
            managers: User
    ]

    Date dateCreated
    Date lastModified

    static constraints = {

    }

    static mapping = {
    }
}
