package navimateforbusiness.enums

/**
 * Created by Siddharth on 25-01-2018.
 */
enum Visibility {
    PUBLIC(1), PRIVATE(2)

    //Role Related Constants
    public final int value

    //Constructor To Initialise Values Of Enum Elements
    Visibility(int value) {
        this.value = value
    }
}