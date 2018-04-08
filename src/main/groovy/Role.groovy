package navimateforbusiness

enum Role {
    REP(1), MANAGER(2), CC(3), ADMIN(4), NVM_ADMIN(5)

    //Role Related Constants
    public final int value

    //Constructor To Initialise Values Of Enum Elements
    Role(int value) {
        this.value = value
    }
}