package navimateforbusiness.api

import navimateforbusiness.Task

class PortingApiController {

    // Port task creator column from owner
    def taskCreator() {
        def tasks = Task.findAll()
        tasks.each {task ->
            task.creator = task.manager
            task.save(flush: true, failOnError: true)
        }
    }
}
