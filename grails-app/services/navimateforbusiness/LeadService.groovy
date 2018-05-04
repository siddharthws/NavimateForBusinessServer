package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import com.mongodb.client.FindIterable

import java.text.SimpleDateFormat

import static com.mongodb.client.model.Filters.*

@Transactional
class LeadService {
    // ----------------------- Dependencies ---------------------------//
    def googleApiService
    def templateService
    def fieldService
    def taskService

    // ----------------------- Getter APIs ---------------------------//
    // Method to search leads in mongo database using filter, pager and sorter
    def getForUserByFilter(User user, def filters, def pager, def sorter) {
        // Prepare mongo filters
        def mongoFilters = []

        // Add accountId and isRemoved flag filters
        mongoFilters.push(eq("accountId", user.accountId))

        // Add isRemoved filter by default (unless specified in filters
        if (!filters?.includeRemoved) {
            mongoFilters.push(ne("isRemoved", true))
        }

        // Add role specific filters
        if (user.role == navimateforbusiness.Role.MANAGER) {
            // Objects should either be owned by user or publicly visible for a manager to view it
            mongoFilters.push(or(   eq("ownerId", user.id),
                                    eq("visibility", navimateforbusiness.Visibility.PUBLIC.name())))
        } else if (user.role == navimateforbusiness.Role.REP) {
            // Objects should either be owned by rep's manager or publicly visible for a rep to view it
            mongoFilters.push(or(   eq("ownerId", user.manager.id),
                                    eq("visibility", navimateforbusiness.Visibility.PUBLIC.name())))
        }

        // Apply ID filters if any
        if (filters?._ids) {
            def idFilters = []
            filters._ids.each {id -> idFilters.push(eq("_id", id))}
            mongoFilters.push(or(idFilters))
        }

        // Atleast sort the objects by name
        if (!sorter) {
            sorter = [[name: navimateforbusiness.Constants.Filter.SORT_ASC]]
        }

        // Apply date filter
        if (filters?.createTime?.from)  {mongoFilters.push(gte("createTime", "$filters.createTime.from"))}
        if (filters?.createTime?.to)    {mongoFilters.push(lte("createTime", "$filters.createTime.to"))}
        if (filters?.updateTime?.from)  {mongoFilters.push(gte("updateTime", "$filters.updateTime.from"))}
        if (filters?.updateTime?.to)    {mongoFilters.push(lte("updateTime", "$filters.updateTime.to"))}

        // Apply Ext ID filters if any
        if (filters?.extId) {mongoFilters.push(eq("extId", filters.extId))}

        // Apply Name filters if any
        if (filters?.name?.equal) {mongoFilters.push(eq("name", "$filters.name.equal"))}
        if (filters?.name?.value) {mongoFilters.push(regex("name", /.*$filters.name.value.*/, 'i'))}

        // Apply address / location filter
        if (filters?.address?.value) {mongoFilters.push(regex("address", /.*$filters.address.value.*/, 'i'))}
        if (filters?.location?.bNoBlanks) {mongoFilters.push(and(ne("latitude", 0), ne("longitude", 0)))}

        // Apply Template Filter
        if (filters?.template?.value) {
            def templates = templateService.getForUserByType(user, navimateforbusiness.Constants.Template.TYPE_LEAD)
            def templateFilters = []
            templates.each {it ->
                if (it.name.toLowerCase().contains(filters.template.value.toLowerCase())) {
                    templateFilters.push(eq("templateId", it.id))
                }
            }
            if (templateFilters) {
                mongoFilters.push(or(templateFilters))
            } else {
                mongoFilters.push(eq("templateId", null))
            }
        }

        // Get all fields present in filters
        def templates = templateService.getForUserByType(user, navimateforbusiness.Constants.Template.TYPE_LEAD)
        def fields = []
        templates.each {template -> fields.addAll(fieldService.getForTemplate(template))}
        fields.each {field ->
            def key = "$field.id"
            def filter = filters[key]

            // Ignore if filter not found
            if (!filter) {
                return
            }

            def value = filter.value
            Boolean bNoBlanks = filter.bNoBlanks ?: false

            switch (field.type) {
                case navimateforbusiness.Constants.Template.FIELD_TYPE_TEXT:
                    if (value) {
                        mongoFilters.push(regex("$key", /.*$value.*/, 'i'))
                    }
                    break
                case navimateforbusiness.Constants.Template.FIELD_TYPE_RADIOLIST:
                    if (value) {
                        // Get list of option indexes in field that contain the filter value
                        def json = JSON.parse(field.value)
                        def idxFilters = []
                        json.options.eachWithIndex {it, i ->
                            if (it.toLowerCase().contains(value.toLowerCase())) {
                                idxFilters.push(regex("$key", /.*\"selection\":$i.*/))
                            }
                        }
                        if (idxFilters) {
                            mongoFilters.push(or(idxFilters))
                        } else {
                            mongoFilters.push(eq("$key", null))
                        }
                    }
                    break
                case navimateforbusiness.Constants.Template.FIELD_TYPE_CHECKLIST:
                    if (value) {
                        // Get list of option indexes in field that contain the filter value
                        def json = JSON.parse(field.value)
                        def optFilters = []
                        json.eachWithIndex {it, i ->
                            if (it.name.toLowerCase().contains(value.toLowerCase())) {
                                optFilters.push(regex("$key", /.*\"name\":\"$it.name\",\"selection\":true.*/))
                            }
                        }
                        if (optFilters) {
                            mongoFilters.push(or(optFilters))
                        } else {
                            mongoFilters.push(eq("$key", null))
                        }
                    }
                    break
                case navimateforbusiness.Constants.Template.FIELD_TYPE_CHECKBOX:
                    if (value) {
                        if ("yes".contains(value.toLowerCase())) {
                            mongoFilters.push(eq("$key", "true"))
                        } else if ("no".contains(value.toLowerCase())) {
                            mongoFilters.push(eq("$key", "false"))
                        }
                    }
                    break
                case navimateforbusiness.Constants.Template.FIELD_TYPE_NUMBER:
                case navimateforbusiness.Constants.Template.FIELD_TYPE_DATE:
                    if (value.from) {
                        mongoFilters.push(gte("$key", "$value.from"))
                    }
                    if (value.to) {
                        mongoFilters.push(lte("$key", "$value.to"))
                    }
                    break
            }
            if (bNoBlanks) {
                mongoFilters.push(ne("$key", null))
            }
        }

        // Get results
        FindIterable fi = LeadM.find(and(mongoFilters))
        int rowCount = fi.size()

        // Apply Sorting
        if (sorter) {
            def sortBson = [:]
            sorter.each {sortObj ->
                def key = sortObj.keySet()[0]
                sortBson[key] = sortObj[key]
            }
            fi = fi.sort(sortBson)
        }

        // Apply paging
        if (pager.startIdx) {fi = fi.skip(pager.startIdx)}
        if (pager.count) {fi = fi.limit(pager.count)}

        // Prepare leads array to return
        def leads = []
        fi.each {LeadM lead -> leads.push(lead)}

        // Return response
        return [
                rowCount: rowCount,
                leads: leads
        ]
    }

    // Method to get lead for user by id
    def getForUserById(User user, String id) {
        def leads = getForUserByFilter(user, [_ids: [id]], [:], []).leads

        if (leads) {
            return leads[0]
        }
        return null
    }

    // Method to get lead for user by name
    def getForUserByName(User user, String name) {
        def leads = getForUserByFilter(user, [name: [equal: name]], [:], []).leads

        if (leads) {
            return leads[0]
        }
        return null
    }

    // Method to get lead for user by id
    def getForUserByExtId(User user, String extId) {
        def leads = getForUserByFilter(user, [extId: extId], [:], []).leads

        if (leads) {
            return leads[0]
        }
        return null
    }

    // Method to get through last updated
    def getForUserAfterLastUpdated(User user, Date date) {
        def leads = getForUserByFilter(user, [includeRemoved: true,
                                              updateTime: [from: date.format(navimateforbusiness.Constants.Date.FORMAT_BACKEND,
                                                                             navimateforbusiness.Constants.Date.TIMEZONE_IST)]], [:], []).leads
        leads
    }

    // ----------------------- Public APIs ---------------------------//
    // Methods to convert lead objects to / from JSON
    def toJson(LeadM lead, User user) {
        // Convert template properties to JSON
        def json = [
                id:         lead.id,
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
            lead = getForUserById(user, json.id)
            if (!lead) {
                throw new navimateforbusiness.ApiException("Illegal access to lead", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
            }
        } else if (json.extId) {
            lead = getForUserByExtId(user, json.extId)
        }

        // Create new lead object if not found
        if (!lead) {
            lead = new LeadM(
                    accountId: user.account.id,
                    ownerId: user.id,
                    visibility: navimateforbusiness.Visibility.PRIVATE,
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
                lead.latitude = latlngs[0].latitude
                lead.longitude = latlngs[0].longitude
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
            lead["$field.id"] = json.values.find {it -> it.fieldId == field.id}.value
        }

        // Add date info
        String currentTime = new Date().format(navimateforbusiness.Constants.Date.FORMAT_BACKEND, navimateforbusiness.Constants.Date.TIMEZONE_IST)
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
            throw new navimateforbusiness.ApiException("No rows to export", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        } else if (!params.columns) {
            throw new navimateforbusiness.ApiException("No columns to export", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
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

                    // Parse special values as per column type
                    switch (column.type) {
                        case navimateforbusiness.Constants.Template.FIELD_TYPE_PHOTO:
                        case navimateforbusiness.Constants.Template.FIELD_TYPE_SIGN:
                            if (value) {
                                value = "https://biz.navimateapp.com/#/photos?name=" + value
                            }
                            break
                        case navimateforbusiness.Constants.Template.FIELD_TYPE_CHECKBOX:
                            value = value ? "yes" : "no"
                            break
                        case navimateforbusiness.Constants.Template.FIELD_TYPE_DATE:
                            SimpleDateFormat sdf = new SimpleDateFormat(navimateforbusiness.Constants.Date.FORMAT_BACKEND)
                            value = value ? sdf.parse(value).format(navimateforbusiness.Constants.Date.FORMAT_FRONTEND) : ""
                            break
                        case navimateforbusiness.Constants.Template.FIELD_TYPE_RADIOLIST:
                            if (value) {
                                def valueJson = JSON.parse(value)
                                value = valueJson.options[valueJson.selection]
                            }
                            break
                        case navimateforbusiness.Constants.Template.FIELD_TYPE_CHECKLIST:
                            if (value) {
                                def valueJson = JSON.parse(value)
                                value = ""
                                valueJson.each {option ->
                                    if (option.selection) {
                                        if (value) {
                                            value += ", " + option.name
                                        } else {
                                            value = option.name
                                        }
                                    }
                                }
                            }
                            break
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
        def tasks = taskService.getForUserByLead(user, lead)
        tasks.each {Task task ->
            taskService.remove(user, task)
        }

        // Remove lead
        lead.isRemoved = true
        lead.updateTime = new Date().format(navimateforbusiness.Constants.Date.FORMAT_BACKEND, navimateforbusiness.Constants.Date.TIMEZONE_IST)
        lead.save(failOnError: true, flush: true)
    }

    // Method to get FCMs associated with the lead
    def getAffectedReps (User user, LeadM lead) {
        def reps = []

        // Get reps from all open tasks with this lead
        def tasks = taskService.getForUserByLead(user, lead)
        def openTasks = tasks.findAll {Task it -> it.status == navimateforbusiness.TaskStatus.OPEN}
        openTasks.each {Task task ->
            if (task.rep) {reps.push(task.rep)}
        }

        reps
    }

    // ----------------------- Private APIs ---------------------------//
}
