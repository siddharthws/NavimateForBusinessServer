
// Constants available throughout app
var Constants = {}

/* ------------------------------- Events -------------------------------------*/
Constants.Events = {
    // Table Module Related events
    TABLE_INIT:             "event_table_init",
    TABLE_EXPORT:           "event_table_export",
    TABLE_TOGGLE_COLUMNS:   "event_table_toggle_columns",
    TABLE_CLEAR_FILTERS:    "event_table_clear_filters",

    // Template Editor Module related events
    TEMPLATE_VALIDATE:          "event_template_validate",
    TEMPLATE_VALIDATE_SUCCESS:  "event_template_validate_success",

    // Map related events
    MAP_CENTER:             "evt-map-center",
    MAP_MARKER_CLICK:       "evt-marker-click",
    MAP_MARKER_DRAGEND:     "evt-marker-dragend",

    // Data Service Related events
    TEAM_DATA_READY:              "evt-team-data-ready",
    LEAD_DATA_READY:              "evt-lead-data-ready",
    FORM_TEMPLATE_DATA_READY:     "evt-form-template-data-ready",
    LEAD_TEMPLATE_DATA_READY:     "evt-lead-template-data-ready",
    TASK_TEMPLATE_DATA_READY:     "evt-task-template-data-ready",
    TASK_DATA_READY:              "evt-task-data-ready"
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
    ERROR_OFFLINE:          6
}

// Form Related Constants
Constants.Form = {
    Types : [
        'text',
        'number',
        'radioList',
        'checkList',
        'photo',
        'signature'
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

    OPTION_MANAGE   : 0,
    OPTION_REPORT   : 1,
    OPTION_FORM     : 2,
    OPTION_LEAD     : 3,
    OPTION_TASK     : 4,
    OPTION_PROFILE  : 5,
    OPTION_OPEN     : 6,
    OPTION_CLOSE    : 7
}

/*-----------------------------Dashboard user role-------------------------------------*/
Constants.Role = {
    REP :       1,
    MANAGER:    2,
    ADMIN:      3,
    NVM_ADMIN:  4
}

Constants.DashboardNav.Options = [
    {
        id:         Constants.DashboardNav.OPTION_MANAGE,
        name:       'Manage',
        state:      "manage"
    },
    {
        id:         Constants.DashboardNav.OPTION_REPORT,
        name:       'Reports',
        state:      'report'
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
        id:         Constants.DashboardNav.OPTION_OPEN,
        name:       'Open',
        state:      'open'
    },
    {
        id:         Constants.DashboardNav.OPTION_CLOSE,
        name:       'Close',
        state:      'close'
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
            Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_OPEN],
            Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_CLOSE]
        ]
    },
    {
        id:         Constants.DashboardNav.ITEM_REPORTS,
        name:       "Reports",
        state:      "reports",
        accessLevel:Constants.Role.MANAGER,
        options:    [
            Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_REPORT]
        ]
    },
    {
        id:         Constants.DashboardNav.ITEM_TEMPLATES,
        name:       "Templates",
        state:      "templates",
        accessLevel:Constants.Role.MANAGER,
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
            Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_PROFILE]
        ]
    }
]

/* ------------------------------- Pagination -----------------------------------*/
Constants.Table = {
    DEFAULT_COUNT_PER_PAGE:     10
}

/* ------------------------------- Template -----------------------------------*/
Constants.Template = {
    // Template Types
    TYPE_FORM:          1,
    TYPE_LEAD:          2,

    // Template Field types
    FIELD_TYPE_TEXT:            1,
    FIELD_TYPE_NUMBER:          2,
    FIELD_TYPE_RADIOLIST:       3,
    FIELD_TYPE_CHECKLIST:       4,
    FIELD_TYPE_PHOTO:           5,
    FIELD_TYPE_SIGN:            6,
    FIELD_TYPE_LOCATION:        7,

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
    ]
}

// Available field types for different template editors
Constants.Template.FORM_FIELD_TYPES = [
    Constants.Template.FIELD_TYPE_TEXT,
    Constants.Template.FIELD_TYPE_NUMBER,
    Constants.Template.FIELD_TYPE_RADIOLIST,
    Constants.Template.FIELD_TYPE_CHECKLIST,
    Constants.Template.FIELD_TYPE_PHOTO,
    Constants.Template.FIELD_TYPE_SIGN
]

Constants.Template.LEAD_FIELD_TYPES = [
    Constants.Template.FIELD_TYPE_TEXT,
    Constants.Template.FIELD_TYPE_NUMBER,
    Constants.Template.FIELD_TYPE_RADIOLIST,
    Constants.Template.FIELD_TYPE_CHECKLIST
]

Constants.Template.TASK_FIELD_TYPES = [
    Constants.Template.FIELD_TYPE_TEXT,
    Constants.Template.FIELD_TYPE_NUMBER,
    Constants.Template.FIELD_TYPE_RADIOLIST,
    Constants.Template.FIELD_TYPE_CHECKLIST
]

/* ------------------------------- Filters -----------------------------------*/
Constants.Filter = {
    // Filter types
    TYPE_NONE           : 0,
    TYPE_SELECTION      : 1,
    TYPE_TEXT           : 2,
    TYPE_NUMBER         : 3,
    TYPE_DATE           : 4
}