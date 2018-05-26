package navimateforbusiness.objects

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

    LatLng(String string) {
        // Get LatLng Array
        def latlng = string.split(',')

        // Extract into Lat and Lng
        this.lat = latlng[0] ? Double.valueOf(latlng[0]) : 0
        this.lng = latlng[1] ? Double.valueOf(latlng[1]) : 0
    }

    @Override
    String toString() {
        return String.valueOf(lat) + "," + String.valueOf(lng)
    }

    @Override
    boolean equals(def that) {
        return this.lat == that.lat && this.lng == that.lng
    }

    boolean isValid() {
        return (lat || lng)
    }
}
