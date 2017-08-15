package navimateforbusiness

class Account {

    String name
    User admin

    static hasMany = [
            managers: User
    ]

    Date dateCreated
    Date lastUpdated

    static constraints = {

    }

    static mapping = {
    }
}
