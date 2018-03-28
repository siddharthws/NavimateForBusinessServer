/**
 * Created by Siddharth on 27-03-2018.
 */

app.factory('ObjTab', function() {
    // ----------------------------------- Constructor ------------------------------------//
    function ObjTab (name, state, access) {
        this.name       = name
        this.state      = state
        this.access     = access
    }

    return ObjTab
})
