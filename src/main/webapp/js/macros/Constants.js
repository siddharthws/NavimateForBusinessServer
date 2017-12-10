
// Constants available throughout app
var Constants = {}

/* ------------------------------- Tracking -----------------------------------*/
Constants.Tracking = {
    // Status Constants
    STATUS_UNAVAILABLE: 0,
    STATUS_WAITING:     1,
    STATUS_AVAILABLE:   2
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

    OPTION_MANAGE   : 0,
    OPTION_REPORT   : 1,
    OPTION_FORM     : 2
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
    }
]

Constants.DashboardNav.Menu = [
    {
        id:         Constants.DashboardNav.ITEM_TEAM,
        name:       "Team",
        state:      "team",
        options:    [
            Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_MANAGE]
        ]
    },
    {
        id:         Constants.DashboardNav.ITEM_LEADS,
        name:       "Leads",
        state:      "leads",
        options:    [
            Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_MANAGE]
        ]
    },
    {
        id:         Constants.DashboardNav.ITEM_TASKS,
        name:       "Tasks",
        state:      "tasks",
        options:    [
            Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_MANAGE]
        ]
    },
    {
        id:         Constants.DashboardNav.ITEM_REPORTS,
        name:       "Reports",
        state:      "reports",
        options:    [
            Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_REPORT]
        ]
    },
    {
        id:         Constants.DashboardNav.ITEM_TEMPLATES,
        name:       "Templates",
        state:      "templates",
        options:    [
            Constants.DashboardNav.Options[Constants.DashboardNav.OPTION_FORM]
        ]
    }
]

/* ------------------------------- Template -----------------------------------*/
Constants.Template = {
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

/* ------------------------------- Filters -----------------------------------*/
Constants.Filter = {
    // Filter types
    TYPE_NONE           : 0,
    TYPE_SELECTION      : 1,
    TYPE_TEXT           : 2,
    TYPE_NUMBER         : 3,
    TYPE_DATE           : 4
}