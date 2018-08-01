/**
 * Created by Siddharth on 04-12-2017.
 */
// Static APIs available throughout frontend
var Statics = {}

// APi to create array using size
Statics.getArray = function (size) {
    return new Array(size)
}

Array.prototype.contains = function (elem) {
    return this.indexOf(elem) != -1
}

Array.prototype.addAll = function (arr) {
    if (arr) {
        var that = this
        arr.forEach(function (elem) {
            if (!that.contains(elem)) {
                that.push(elem)
            }
        })
    }
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
    } else {
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
        case Constants.Template.FIELD_TYPE_PRODUCT:
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
        case Constants.Template.FIELD_TYPE_PRODUCT:
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

// Method to validate a latlng
Statics.isPositionValid = function (pos) {
    if (pos != null && pos.lat() != 0 && pos.lng() != 0) {
        return true
    }

    return false
}

// Attach format method to Date prototype
Date.prototype.format = function (format) {
    return moment(this.toString()).format(format)
}
