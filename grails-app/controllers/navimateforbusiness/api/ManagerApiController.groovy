package navimateforbusiness.api

import grails.converters.JSON
import grails.core.GrailsApplication
import navimateforbusiness.FormM
import navimateforbusiness.TaskM
import navimateforbusiness.User
import navimateforbusiness.enums.Role
import navimateforbusiness.objects.ObjPager
import navimateforbusiness.objects.ObjSorter
import navimateforbusiness.util.Constants
import navimateforbusiness.LeadM
import navimateforbusiness.ProductM
import navimateforbusiness.enums.TaskStatus
import navimateforbusiness.Template
import navimateforbusiness.enums.Visibility
import navimateforbusiness.util.ApiException
import org.springframework.web.multipart.MultipartFile

// APIs exposed to users with manager access or higher
class ManagerApiController {
    // ----------------------- Dependencies ---------------------------//
    def authService
    def userService
    def leadService
    def taskService
    def formService
    def fcmService
    def templateService
    def tableService
    def filtrService
    def sortingService
    def searchService
    def exportService
    def importService
    def productService
    def excelService

    GrailsApplication grailsApplication

    // ----------------------- Template APIs ----------------------- //
    def getTemplates() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get templates for this user
        List<Template> templates = templateService.getForUser(user)

        // Prepare JSON response
        def resp = []
        templates.each {template ->
            resp.push(templateService.toJson(template))
        }

