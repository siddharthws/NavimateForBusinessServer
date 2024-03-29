package navimateforbusiness.util

import navimateforbusiness.objects.LatLng
import org.apache.poi.ss.usermodel.DataFormatter
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

import java.text.SimpleDateFormat

/**
 * Created by Siddharth on 29-08-2017.
 */
class Constants {

    // Http Status Codes
    class HttpCodes{
        // 4xx
        public static final int BAD_REQUEST                     = 400
        public static final int UNAUTHORIZED                    = 401
        public static final int CONFLICT                        = 409

        // 5xx
        public static final int INTERNAL_SERVER_ERROR           = 500
    }

    // Tracking related constants
    class Tracking {
        // Error Codes
        public static final int ERROR_NONE              = 0
        public static final int ERROR_IDLE              = 1
        public static final int ERROR_WAITING           = 2
        public static final int ERROR_NO_UPDATES        = 3
        public static final int ERROR_NO_GPS            = 4
        public static final int ERROR_NO_PERMISSION     = 5
        public static final int ERROR_OFFLINE           = 6
    }

    // Template Related Constants
    class Template {
        // Template Types
        public static final int TYPE_FORM                 = 1
        public static final int TYPE_LEAD                 = 2
        public static final int TYPE_TASK                 = 3
        public static final int TYPE_PRODUCT              = 4

        // Field types
        public static final int FIELD_TYPE_NONE           = 0
        public static final int FIELD_TYPE_TEXT           = 1
        public static final int FIELD_TYPE_NUMBER         = 2
        public static final int FIELD_TYPE_RADIOLIST      = 3
        public static final int FIELD_TYPE_CHECKLIST      = 4
        public static final int FIELD_TYPE_PHOTO          = 5
        public static final int FIELD_TYPE_SIGN           = 6
        public static final int FIELD_TYPE_LOCATION       = 7
        public static final int FIELD_TYPE_CHECKBOX       = 8
        public static final int FIELD_TYPE_DATE           = 9
        public static final int FIELD_TYPE_FILE           = 10
        public static final int FIELD_TYPE_PRODUCT        = 11
        public static final int FIELD_TYPE_INVOICE        = 12

        // Special Object Column type
        public static final int FIELD_TYPE_LEAD           = 21
        public static final int FIELD_TYPE_TASK           = 22

        public static final int FIELD_TYPE_TEMPLATE       = 41
        public static final int FIELD_TYPE_REP            = 42
        public static final int FIELD_TYPE_NON_REP        = 43
    }

    // Filter related constants
    class Filter {
        // Filter types
        public static final int TYPE_NONE           = 0
        public static final int TYPE_SELECTION      = 1
        public static final int TYPE_TEXT           = 2
        public static final int TYPE_NUMBER         = 3
        public static final int TYPE_DATE           = 4
        public static final int TYPE_OBJECT         = 5

        // Sort Types
        public static final int SORT_NONE          = 0
        public static final int SORT_ASC           = 1
        public static final int SORT_DESC          = -1
    }

    // Table related constants
    class Table {
        // Maximum number of rows that can be selected at once
        public static final int MAX_SELECTION_COUNT = 500

        // Multiselect types
        public static final int MS_INCLUDE = 1
        public static final int MS_EXCLUDE = 2
    }

    static class Date {
        // Date Formats
        public static final String FORMAT_LONG       = "yyyy-MM-dd HH:mm:ss"
        public static final String FORMAT_DATE_ONLY  = "yyyy-MM-dd"
        public static final String FORMAT_TIME_ONLY  = "HH:mm:ss"
        public static final String FORMAT_TIME_SHORT = "h:mm a"
        public static final String FORMAT_UTC        = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

        // Timezones
        public static final TimeZone TIMEZONE_IST = TimeZone.getTimeZone('Asia/Calcutta')

        // Method to convert to IST
        static java.util.Date IST (java.util.Date date) {
            Calendar cal = Calendar.getInstance()
            cal.setTime(date)
            cal.setTimeInMillis(cal.getTimeInMillis() - cal.timeZone.getRawOffset() + TIMEZONE_IST.getRawOffset())
            return cal.time
        }
    }

