package navimateforbusiness

import grails.gorm.transactions.Transactional
import com.mongodb.client.FindIterable
import navimateforbusiness.enums.TaskStatus
import navimateforbusiness.enums.Visibility
import navimateforbusiness.objects.ObjPager
import navimateforbusiness.objects.ObjSorter
import navimateforbusiness.util.ApiException
import navimateforbusiness.util.Constants

import static com.mongodb.client.model.Filters.*

@Transactional
class LeadService {
    // ----------------------- Dependencies ---------------------------//
    def googleApiService
    def templateService
    def fieldService
    def taskService
    def mongoService

    // ----------------------- Getter APIs ---------------------------//
    // Method to filter using input JSON
    def filter (User user, def requestJson, boolean bPaging) {
        // Get task filters, sorter and pager
        def leadFilter = requestJson.filter.find {it.id == Constants.Template.TYPE_LEAD}.filter
        def leadSorter = new ObjSorter(requestJson.sorter.find {it.id == Constants.Template.TYPE_LEAD}.sorter)
        ObjPager pager = bPaging ? new ObjPager(requestJson.pager) : new ObjPager()

        // Filter and return
        getAllForUserByFPS(user, leadFilter, pager, leadSorter)
    }

    // Method to search leads in mongo database using filter, pager and sorter
    def getAllForUserByFPS(User user, def filters, ObjPager pager, ObjSorter sorter) {
        // Get mongo filters
        def pipeline = mongoService.getLeadPipeline(user, filters, sorter)

        // Get results
        def dbResult = LeadM.aggregate(pipeline)
        int count = dbResult.size()

        // Apply paging
        def pagedResult = pager.apply(dbResult)

        // Return response
        return [
            rowCount: count,
            leads: pagedResult.collect { (LeadM) it }
        ]
    }

    // method to get list of leads using filters
    List<LeadM> getAllForUserByFilter(User user, def filters) {
        getAllForUserByFPS(user, filters, new ObjPager(), new ObjSorter()).leads
    }

    // Method to get a single lead using filters
    LeadM getForUserByFilter(User user, def filters) {
        getAllForUserByFilter(user, filters)[0]
    }

    // ----------------------- Public APIs ---------------------------//
    // Methods to convert lead objects to / from JSON
    def toJson(LeadM lead, User user) {
        // Convert template properties to JSON
        def json = [
                id:         lead.id,
                owner:      [id: lead.ownerId, name: User.findById(lead.ownerId).name],
                name:       lead.name,
                address:    lead.address,
                lat:        lead.latitude,
                lng:        lead.longitude,
                templateId: lead.templateId,
                values:     []
        ]

        // Add templated values in JSON
        def template = templateService.getForUserById(user, lead.templateId)
        def fields = fieldService.getForTemplate(template)
        fields.each {Field field ->
            json.values.push([fieldId: field.id, value: lead["$field.id"]])
        }

        json
    }

    LeadM fromJson(def json, User user) {
        LeadM lead = null

        // Get existing template or create new
        if (json.id) {
            lead = getForUserByFilter(user, [ids: [json.id]])
            if (!lead) {
                throw new ApiException("Illegal access to lead", Constants.HttpCodes.BAD_REQUEST)
            }
        } else if (json.extId) {
            lead = getForUserByFilter(user, [extId: json.extId])
        }

        // Create new lead object if not found
        if (!lead) {
            lead = new LeadM(
                    accountId: user.account.id,
                    ownerId: user.id,
                    visibility: Visibility.PUBLIC,
                    isRemoved: false,
                    extId: json.extId
            )
        }

        // Set name
        lead.name = json.name

        // Update address and latlng if changed
        if (json.address && lead.address != json.address) {
            lead.address = json.address

            if (!json.lat || !json.lng) {
                // Get latlng from google for this address
                String[] addresses = [json.address]
                def latlngs = googleApiService.geocode(addresses)
                lead.latitude = latlngs[0].lat
                lead.longitude = latlngs[0].lng
            } else {
                // Assign lat lng from JSON
                lead.latitude = json.lat
                lead.longitude = json.lng
            }
        }

        // Set template ID
        lead.templateId = json.templateId

        // Prepare template data
        def template = templateService.getForUserById(user, json.templateId)
        def fields = fieldService.getForTemplate(template)
        fields.each {field ->
            // Set value for this field from JSON received
            lead["$field.id"] = fieldService.parseValue(field, json.values.find {it -> it.fieldId == field.id}.value)
        }

        // Add date info in long format
        String currentTime = new Date().format( Constants.Date.FORMAT_LONG,
                                                Constants.Date.TIMEZONE_IST)
        if (!lead.createTime) {
            lead.createTime = currentTime
        }
        lead.updateTime = currentTime

        lead
    }

    // Method to convert lead array into exportable data
    def getExportData(User user, List<LeadM> leads, def params) {
        List objects    = []
        List fields     = []
        Map labels      = [:]

        // Validate data
        if (!leads) {
            throw new ApiException("No rows to export", Constants.HttpCodes.BAD_REQUEST)
        } else if (!params.columns) {
            throw new ApiException("No columns to export", Constants.HttpCodes.BAD_REQUEST)
        }

        // Create one export object for each selected row
        leads.each {it -> objects.push([:])}

        // Iterate through each column
        params.columns.each {column ->
            // Add field and label
            fields.push(column.label)
            labels.put(column.label, column.label)

            // Iterate through each lead
            leads.eachWithIndex {lead, i ->
                def value

                if (column.field == "template") {
                    value = templateService.getForUserById(user, lead.templateId).name
                } else if (column.field == "location") {
                    value = "https://www.google.com/maps/search/?api=1&query=" + lead.latitude + "," + lead.longitude
                } else {
                    value = lead["$column.field"]
                    if (value) {
                        value = fieldService.formatForExport(column.type, value)
                    }
                }

                if (value == null || value.equals("")) {
                    value = '-'
                }

                // Add data to objects
                objects[i][column.label] = value
            }
        }

        return [
                objects: objects,
                fields: fields,
                labels: labels
        ]
    }

    // Method to remove a lead object
    def remove(User user, LeadM lead) {
        // Remove all tasks associated with lead
        def tasks = taskService.getAllForUserByFilter(user, [lead: [ids: [lead.id]]])
        tasks.each {  taskService.remove(user, it) }

        // Remove lead
        lead.isRemoved = true
        lead.updateTime = new Date().format(Constants.Date.FORMAT_LONG,
                                            Constants.Date.TIMEZONE_IST)
        lead.save(failOnError: true, flush: true)
    }

    // Method to get FCMs associated with the lead
    def getAffectedReps (User user, LeadM lead) {
        def reps = []

        // Get reps from all open tasks with this lead
        def tasks = taskService.getAllForUserByFilter(user, [lead: [ids: [lead.id]]])
        def openTasks = tasks.findAll {it.status == TaskStatus.OPEN}
        openTasks.each { if (it.repId) {reps.push(User.findById(it.repId))} }

        reps
    }

    // ----------------------- Private APIs ---------------------------//
}
