package navimateforbusiness

enum Role {
    REP(1), MANAGER(2), ADMIN(3), NVM_ADMIN(4)

    //Role Related Constants
    public final int value

    //Constructor To Initialise Values Of Enum Elements
    Role(int value) {
        this.value = value
    }
}