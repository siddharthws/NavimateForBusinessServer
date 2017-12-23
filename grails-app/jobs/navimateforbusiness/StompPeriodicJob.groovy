package navimateforbusiness

/**
 * Created by Siddharth on 24-12-2017.
 */
class StompPeriodicJob {
    // Inject Stomp Service
    StompSessionService stompSessionService

    // Add 15 minutes trigger
    static triggers = {
        simple name: 'stompPeriodicTrigger', repeatInterval: 15 * 60 * 1000
    }

    void execute() {
        // Cleanup stomp sessions
        stompSessionService.cleanSessions()
    }
}
