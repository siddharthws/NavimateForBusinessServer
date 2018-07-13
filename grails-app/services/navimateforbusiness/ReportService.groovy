package navimateforbusiness

import grails.gorm.transactions.Transactional
import navimateforbusiness.objects.LatLng
import navimateforbusiness.util.ApiException
import navimateforbusiness.util.Constants

@Transactional
class ReportService {
    def googleApiService

    def getLocationReport(User rep, Date date) {
        def report = [points: [], distance: 0]

        // Get report for this user on this date
        def locReport = LocReport.findByAccountAndOwnerAndSubmitDate(rep.account, rep, date)
        if (!locReport) {
            throw new ApiException("Report not found", Constants.HttpCodes.BAD_REQUEST)
        }

        // Get snapped latlngs for the report
        def snappedPoints = getRoadSnappedPoints(locReport)

        // Get all submissions in this report
        def submissions = LocSubmission.findAllByAccountAndReport(rep.account, locReport).sort {it.submitDate}

        // Iterate through each snapped point to create report for frontend
        int status = Constants.Tracking.ERROR_NONE
        snappedPoints.eachWithIndex {LatLng point, i ->
            // Create a report element for response
            def reportObjJson = [
                    time:       "",
                    latitude:   point.lat,
                    longitude:  point.lng,
                    status:     status,
                    speed:      0.0f,
                    battery:    0
            ]

            // Check if this point has a submission associated with it
            def submission = submissions.find {it.roadsIdx == i}
            if (submission) {
                // Add time, speed, battery information information
                reportObjJson.time = Constants.Formatters.TIME_SHORT.format(submission.submitDate)
                reportObjJson.speed = submission.speed
                reportObjJson.battery = submission.battery

                // Reset status to the next available report object
                int submissionIndex = submissions.indexOf(submission)
                if (submissionIndex < submissions.size() - 1) {
                    LocSubmission nextSubmission = submissions[submissionIndex + 1]
                    status = nextSubmission.status
                }
            }

            // Add to report
            report.points.push(reportObjJson)
        }

        // Add travel distance
        report.distance = locReport.distance

        // Send report
        report
    }

    List<LatLng> getRoadSnappedPoints(LocReport report) {
        // Get all valid submissions from report
        def submissions = LocSubmission.findAllByAccountAndReport(report.account, report).sort {it.submitDate}
        def validSubmissions = submissions.findAll {new LatLng(it.latlngString).isValid()}

        // Check if all valid submissions have a valid road index
        def noRoadSubmission = validSubmissions.findAll {it.roadsIdx == -2}
        if (noRoadSubmission) { refreshLocReport(report) }

        // Return decoded polyline for report
        if (report.encPolyline) {
            return Constants.decodePolyline(report.encPolyline)
        } else {
            return []
        }
    }

    def saveLocationReport(User rep, def reportJson) {
        // Iterate through report
        reportJson.each {rowJson ->
            // Get submission date
            Date submitDate = Constants.Date.IST(new Date(rowJson.timestamp))

            // Get date only portion
            Date submitDateOnly = Constants.Date.IST(new Date(rowJson.timestamp))
            submitDateOnly.clearTime()

            // Check if location report exists for this date, otherwise create a new one
            def locReport = LocReport.findByAccountAndOwnerAndSubmitDate(rep.account, rep, submitDateOnly)
            if (!locReport) {
                locReport = new LocReport(account: rep.account, owner: rep, submitDate: submitDateOnly)
                locReport.save(failOnError: true, flush: true)
            }

            // Create a new Location Submission object
            LocSubmission submission = new LocSubmission(
                    account: rep.account,
                    report: locReport,
                    latlngString: new LatLng(rowJson.latitude, rowJson.longitude).toString(),
                    submitDate: submitDate,
                    status: rowJson.status,
                    speed: rowJson.speed,
                    battery: rowJson.battery)

            // Save submission to database
            submission.save(flush: true, failOnError: true)
        }
    }

    private def getSmoothPath(List<LatLng> path) {
        // Get roads path
        def roadsResp = googleApiService.snapToRoads(path)

        // Get point between which directions result is required
        def smoothPath = []
        for (int i  = 0; i < roadsResp.points.size() - 1; i++) {
            // Add this point in path
            smoothPath.push(roadsResp.points[i])

            // Get distance between this and next point
            LatLng currPoint = roadsResp.points[i].point
            LatLng nextPoint = roadsResp.points[i+1].point
            int distance = Constants.getDistanceBetweenCoordinates(currPoint, nextPoint)

            // Get result form directions API
            if (distance > 300) {
                def directions = googleApiService.directions(currPoint, nextPoint)
                int dirDistance = Constants.getLatlngListDistance(directions.points)

                // Ignore if too much difference in distance
                if (dirDistance < 5 * distance) {
                    for (int j = 1; j < directions.points.size() - 1; j++) {
                        smoothPath.push([point: directions.points[j]])
                    }
                }
            }
        }

        // Add last point in path
        if (roadsResp && roadsResp.points && roadsResp.points.size() > 1) {
            smoothPath.push(roadsResp.points.last())
        }

        smoothPath
    }

    def refreshLocReport(LocReport report) {
        // Get all latlngs from report
        def submissions = LocSubmission.findAllByAccountAndReport(report.account, report).sort {it.submitDate}
        def validSubmissions = submissions.findAll {new LatLng(it.latlngString).isValid()}
        def latlngs = validSubmissions.collect {new LatLng(it.latlngString)}

        // Convert LatLngs to a smooth Path
        def smoothPath = getSmoothPath(latlngs)

        // Update road indexes for all submissions
        validSubmissions.eachWithIndex { LocSubmission it, int i ->
            def roadsResult = smoothPath.find {i == it.origIdx}
            if (roadsResult) {
                it.roadsIdx = smoothPath.indexOf(roadsResult)
            } else {
                it.roadsIdx = -1
            }

            it.save(flush: true, failOnError: true)
        }

        // Update encoded polyline in report
        def smoothLatLngs = smoothPath.collect {it.point}
        report.encPolyline = Constants.encodePolyline(smoothLatLngs)

        // Update total distance travelled
        long distanceM = Constants.getLatlngListDistance(smoothLatLngs)
        report.distance = distanceM / 1000

        report.save(flush: true, failOnError: true)
    }
}
