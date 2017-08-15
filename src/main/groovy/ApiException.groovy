package navimateforbusiness

import org.grails.core.exceptions.GrailsException

class ApiException extends GrailsException {
    int responseCode = 500 // response code to send for this exception

    public ApiException() {}

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, int responseCode) {
        super(message)
        this.responseCode = responseCode
    }
}
