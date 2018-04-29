/**
 * Created by Siddharth on 04-12-2017.
 */
// Static APIs available throughout frontend
var Statics = {}

// APi to create array using size
Statics.getArray = function (size) {
    return new Array(size)
}

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

// Method to convert value object from / to String
Statics.getValueFromString = function (valueString, fieldType) {
    var value = valueString

    // Parse string for specific types
    switch (fieldType) {
        case Constants.Template.FIELD_TYPE_RADIOLIST:
        case Constants.Template.FIELD_TYPE_CHECKLIST:
            if (value && value.length) {
                value = JSON.parse(value)
            }
            break
        case Constants.Template.FIELD_TYPE_CHECKBOX:
            value = (value == 'true')
            break
    }

    // Return Value object
    return value
}

Statics.getStringFromValue = function (value, fieldType) {
    var valueString = value

    // Parse string for specific types
    switch (fieldType) {
        case Constants.Template.FIELD_TYPE_RADIOLIST:
        case Constants.Template.FIELD_TYPE_CHECKLIST:
            if (value) {
                valueString = JSON.stringify(value)
            }
            break
        case Constants.Template.FIELD_TYPE_CHECKBOX:
            valueString = value ? 'true' : 'false'
            break
    }

    // Return Value object
    return valueString
}

// Method to validate a number input
Statics.validateNumber = function (num) {
    if ((!num && num != 0) || (num == 'null')) {
        return false
    }

    return true
}
