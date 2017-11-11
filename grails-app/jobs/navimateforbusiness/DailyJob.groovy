package navimateforbusiness

class DailyJob {
    static triggers = {
        //declared a trigger with time 00:01:00 of everyday
        cron name: 'dailyTrigger', cronExpression: "0 1 0 * * ?"
    }

    void execute() {

    }
}