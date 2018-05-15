/**
 * Created by Siddharth on 27-03-2018.
 */

app.factory('ObjMenu', function($state) {
    // ----------------------------------- Constructor ------------------------------------//
    function ObjMenu (name, icon, access, tabs) {
        this.name       = name
        this.icon       = icon
        this.access     = access
        this.tabs       = tabs
        this.activeTab  = this.tabs[0]
    }

    // ----------------------------------- Public methods ------------------------------------//
    // method to change state when this menu item is selected
    ObjMenu.prototype.select = function () {
        $state.go('dashboard.' + this.activeTab.state)
    }

    // method to change state when a tab in this menu item is selected
    ObjMenu.prototype.selectTab = function (idx) {
        // Change state
        $state.go('dashboard.' + this.tabs[idx].state)
    }

    return ObjMenu
})
