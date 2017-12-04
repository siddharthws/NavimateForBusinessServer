/**
 * Created by Siddharth on 04-12-2017.
 */
// Static APIs available throughout frontend
var Statics = {}

// API to absorb UI event
Statics.absorbEvent = function (event) {
    event.stopPropagation()
}
