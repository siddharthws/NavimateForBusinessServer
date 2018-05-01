package navimateforbusiness.api

import navimateforbusiness.Task

import grails.converters.JSON

class PortingApiController {

    def fixManagers() {
        // Iterate through all tasks
        Task.findAll().eachWithIndex { Task task, int i ->
            if (task.rep && task.account == task.rep.account && task.manager != task.rep.manager) {
                task.manager = task.rep.manager
                task.save(flush: true, failOnError: true)
                log.error(task.id + " : " + task.manager.name + " : " + task.rep.manager.name)
            }
        }

        def resp = [success: true]
        render resp as JSON
    }
}
