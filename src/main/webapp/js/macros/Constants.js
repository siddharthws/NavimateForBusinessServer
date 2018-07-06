
// Constants available throughout app
var Constants = {}

/* ------------------------------- Cookie Info -------------------------------------*/
Constants.Cookie = {
    KEY_EMAIL:      "Navm8Email",
    KEY_PASSWORD:   "Navm8Password"
}

/* ------------------------------- Events -------------------------------------*/
Constants.Events = {
    // Table Module Related events
    TABLE_INIT:             "event_table_init",
    TABLE_EXPORT:           "event_table_export",
    TABLE_TOGGLE_COLUMNS:   "event_table_toggle_columns",
    TABLE_SYNC:             "event_table_sync",
    TABLE_RESET:            "event_table_reset",

    // Template Editor Module related events
    TEMPLATE_VALIDATE:          "event_template_validate",
    TEMPLATE_VALIDATE_SUCCESS:  "event_template_validate_success",

    // Map related events
    MAP_CENTER:             "evt-map-center",
    MAP_MARKER_CLICK:       "evt-marker-click",
    MAP_MARKER_DRAGEND:     "evt-marker-dragend",
    MAP_MARKER_SELECTED:     "evt-marker-selected"
}

/* ------------------------------- Tracking -----------------------------------*/
Constants.Tracking = {
    // Error Codes reported by app while tracking
    ERROR_NONE:             0,
    ERROR_IDLE:             1,
    ERROR_WAITING:          2,
    ERROR_NO_UPDATES:       3,
    ERROR_NO_GPS:           4,
    ERROR_NO_PERMISSION:    5,
    ERROR_OFFLINE:          6,

    STATUS_MESSAGES: [
        "Active",
        "Idle",
        "Waiting for location",
        "Waiting...",
        "GPS is disabled",
        "Location permission denied",
        "Phone is offline / switched off"
    ]
}

/* ------------------------------- Dashboard navigation -----------------------------------*/
Constants.DashboardNav = {
    ITEM_TEAM       : 0,
    ITEM_LEADS      : 1,
    ITEM_TASKS      : 2,
    ITEM_REPORTS    : 3,
    ITEM_TEMPLATES  : 4,
    ITEM_COMPANY    : 5,

    OPTION_MANAGE       : 0,
    OPTION_SUBMISSION   : 1,
    OPTION_FORM         : 2,
    OPTION_LEAD         : 3,
    OPTION_TASK         : 4,
    OPTION_PROFILE      : 5,
    OPTION_SETTINGS     : 6,
    OPTION_MOVEMENT     : 7,
}

/*-----------------------------Dashboard user role-------------------------------------*/
Constants.Role = {
    REP :       1,
    MANAGER:    2,
    CC:         3,
    ADMIN:      4,
    NVM_ADMIN:  5
}

Constants.Role.REGISTRATION = [
    {id: Constants.Role.ADMIN,      name: "Admin"},
    {id: Constants.Role.CC,         name: "Customer Care"},
    {id: Constants.Role.MANAGER,    name: "Manager"}
]

Constants.Visibility = {
    PUBLIC :       1,
    PRIVATE:       2
}

Constants.DashboardNav.Options = [
    {
        id:         Constants.DashboardNav.OPTION_MANAGE,
        name:       'Manage',
        state:      "manage"
    },
    {
        id:         Constants.DashboardNav.OPTION_SUBMISSION,
        name:       'Submission',
        state:      'submission'
    },
    {
        id:         Constants.DashboardNav.OPTION_FORM,
        name:       'Form',
        state:      'form'
    },
    {
        id:         Constants.DashboardNav.OPTION_LEAD,
        name:       'Lead',
        state:      'lead'
    },
    {
        id:         Constants.DashboardNav.OPTION_TASK,
        name:       'Task',
        state:      'task'
    },
    {
        id:         Constants.DashboardNav.OPTION_PROFILE,
        name:       'Profile',
        state:      'profile'
    },
    {
        id:         Constants.DashboardNav.OPTION_SETTINGS,
        name:       'Settings',
        state:      'settings'
    },
    {
        id:         Constants.DashboardNav.OPTION_MOVEMENT,
        name:       'Movement',
        state:      'movement'
    }
]

Constants.DashboardNav.Menu = [
    {
        id:         Constants.DashboardNav.ITEM_TEAM,
        name:       "Team",
        state:      "team",
        accessLevel:Constants.Role.MANAGER,
        options:    [
            Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_MANAGE]
        ]
    },
    {
        id:         Constants.DashboardNav.ITEM_LEADS,
        name:       "Leads",
        state:      "leads",
        accessLevel:Constants.Role.MANAGER,
        options:    [
            Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_MANAGE]
        ]
    },
    {
        id:         Constants.DashboardNav.ITEM_TASKS,
        name:       "Tasks",
        state:      "tasks",
        accessLevel:Constants.Role.MANAGER,
        options:    [
            Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_MANAGE]
        ]
    },
    {
        id:         Constants.DashboardNav.ITEM_REPORTS,
        name:       "Reports",
        state:      "reports",
        accessLevel:Constants.Role.MANAGER,
        options:    [
            Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_SUBMISSION],
            //Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_MOVEMENT]
        ]
    },
    {
        id:         Constants.DashboardNav.ITEM_TEMPLATES,
        name:       "Templates",
        state:      "templates",
        accessLevel:Constants.Role.ADMIN,
        options:    [
            Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_FORM],
            Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_LEAD],
            Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_TASK]
        ]
    },
    {
        id:         Constants.DashboardNav.ITEM_COMPANY,
        name:       "Company",
        state:      "company",
        accessLevel:Constants.Role.ADMIN,
        options:    [
            Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_SETTINGS]
        ]
    }
]

