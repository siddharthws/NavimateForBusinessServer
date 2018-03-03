package navimateforbusiness

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import org.grails.web.json.JSONArray

@Transactional
class ReportService {
    static String FORMAT_DATE = "yyyy-MM-dd"
    static String FORMAT_TIME = "HH:mm:ss"
    static String FORMAT_TIME_12_HR = "hh:mm a"
    static TimeZone IST = TimeZone.getTimeZone('Asia/Calcutta')

    def getLocationReport(User rep, String date) {
        def report = []

        // Get all report data submitted by this rep
        def locReport = LocationReport.findByAccountAndOwner(rep.account, rep)

        // Get all report objects submitted on given date
        locReport = locReport.findAll {it -> (date == it.dateSubmitted.format(FORMAT_DATE, IST))}

        // Sort items in increasing order of time
        locReport = locReport.sort {it -> it.dateSubmitted.format(FORMAT_TIME, IST)}

        // Iterate through each report item
        locReport.each {row ->
            // Prepare JSON for row
            def rowJson = [
                    time: row.dateSubmitted.format(FORMAT_TIME_12_HR, IST),
                    latitude: row.latitude,
                    longitude: row.longitude,
                    status: row.status
            ]

            // Add to report
            report.push(rowJson)
        }

        // Send report
        report
    }

    def saveLocationReport(User rep, def reportJson) {
        // Iterate through report
        reportJson.each {rowJson ->
            // Create report object
            LocationReport locReport = new LocationReport(
                    account: rep.account,
                    owner: rep,
                    latitude: rowJson.latitude ? rowJson.latitude : 0,
                    longitude: rowJson.longitude ? rowJson.longitude : 0,
                    dateSubmitted: new Date((long) rowJson.timestamp),
                    status: rowJson.status
            )

            // Save to database
            locReport.save(flush: true, failOnError: true)
        }
    }
}
