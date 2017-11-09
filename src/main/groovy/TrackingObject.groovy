package navimateforbusiness
/**
 * Created by Siddharth on 28-10-2017.
 */
class TrackingObject {
    User rep
    int status = navimateforbusiness.Constants.Tracking.STATUS_WAITING
    navimateforbusiness.LatLng position = new navimateforbusiness.LatLng()
    long lastUpdated = System.currentTimeMillis()
    float speed = 0.0f
}
