package navimateforbusiness

/**
 * Created by Siddharth on 20-08-2017.
 */
enum TaskStatus {
    OPEN(1), CLOSED(2)

    //Role Related Constants
    public final int value

    //Constructor To Initialise Values Of Enum Elements
    TaskStatus(int value) {
        this.value = value
    }
}