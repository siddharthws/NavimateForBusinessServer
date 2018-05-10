package navimateforbusiness

/**
 * Created by Siddharth on 17-09-2017.
 */
class LatLng {
    double lat = 0
    double lng = 0

    LatLng() {}

    LatLng(double lat, double lng) {
        this.lat = lat
        this.lng = lng
    }

    @Override
    String toString() {
        return String.valueOf(lat) + "," + String.valueOf(lng)
    }

    String fromString(String source) {
        // Get LatLng Array
        double[] latlng = source.split(',')

        // Extract into Lat and Lng
        this.lat = latlng[0] ?: 0
        this.lng = latlng[1] ?: 0
    }
}
