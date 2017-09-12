/**
 * Created by Siddharth on 12-09-2017.
 */
/**
 * Created by Siddharth on 12-09-2017.
 */

    // Indexes for Menu Items & their corresponding options
var MENU_ITEM_TEAM              = 0
var MENU_ITEM_LEADS             = 1
var MENU_ITEM_TASKS             = 2
var MENU_ITEM_FORMS             = 3

var ITEM_OPTION_MANAGE          = 0
var ITEM_OPTION_REPORT          = 1

var ITEM_OPTIONS = [
    {
        id: ITEM_OPTION_MANAGE,
        name: 'manage',
        title: 'Manage'
    },
    {
        id: ITEM_OPTION_REPORT,
        name: 'report',
        title: 'Reports'
    }
]

// Menu Item Objects
var MENU_ITEMS = [
    {
        id: MENU_ITEM_TEAM,
        name: 'team',
        options: [
            ITEM_OPTIONS[ITEM_OPTION_MANAGE],
            ITEM_OPTIONS[ITEM_OPTION_REPORT]
        ]
    },
    {
        id: MENU_ITEM_LEADS,
        name: 'leads',
        options: [
             ITEM_OPTIONS[ITEM_OPTION_MANAGE],
             ITEM_OPTIONS[ITEM_OPTION_REPORT]
        ]
    },
    {
        id: MENU_ITEM_TASKS,
        name: 'tasks',
        options: [
            ITEM_OPTIONS[ITEM_OPTION_MANAGE]
        ]
    },
    {
        id: MENU_ITEM_FORMS,
        name: 'forms',
        options: [
            ITEM_OPTIONS[ITEM_OPTION_MANAGE]
        ]
    }
]
