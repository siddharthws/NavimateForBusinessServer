package navimateforbusiness

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
        // Status
        public static final int STATUS_UNAVAILABLE  = 0
        public static final int STATUS_WAITING      = 1
        public static final int STATUS_AVAILABLE    = 2

        // Maximum time to wait before marking rep as UNAVAILBALE
        public static final int MAX_UPDATE_WAIT_TIME_S = 60
    }

    // Template Related Constants
    class Template {
        // Template Types
        public static final int TYPE_FORM                 = 1

        // Field types
        public static final int FIELD_TYPE_NONE           = 0
        public static final int FIELD_TYPE_TEXT           = 1
        public static final int FIELD_TYPE_NUMBER         = 2
        public static final int FIELD_TYPE_RADIOLIST      = 3
        public static final int FIELD_TYPE_CHECKLIST      = 4
        public static final int FIELD_TYPE_PHOTO          = 5
        public static final int FIELD_TYPE_SIGN           = 6
        public static final int FIELD_TYPE_LOCATION       = 7
    }

    // Filter related constants
    class Filter {
        // Filter types
        public static final int TYPE_NONE           = 0
        public static final int TYPE_SELECTION      = 1
        public static final int TYPE_TEXT           = 2
        public static final int TYPE_NUMBER         = 3
        public static final int TYPE_DATE           = 4
    }
}
