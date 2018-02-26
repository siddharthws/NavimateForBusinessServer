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
    }

    // Filter related constants
    class Filter {
        // Filter types
        public static final int TYPE_NONE           = 0
        public static final int TYPE_SELECTION      = 1
        public static final int TYPE_TEXT           = 2
        public static final int TYPE_NUMBER         = 3
        public static final int TYPE_DATE           = 4

        // Sort Types
        public static final int SORT_NONE          = 0
        public static final int SORT_ASC           = 1
        public static final int SORT_DESC          = 2
    }
}