    class Formatters {
        public static final SimpleDateFormat LONG = new SimpleDateFormat(Constants.Date.FORMAT_LONG)
        public static final SimpleDateFormat DATE = new SimpleDateFormat(Constants.Date.FORMAT_DATE_ONLY)
        public static final SimpleDateFormat TIME = new SimpleDateFormat(Constants.Date.FORMAT_TIME_ONLY)
        public static final SimpleDateFormat TIME_SHORT = new SimpleDateFormat(Constants.Date.FORMAT_TIME_SHORT)

        // Data Formatter for Excel import
        public static final DataFormatter DATA = new DataFormatter()
    }

    class Notifications {
        // Types of notifications
        public static final int TYPE_TASK_UPDATE        = 1
        public static final int TYPE_TEMPLATE_UPDATE    = 2
        public static final int TYPE_LEAD_UPDATE        = 3
        public static final int TYPE_ACCOUNT_ADDED      = 4
        public static final int TYPE_ACCOUNT_REMOVED    = 5
    }

    // Distance / LatLng related methods
    static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0)
    }

    static double rad2deg(double rad) {
        return (rad * 180 / Math.PI)
    }

    // Method to calculate distance between coordinates
    static long getLatlngListDistance(List<LatLng> latlngs) {
        long distanceM = 0

        for (int i = 0; i < latlngs.size() - 1; i++) {
            distanceM += getDistanceBetweenCoordinates(latlngs[i], latlngs[i + 1])
        }

        distanceM
    }

    static long getDistanceBetweenCoordinates(LatLng point1, LatLng point2)
    {
        double lat1 = point1.lat
        double lat2 = point2.lat
        double lon1 = point1.lng
        double lon2 = point2.lng

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        int distanceM = (int) (dist * 1609.344);

        return distanceM;
    }

    static double round(double value, int places) {
        double factor = Math.pow(10, places)
        ((double) Math.round(value * factor)) / factor
    }

    /**
     * Decodes an encoded path string into a sequence of LatLngs.
     */
    static def decodePolyline(String encodedPath) {
        int len = encodedPath.length();

        // For speed we preallocate to an upper bound on the final length, then
        // truncate the array before returning.
        def path = []
        int index = 0;
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int result = 1;
            int shift = 0;
            int b;
            while ({
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
                b >= 0x1f
            }());
            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            result = 1;
            shift = 0;
            while ({
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
                b >= 0x1f
            }());
            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            path.add(new LatLng(lat * 1e-5, lng * 1e-5));
        }

        return path;
    }

    /**
     * Encodes a sequence of LatLngs into an encoded path string.
     */
    static def encodePolyline(List<LatLng> path) {
        long lastLat = 0;
        long lastLng = 0;

        final StringBuffer result = new StringBuffer();

        for (def point : path) {
            long lat = Math.round(point.lat * 1e5);
            long lng = Math.round(point.lng * 1e5);

            long dLat = lat - lastLat;
            long dLng = lng - lastLng;

            encode(dLat, result);
            encode(dLng, result);

            lastLat = lat;
            lastLng = lng;
        }
        return result.toString();
    }

    static void encode(long v, StringBuffer result) {
        v = v < 0 ? ~(v << 1) : v << 1;
        while (v >= 0x20) {
            result.append(Character.toChars((int) ((0x20 | (v & 0x1f)) + 63)));
            v >>= 5;
        }
        result.append(Character.toChars((int) (v + 63)));
    }

    static def getISODate(java.util.Date date) {
        // Get date in UTC Format
        SimpleDateFormat utcFormatter = new SimpleDateFormat(Date.FORMAT_UTC)
        utcFormatter.setTimeZone(TimeZone.getTimeZone("UTC"))
        def dateUtc = utcFormatter.format(date)

        // Parse into ISO Format
        DateTimeFormatter parser = ISODateTimeFormat.dateTime()
        DateTime parsedDateTime = parser.parseDateTime(dateUtc)

        // Return parsed date
        parsedDateTime.toDate()
    }
}
