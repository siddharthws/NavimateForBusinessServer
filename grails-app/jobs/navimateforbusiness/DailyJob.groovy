package navimateforbusiness

import navimateforbusiness.Task

class DailyJob {
    static triggers = {
        //declared a trigger with time 00:01:00 of everyday
        cron name: 'dailyTrigger', cronExpression: "0 1 0 * * ?"
    }

    void execute() {
        Date current_date = new Date()
        Date date = current_date.format("yyyy-MM-dd")

        def tasks = Task.findAll ()
            tasks.each {task ->
                if(task.period != 0){
                    def date_created = task.dateCreated.format("yyyy-MM-dd")
                    def period = task.period
                    if(date_created + period >= date) {
                        //Creating new task with previous details
                        Task new_task = new Task(
                                account:    task.account,
                                manager:    task.manager,
                                rep:        task.rep,
                                lead:       task.lead,
                                template:   task.template,
                                period:     task.period,
                                status:     navimateforbusiness.TaskStatus.OPEN
                        )
                        new_task.save(flush: true, failOnError: true)
                        //closed the finished task and set period to zero
                        task.status = TaskStatus.CLOSED
                        task.period = 0
                        task.save(flush: true, failOnError: true)
                    }
                }
            }
    }
}

