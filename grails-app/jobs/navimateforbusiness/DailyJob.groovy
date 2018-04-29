package navimateforbusiness

import navimateforbusiness.Task

class DailyJob {
    static triggers = {
        //declared a trigger with time 00:01:00 of everyday
        cron name: 'dailyTrigger', cronExpression: "0 1 0 * * ?"
    }

    void execute() {
        /*def currentDate = (new Date()).format("yyyy-MM-dd")

        def tasks = Task.findAll ()
        tasks.each {task ->
            if((task.period != 0) && (task.manager !=  null)){
                //create a new task if old task is expired
                def dateCreated = task.dateCreated + task.period
                def expiryDate = dateCreated.format("yyyy-MM-dd")
                if(expiryDate <= currentDate) {
                    //Creating new task with previous details
                    Task newTask = new Task(
                            account:        task.account,
                            manager:        task.manager,
                            rep:            task.rep,
                            lead:           task.lead,
                            formTemplate:   task.formTemplate,
                            period:         task.period,
                            status:         navimateforbusiness.TaskStatus.OPEN
                    )
                    newTask.save(flush: true, failOnError: true)

                    //closed the finished task and set period to zero
                    task.status = TaskStatus.CLOSED
                    task.period = 0
                    task.save(flush: true, failOnError: true)
                }
            }
        }*/
    }
}