/* ------------------------------- Table -----------------------------------*/
Constants.Table = {
    // Table types
    TYPE_INVALID:   0,
    TYPE_LEAD:      1,
    TYPE_TASK:      2,
    TYPE_FORM:      3,
    TYPE_TEAM:      4,

    // Column size types
    COL_SIZE_S:         1,
    COL_SIZE_M:         2,
    COL_SIZE_L:         3,

    // Sorting Types
    SORT_NONE:          0,
    SORT_ASC:           1,
    SORT_DESC:          -1,

    // URL prefix
    URL_PREFIX: [
        'invalid',
        'leads',
        'tasks',
        'forms',
        'team'
    ],

    // Default rows per page
    DEFAULT_COUNT_PER_PAGE:     10,
    MAX_SELECTION_COUNT:        500,

    // Multiselect types
    MS_INCLUDE:                 1,
    MS_EXCLUDE:                 2,

    // IDs for mandatory columns in leads
    ID_LEAD_NAME:               0,
    ID_LEAD_ADDRESS:            1,
    ID_LEAD_LOCATION:           2,
    ID_LEAD_TEMPLATE:           3,
    ID_LEAD_LAST:               3
}

/* ------------------------------- Task -----------------------------------*/
Constants.Task = {
    // Status constants
    STATUS_OPEN:    1,
    STATUS_CLOSED:  2
}

// Status names
Constants.Task.STATUS_NAME = [
    "",
    "OPEN",
    "CLOSED"
]

/* ------------------------------- Template -----------------------------------*/
Constants.Template = {
    // Template Types
    TYPE_FORM:          1,
    TYPE_LEAD:          2,
    TYPE_TASK:          3,
    TYPE_PRODUCT:     4,

    // Template Field types
    FIELD_TYPE_TEXT:            1,
    FIELD_TYPE_NUMBER:          2,
    FIELD_TYPE_RADIOLIST:       3,
    FIELD_TYPE_CHECKLIST:       4,
    FIELD_TYPE_PHOTO:           5,
    FIELD_TYPE_SIGN:            6,
    FIELD_TYPE_LOCATION:        7,
    FIELD_TYPE_CHECKBOX:        8,
    FIELD_TYPE_DATE:            9,
    FIELD_TYPE_FILE:            10,

    // Special Object Column type
    FIELD_TYPE_LEAD:            21,
    FIELD_TYPE_TASK:            22,

    FIELD_TYPE_TEMPLATE:        41,

    // Field Names
    FIELD_NAMES:    [
        '',
        'Text',
        'Number',
        'Radio List',
        'Checklist',
        'Photo',
        'Signature',
        'Location',
        'Checkbox',
        'Date',
        'File Upload'
    ]
}

// Available field types for different template editors
Constants.Template.FORM_FIELD_TYPES = [
    Constants.Template.FIELD_TYPE_TEXT,
    Constants.Template.FIELD_TYPE_NUMBER,
    Constants.Template.FIELD_TYPE_RADIOLIST,
    Constants.Template.FIELD_TYPE_CHECKLIST,
    Constants.Template.FIELD_TYPE_PHOTO,
    Constants.Template.FIELD_TYPE_FILE,
    Constants.Template.FIELD_TYPE_SIGN,
    Constants.Template.FIELD_TYPE_CHECKBOX,
    Constants.Template.FIELD_TYPE_DATE
]

Constants.Template.LEAD_FIELD_TYPES = [
    Constants.Template.FIELD_TYPE_TEXT,
    Constants.Template.FIELD_TYPE_NUMBER,
    Constants.Template.FIELD_TYPE_RADIOLIST,
    Constants.Template.FIELD_TYPE_CHECKLIST,
    Constants.Template.FIELD_TYPE_CHECKBOX,
    Constants.Template.FIELD_TYPE_DATE
]

Constants.Template.TASK_FIELD_TYPES = [
    Constants.Template.FIELD_TYPE_TEXT,
    Constants.Template.FIELD_TYPE_NUMBER,
    Constants.Template.FIELD_TYPE_RADIOLIST,
    Constants.Template.FIELD_TYPE_CHECKLIST,
    Constants.Template.FIELD_TYPE_CHECKBOX,
    Constants.Template.FIELD_TYPE_DATE
]

Constants.Template.PRODUCT_FIELD_TYPES = [
    Constants.Template.FIELD_TYPE_TEXT,
    Constants.Template.FIELD_TYPE_NUMBER,
    Constants.Template.FIELD_TYPE_RADIOLIST,
    Constants.Template.FIELD_TYPE_CHECKLIST,
    Constants.Template.FIELD_TYPE_CHECKBOX,
    Constants.Template.FIELD_TYPE_DATE
]

/* ------------------------------- Filters -----------------------------------*/
Constants.Filter = {
    // Filter types
    TYPE_NONE           : 0,
    TYPE_SELECTION      : 1,
    TYPE_TEXT           : 2,
    TYPE_NUMBER         : 3,
    TYPE_DATE           : 4,
    TYPE_OBJECT         : 5
}

Constants.Date = {
    // Date format types
    FORMAT_LONG:            "YYYY-MM-DD HH:mm:ss",
    FORMAT_SHORT:           "D/MM h:mm A",
    FORMAT_DESCRIPTIVE:     "DD MMM YYYY h:mm A",
    FORMAT_DATE_ONLY:       "DD MMMM YYYY",
    FORMAT_FILE_SUFFIX:     "_DD_MM_YY_HHmm"
}

/* ------------------------------- Map -----------------------------------*/
Constants.Map = {
    // Default map values
    DEFAULT_ZOOM:       14,

    // Marker Types
    MARKER_DEFAULT:    1,
    MARKER_GREEN:      2,
    MARKER_RED:        3
}