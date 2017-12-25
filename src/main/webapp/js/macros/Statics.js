/**
 * Created by Siddharth on 04-12-2017.
 */
// Static APIs available throughout frontend
var Statics = {}

// API to absorb UI event
Statics.absorbEvent = function (event) {
    event.stopPropagation()
}

Statics.getFormattedElapsedTime = function (timeMs) {
    // Get current time
    var currentTime = Date.now()

    // Get elapsed time in seconds
    var elapsedTimeS = (currentTime - timeMs) / 1000

    // Create time and unit text from elapsedTime
    var timeText = ""
    var unitText = ""
    if (elapsedTimeS < 60) {
        timeText = Math.round(elapsedTimeS)
        unitText = "second"
    } else if (elapsedTimeS < 60 * 60) {
        timeText = Math.round(elapsedTimeS / 60)
        unitText = "minute"
    } else if (elapsedTimeS < 24 * 60 * 60) {
        timeText = Math.round(elapsedTimeS / (60 * 60))
        unitText = "hour"
    } else if (elapsedTimeS < 60 * 60) {
        timeText = Math.round(elapsedTimeS / (24 * 60 * 60))
        unitText = "day"
    }

    // Change unit to plural if required
    if (timeText > 1) {
        unitText += "s"
    }

    return timeText + " " + unitText
}
