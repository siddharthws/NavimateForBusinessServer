package navimateforbusiness

import org.grails.core.exceptions.GrailsException

class ApiException extends GrailsException {
    // response code to send for this exception
    int responseCode = navimateforbusiness.Constants.HttpCodes.INTERNAL_SERVER_ERROR

    public ApiException() {}

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, int responseCode) {
        super(message)
        this.responseCode = responseCode
    }
}