        render resp as JSON
    }

    def searchTemplates() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get input params
        String text = request.JSON.text
        def pager = new ObjPager(request.JSON.pager)

        // Get all for this user
        def templates = templateService.getForUser(user)

        // Perform Search
        def fTemplates = templates.findAll {it.name.toLowerCase().contains(text.toLowerCase())}

        // Apply Paging
        def pTemplates = pager.apply(fTemplates)

        // Send response with IDs, names and total count
        def resp = [
                results: pTemplates.collect {[id: it.id, name: it.name]},
                totalCount: fTemplates.size()
        ]
        render resp as JSON
    }

    // ----------------------- Team APIs ----------------------- //
    def getTeamById () {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // get team members for this user
        def team = userService.getRepsForUser(user)

        // Find users with given IDs
        def selectedTeam = []
        request.JSON.ids.each {id -> selectedTeam.push(team.find {it -> it.id == id}) }

        // Throw exception if all users not found
        if (selectedTeam.size() != request.JSON.ids.size()) {
            throw new ApiException("Invalid team IDs requested", Constants.HttpCodes.BAD_REQUEST)
        }

        // Prepare JSON response
        def resp = []
        selectedTeam.each {rep -> resp.push(userService.toJson(rep))}

        render resp as JSON
    }

    def getTeamTable() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get filters from request
        def filter = request.JSON.filter
        def pager = new ObjPager(filter.pager)

        // Get team for this user
        def team = userService.getRepsForUser(user)

        // Convert team to tabular format
        def table = tableService.parseTeam(user, team)

        // Apply column filters to table
        table.rows = filtrService.applyToTable(table.rows, filter.colFilters)
        int totalRows = table.rows.size()

        // Apply sorting to table
        table.rows = sortingService.sortRows(table.columns, table.rows, filter.sortList)

        // Apply paging to table
        table.rows = pager.apply(table.rows)

        // Send response
        def resp = [
                rows: table.rows,
                columns: request.JSON.bColumns ? table.columns : null,
                totalRows: totalRows
        ]
        render resp as JSON
    }

    def getRepIds() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get filters from request
        def filter = request.JSON.filter

        // get team for this user
        def team = userService.getRepsForUser(user)

        // Convert team to tabular format
        def table = tableService.parseTeam(user, team)

        // Apply column filters to table
        table.rows = filtrService.applyToTable(table.rows, filter.colFilters)

        // Prepare response as list of IDs & names
        def resp = []
        table.rows.each {row ->
            resp.push(id: row.id, name: row.name)
        }
        render resp as JSON
    }

    def exportTeam() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get filters from request
        def filter = request.JSON.filter
        def exportParams = request.JSON.exportParams

        // get team for this user
        def team = userService.getRepsForUser(user)

        // Convert team to tabular format
        def table = tableService.parseTeam(user, team)

        // Apply column filters to table
        table.rows = filtrService.applyToTable(table.rows, filter.colFilters)

        // Apply sorting to table
        table.rows = sortingService.sortRows(table.columns, table.rows, filter.sortList)

        // Export table
        def exportData = tableService.getExportData(table, exportParams)

        // Set response parameters
        response.setHeader("Content-disposition", "attachment; filename=exportfile.xls")
        response.contentType = grailsApplication.config.getProperty("grails.mime.types.xls")

        // Export data
        exportService.export('csv', response.outputStream, exportData.objects, exportData.fields, exportData.labels, [:], [:])
    }

    def searchTeam() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get input params
        def text = request.JSON.text
        def pager = new ObjPager(request.JSON.pager)

        // get reps for this user
        def reps = userService.getRepsForUser(user)

        // get search results
        reps = searchService.searchUsers(reps, text)
        int totalCount = reps.size()

        // Get pages results
        reps = pager.apply(reps)

        // Create response array with IDs and names
        def resp = [
                items: [],
                totalCount: totalCount
        ]
        reps.each {it -> resp.items.push([id: it.id, title: it.name])}

        // Send response
        render resp as JSON
    }

    def searchReps() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get input params
        String text = request.JSON.text
        def pager = new ObjPager(request.JSON.pager)

        // Get all for this user
        def reps = userService.getRepsForUser(user)

        // Perform Search
        reps = reps.findAll {it.name.toLowerCase().contains(text.toLowerCase())}

        // Apply Paging
        def pagedReps = pager.apply(reps)

        // Send response with IDs, names and total count
        def resp = [
                results: pagedReps.collect {[id: it.id, name: it.name]},
                totalCount: reps.size()
        ]
        render resp as JSON
    }

    def searchNonReps() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get input params
        String text = request.JSON.text
        def pager = new ObjPager(request.JSON.pager)

        // Get all for this user
        def users = userService.getNonRepsForUser(user)

        // Perform Search
        users = users.findAll {it.name.toLowerCase().contains(text.toLowerCase())}

        // Apply Paging
        def pagedUsers = pager.apply(users)

        // Send response with IDs, names and total count
        def resp = [
                results: pagedUsers.collect {[id: it.id, name: it.name]},
                totalCount: users.size()
        ]
        render resp as JSON
    }

    // ----------------------- LEAD APIs ----------------------- //
    def getLeadsById () {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Find leads with given IDs
        def leads = leadService.getAllForUserByFilter(user, [ids: request.JSON.ids])

        // Throw exception if all tasks not found
        if (leads.size() != request.JSON.ids.size()) {
            throw new ApiException("Invalid lead IDs requested", Constants.HttpCodes.BAD_REQUEST)
        }

        // Prepare JSON response
        def resp = []
        leads.each {lead -> resp.push(leadService.toJson(lead, user))}

        render resp as JSON
    }

    def getLeadTable() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // get leads for this user
        def filteredLeads = leadService.filter(user, request.JSON, true)

        // Send JSON Response
        def resp = [
                rowCount: filteredLeads.rowCount,
                leads: filteredLeads.leads.collect {leadService.toJson(it, user)}
        ]
        render resp as JSON
    }

    def editLeads() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Parse lead JSON to task objects
        def leads = []
        def reps = []
        request.JSON.leads.each {leadJson ->
            // Parse to lead object and assign update & create time
            LeadM lead = leadService.fromJson(leadJson, user)

            leads.push(lead)
            reps.addAll(leadService.getAffectedReps(user, lead))
        }

        // Save tasks
        leads.each {it -> it.save(flush: true, failOnError: true)}

        // Collect FCM Ids of affected reps & notify each rep
        fcmService.notifyUsers(reps, Constants.Notifications.TYPE_LEAD_UPDATE)

        // Return response
        def resp = [success: true]
        render resp as JSON
    }

    def removeLeads() {
        // Get user
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Iterate through IDs to be remove
        def reps = []
        request.JSON.ids.each {id ->
            // Get lead with this id
            LeadM lead = leadService.getForUserByFilter(user, [ids: [id]])
            if (!lead) {
                throw new ApiException("Lead not found...", Constants.HttpCodes.BAD_REQUEST)
            }

            // remove only if lead is editable by this user
            if (user.role == Role.ADMIN || lead.ownerId == user.id) {
                // Remove lead
                reps.addAll(leadService.getAffectedReps(user, lead))
                leadService.remove(user, lead)
            }
        }

        // Collect FCM Ids of affected reps & notify each rep
        fcmService.notifyUsers(reps, Constants.Notifications.TYPE_LEAD_UPDATE)

        def resp = [success: true]
        render resp as JSON
    }

    def searchLeads() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get input params
        String text = request.JSON.text
        def pager = new ObjPager(request.JSON.pager)

        // Filter leads for this user
        def leads = leadService.getAllForUserByFPS(user, [name: [regex: text]], pager, new ObjSorter())

        // Send response with IDs, names and total count
        def resp = [
                results: leads.leads.collect {[id: it.id, name: it.name]},
                totalCount: leads.rowCount
        ]
        render resp as JSON
    }

    def getLeadIds() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // filter using request JSON
        def filteredLeads = leadService.filter(user, request.JSON, false)

        // Ensure number of rows are less than max limit
        if (filteredLeads.leads.size() > Constants.Table.MAX_SELECTION_COUNT) {
            throw new ApiException("Too many rows. Maximum " + Constants.Table.MAX_SELECTION_COUNT + " rows can be selected at once.")
        }

        // Prepare response as list of IDs & names
        def resp = []
        filteredLeads.leads.each {LeadM lead ->
            resp.push(id: lead.id, name: lead.name)
        }
        // Send response
        render resp as JSON
    }

    def exportLeads() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get Export Parameters
        def exportParams = request.JSON.exportParams

        // Filter objects
        def leads = leadService.filter(user, request.JSON, false).leads

        // Extract selected leads if applicable
        if (exportParams.selection) {
            // Find all leads with IDs contained in selection array
            leads = leads.findAll {it -> exportParams.selection.contains(it.id)}
        }

        // Create workbook using excel service
        def wb = excelService.exportToWorkbook(user, Constants.Template.TYPE_LEAD, leads, exportParams)

        // Set response parameters
        response.setHeader("Content-disposition", "attachment; filename=exportfile.xls")
        response.contentType = grailsApplication.config.getProperty("grails.mime.types.excel")

        // Send workboot as response
        wb.write(response.getOutputStream())
        response.flushBuffer()
    }

    def importLeads() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get file
        MultipartFile file = request.getFile('importFile')

        // Parse to Leads and save
        excelService.importFile(user, Constants.Template.TYPE_LEAD, file)

        def resp = [success: true]
        render resp as JSON
    }

    // ----------------------- TASK APIs ----------------------- //
    def getTasksById () {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Find leads with given IDs
        def tasks = taskService.getAllForUserByFilter(user, [ids: request.JSON.ids])

        // Throw exception if all tasks not found
        if (tasks.size() != request.JSON.ids.size()) {
            throw new ApiException("Invalid task IDs requested", Constants.HttpCodes.BAD_REQUEST)
        }

        // Prepare JSON response
        def resp = tasks.collect {taskService.toJson(it, user)}

        render resp as JSON
    }

    def getTaskTable() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Filter Tasks
        def filteredTasks = taskService.filter(user, request.JSON, true)

        // Send JSON Response
        def resp = [
                rowCount: filteredTasks.rowCount,
                tasks: filteredTasks.tasks.collect {taskService.toJson(it, user)},
                leads: filteredTasks.tasks.collect {leadService.toJson(it.lead, user)}
        ]
        render resp as JSON
    }

    def getTaskIds() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Filter Tasks
        def filteredTasks = taskService.filter(user, request.JSON, false)

        // Ensure number of rows are less than max limit
        if (filteredTasks.tasks.size() > Constants.Table.MAX_SELECTION_COUNT) {
            throw new ApiException("Too many rows. Maximum " + Constants.Table.MAX_SELECTION_COUNT + " rows can be selected at once.")
        }

        // Prepare response as list of IDs & names
        def resp = filteredTasks.tasks.collect {[id: it.id, name: it.publicId]}
        render resp as JSON
    }

    def editTasks() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Parse task JSON to task objects
        def tasks = []
        def reps = []
        request.JSON.tasks.each {taskJson ->
            def task = taskService.fromJson(taskJson, user)
            tasks.push(task)

            // Push FCM
            if (task.repId) {reps.push(User.findById(task.repId))}
        }

        // Save tasks
        tasks.each {it -> it.save(flush: true, failOnError: true)}

        // Collect FCM Ids of affected reps & notify each rep
        fcmService.notifyUsers(reps, Constants.Notifications.TYPE_TASK_UPDATE)

        // Return response
        def resp = [success: true]
        render resp as JSON
    }

    def exportTasks() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get Export Parameters
        def exportParams = request.JSON.exportParams

        // Filter objects
        def tasks = taskService.filter(user, request.JSON, false).tasks

        // Extract selected leads if applicable
        if (exportParams.selection) {
            // Find all leads with IDs contained in selection array
            tasks = tasks.findAll {it -> exportParams.selection.contains(it.id)}
        }

        // Create workbook using excel service
        def wb = excelService.exportToWorkbook(user, Constants.Template.TYPE_TASK, tasks, exportParams)

        // Set response parameters
        response.setHeader("Content-disposition", "attachment; filename=exportfile.xls")
        response.contentType = grailsApplication.config.getProperty("grails.mime.types.excel")

        // Send workboot as response
        wb.write(response.getOutputStream())
        response.flushBuffer()
    }

    def importTasks() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get file
        MultipartFile file = request.getFile('importFile')

        // Parse to Leads and save
        excelService.importFile(user, Constants.Template.TYPE_TASK, file)

        def resp = [success: true]
        render resp as JSON
    }

    def removeTasks() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        def reps =[]
        request.JSON.ids.each {id ->
            TaskM task = taskService.getForUserByFilter(user, [ids: [id]])
            if (!task) {
                throw new ApiException("Task not found...", Constants.HttpCodes.BAD_REQUEST)
            }

            // Remove task
            if (task.repId) {reps.push(User.findById(task.repId))}
            taskService.remove(user, task)
        }

        // Collect FCM Ids of affected reps & notify each rep
        fcmService.notifyUsers(reps, Constants.Notifications.TYPE_TASK_UPDATE)

        def resp = [success: true]
        render resp as JSON
    }

    def closeTasks() {
        // Get user
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Iterate through IDs to be remove
        def reps  = []
        request.JSON.ids.each {id ->
            TaskM task = taskService.getForUserByFilter(user, [ids: [id]])
            if (!task) {
                throw new ApiException("Task not found...", Constants.HttpCodes.BAD_REQUEST)
            }

            // Update and save task
            if (task.status == TaskStatus.OPEN) {
                task.resolutionTimeHrs = taskService.getResolutionTime(task)
                task.status = TaskStatus.CLOSED
                if (task.repId) {reps.push(User.findById(task.repId))}
                task.save(flush: true, failOnError: true)
            }
        }

        // Collect FCM Ids of affected reps & notify each rep
        fcmService.notifyUsers(reps, Constants.Notifications.TYPE_TASK_UPDATE)

        def resp = [success: true]
        render resp as JSON
    }

    def stopTaskRenewal() {
        // Get user
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Iterate through IDs to be remove
        request.JSON.ids.each {id ->
            TaskM task = taskService.getForUserByFilter(user, [ids: [id]])
            if (!task) {
                throw new ApiException("Task not found...", Constants.HttpCodes.BAD_REQUEST)
            }

            // Update and save task
            task.period = 0
            task.save(flush: true, failOnError: true)
        }

        def resp = [success: true]
        render resp as JSON
    }

    def searchTasks() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get input params
        String text = request.JSON.text
        def pager = new ObjPager(request.JSON.pager)

        // Get all for this user
        def tasks = taskService.getAllForUserByFilter(user, [publicId: [regex: text]])

        // Apply Paging
        def pagedTasks = pager.apply(tasks)

        // Send response with IDs, names and total count
        def resp = [
                results: pagedTasks.collect {[id: it.id, name: it.publicId]},
                totalCount: tasks.size()
        ]
        render resp as JSON
    }

    // ----------------------- FORM APIs ----------------------- //
    def getFormsById () {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Find leads with given IDs
        def forms = formService.getAllForUserByFilter(user, [ids: request.JSON.ids])

        // Throw exception if all forms not found
        if (forms.size() != request.JSON.ids.size()) {
            throw new ApiException("Invalid form IDs requested", Constants.HttpCodes.BAD_REQUEST)
        }

        // Prepare JSON response
        def resp = []
        forms.each {form -> resp.push(formService.toJson(form, user))}

        render resp as JSON
    }

    def getFormTable() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // get leads for this user
        def filteredForms = formService.filter(user, request.JSON, true)

        // Send JSON Response
        def resp = [
                rowCount: filteredForms.rowCount,
                forms: filteredForms.forms.collect {formService.toJson(it, user)},
                tasks: filteredForms.forms.collect {it.task ? taskService.toJson(it.task, user) : null},
                leads: filteredForms.forms.collect {it.task ? leadService.toJson(it.task.lead, user) : null}
        ]
        render resp as JSON
    }

    def getFormIds() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get filters from request
        def filteredForms = formService.filter(user, request.JSON, false)

        // Ensure number of rows are less than max limit
        if (filteredForms.forms.size() > Constants.Table.MAX_SELECTION_COUNT) {
            throw new ApiException("Too many rows. Maximum " + Constants.Table.MAX_SELECTION_COUNT + " rows can be selected at once.")
        }

        // Prepare response as list of IDs & names
        def resp = filteredForms.forms.collect {[id: it.id, name: User.findById(it.ownerId).name]}
        render resp as JSON
    }

    def exportForms() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get Export Parameters
        def exportParams = request.JSON.exportParams

        // Filter objects
        def forms = formService.filter(user, request.JSON, false).forms

        // Extract selected leads if applicable
        if (exportParams.selection) {
            // Find all leads with IDs contained in selection array
            forms = forms.findAll {it -> exportParams.selection.contains(it.id)}
        }

        // Create workbook using excel service
        def wb = excelService.exportToWorkbook(user, Constants.Template.TYPE_FORM, forms, exportParams)

        // Set response parameters
        response.setHeader("Content-disposition", "attachment; filename=exportfile.xls")
        response.contentType = grailsApplication.config.getProperty("grails.mime.types.excel")

        // Send workboot as response
        wb.write(response.getOutputStream())
        response.flushBuffer()
    }

    def removeForms() {
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        request.JSON.ids.each {id ->
            FormM form = formService.getForUserByFilter(user, [ids: [id]])
            if (!form) {
                throw new ApiException("Form not found...", Constants.HttpCodes.BAD_REQUEST)
            }

            // Remove form
            formService.remove(user, form)
        }

        def resp = [success: true]
        render resp as JSON
    }

    // ----------------------- Product APIs ----------------------- //
    def editProduct() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Parse product JSON to product objects
        def products = request.JSON.products.collect { productService.fromJson(it, user) }

        // Save products
        products.each {it.save(flush: true, failOnError: true)}

        // Return response
        def resp = [success: true]
        render resp as JSON
    }

    def getProductsById () {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Find products with given IDs
        def products = productService.getAllForUserByFilter(user, [ids: request.JSON.ids])

        // Throw exception if all tasks not found
        if (products.size() != request.JSON.ids.size()) {
            throw new ApiException("Invalid product IDs requested", Constants.HttpCodes.BAD_REQUEST)
        }

        // Prepare JSON response
        def resp = []
        products.each {product -> resp.push(productService.toJson(product, user))}

        render resp as JSON
    }

    def getProductTable() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // filter using request JSON
        def filteredProducts = productService.filter(user, request.JSON, true)

        // Send JSON Response
        def resp = [
                rowCount: filteredProducts.rowCount,
                products: filteredProducts.products.collect {productService.toJson(it, user)}
        ]
        render resp as JSON
    }

    def getProductIds() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // filter using request JSON
        def filteredProducts = productService.filter(user, request.JSON, false)

        // Ensure number of rows are less than max limit
        if (filteredProducts.products.size() > Constants.Table.MAX_SELECTION_COUNT) {
            throw new ApiException("Too many rows. Maximum " + Constants.Table.MAX_SELECTION_COUNT + " rows can be selected at once.")
        }

        // Prepare response as list of IDs & names
        def resp = []
        filteredProducts.products.each {ProductM product ->
            resp.push(id: product.id, name: product.name)
        }
        // Send response
        render resp as JSON
    }

    def removeProduct() {
        // Get user
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Iterate through IDs to be remove
        def reps = []
        request.JSON.ids.each {id ->
            // Get product with this id
            ProductM product = productService.getForUserByFilter(user, [ids: [id]])
            if (!product) {
                throw new ApiException("Product not found...", Constants.HttpCodes.BAD_REQUEST)
            }

            // remove only if product is editable by this user
            if (user.role == Role.ADMIN || product.ownerId == user.id) {
                // Remove lead
                reps.addAll(productService.getAffectedReps(user, product))
                productService.remove(user, product)
            }
        }

        def resp = [success: true]
        render resp as JSON
    }

    def searchProducts() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get input params
        String text = request.JSON.text
        def pager = new ObjPager(request.JSON.pager)

        // Filter products for this user
        def products = productService.getAllForUserByFPS(user, [name: [regex: text]], pager, new ObjSorter())

        // Send response with IDs, names and total count
        def resp = [
                results: products.products.collect {[id: it.id, name: it.name]},
                totalCount: products.rowCount
        ]
        render resp as JSON
    }

    def importProducts() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get file
        MultipartFile file = request.getFile('importFile')

        // Parse to Leads and save
        excelService.importFile(user, Constants.Template.TYPE_PRODUCT, file)

        def resp = [success: true]
        render resp as JSON
    }

    def exportProducts() {
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        // Get Export Parameters
        def exportParams = request.JSON.exportParams

        // Filter objects
        def products = productService.filter(user, request.JSON, false).products

        // Extract selected leads if applicable
        if (exportParams.selection) {
            // Find all leads with IDs contained in selection array
            products = products.findAll {it -> exportParams.selection.contains(it.id)}
        }

        // Create workbook using excel service
        def wb = excelService.exportToWorkbook(user, Constants.Template.TYPE_PRODUCT, products, exportParams)

        // Set response parameters
        response.setHeader("Content-disposition", "attachment; filename=exportfile.xls")
        response.contentType = grailsApplication.config.getProperty("grails.mime.types.excel")

        // Send workboot as response
        wb.write(response.getOutputStream())
        response.flushBuffer()
    }

    // ----------------------- Private methods ----------------------- //
}
