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
        // Get input array form path
        def inputArr = getInputArr(path)

        // Get google response for each input element
        def respArr = getPathFromGoogle(inputArr)

        // Parse input and response to a list of points with original indexes
        def smoothPath = parseToSmoothPath(path, inputArr, respArr)

        smoothPath
    }

    private def getInputArr(List<LatLng> path) {
        def inputArr = []

        // Create input array as per point distances in path
        for (int i  = 0; i < path.size() - 1; i++) {
            // Get distance between this and next point
            int distance = Constants.getDistanceBetweenCoordinates(path[i], path[i+1])

            if(distance <= 500) {
                // Get the last added input
                def lastAddedInput = inputArr ? inputArr.last() : null

                // Add new if last one is invalid
                if (!lastAddedInput || lastAddedInput.type == "directions") {
                    lastAddedInput = [type: "roads", points: []]
                    inputArr.push(lastAddedInput)
                }

                // Add this point to input
                lastAddedInput.points.push(path[i])

                // Add next point if it is the last point
                if (i + 1 == path.size() - 1) {
                    lastAddedInput.points.push(path[i + 1])
                }
            } else {
                // Add as directions input
                inputArr.push([type: "directions", start: path[i], end: path[i+1]])
            }
        }

        inputArr
    }

    private def getPathFromGoogle(def arr) {
        def respArr = []

        arr.each {def entry ->
            if (entry.type == "directions") {
                // Get response form directions API
                def resp = googleApiService.directions(entry.start, entry.end)
                respArr.push(resp)
            } else {
                // Get response from roads API
                def resp = googleApiService.snapToRoads(entry.points)
                respArr.push(resp)
            }
        }

        respArr
    }

    private def parseToSmoothPath(def origList, def inArr, def outArr) {
        def smoothPath = []

        // Thorw exception if input and output array are of different sizes
        if (inArr.size() != outArr.size()) {
            throw new ApiException("Illegal input and output for getting path", Constants.HttpCodes.INTERNAL_SERVER_ERROR)
        }

        for (int i = 0; i < inArr.size(); i++) {
            // Get inptu and output elements
            def inElem = inArr[i]
            def outElem = outArr[i]

            // Check type of element
            if (inElem.type == "directions") {
                // Add start point to array
                smoothPath.push([point: outElem.points.first(), origIdx: origList.indexOf(inElem.start)])

                // Push all points between first and last
                outElem.points.subList(1, outElem.points.size() - 2).each {smoothPath.push([point: it])}

                // Add endpoint to array
                smoothPath.push([point: outElem.points.last(), origIdx: origList.indexOf(inElem.end)])
            } else {
                // Add all points to smooth path with correct indexes
                int idxOffset = origList.indexOf(outElem.points[0])
                outElem.points.findAll {it.origIdx}.each {it.origIdx += idxOffset}

                // Add all points to smooth points
                smoothPath.addAll(outElem.points)
            }
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
