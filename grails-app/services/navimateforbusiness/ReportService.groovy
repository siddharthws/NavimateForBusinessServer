package navimateforbusiness

import grails.gorm.transactions.Transactional
import navimateforbusiness.objects.LatLng

@Transactional
class ReportService {
    def googleApiService

    def getLocationReport(User rep, Date date) {
        def report = [points: [], distance: 0]

        // Get report for this user on this date
        def locReport = LocReport.findByAccountAndOwnerAndSubmitDate(rep.account, rep, date)
        if (!locReport) {
            throw new navimateforbusiness.ApiException("Report not found", navimateforbusiness.Constants.HttpCodes.BAD_REQUEST)
        }

        // Get snapped latlngs for the report
        def snappedPoints = getRoadSnappedPoints(locReport)

        // Get all submissions in this report
        def submissions = LocSubmission.findAllByAccountAndReport(rep.account, locReport).sort {it.submitDate}

        // Iterate through each snapped point to create report for frontend
        int status = navimateforbusiness.Constants.Tracking.ERROR_NONE
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
                reportObjJson.time = navimateforbusiness.Constants.Formatters.TIME_SHORT.format(submission.submitDate)
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
        def noRoadSubmission = validSubmissions.findAll {!it.roadsIdx}
        if (noRoadSubmission) {
            // Collect all LatLngs
            def latlngs = validSubmissions.collect {new LatLng(it.latlngString)}

            // Get road snapped results from google
            def googleRoadsResp = googleApiService.snapToRoads(latlngs)

            // Update road indexes for all submissions
            validSubmissions.eachWithIndex { LocSubmission it, int i ->
                def roadsResult = googleRoadsResp.find {i == it.originalIndex}
                if (roadsResult) {
                    it.roadsIdx = googleRoadsResp.indexOf(roadsResult)
                } else {
                    it.roadsIdx = -1
                }

                it.save(flush: true, failOnError: true)
            }

            // Update encoded polyline in report
            report.encPolyline = encodePolyline(googleRoadsResp)

            // Update total distance travelled
            def roadPoints = googleRoadsResp.collect {new LatLng(it.location.latitude, it.location.longitude)}
            long distanceM = navimateforbusiness.Constants.getLatlngListDistance(roadPoints)
            report.distance = distanceM / 1000

            report.save(flush: true, failOnError: true)
        }

        // Return decoded polyline for report
        if (report.encPolyline) {
            return decodePolyline(report.encPolyline)
        } else {
            return []
        }
    }

    def saveLocationReport(User rep, def reportJson) {
        // Iterate through report
        reportJson.each {rowJson ->
            // Get submission date
            Date submitDate = new Date(rowJson.timestamp)

            // Get date only portion
            Date submitDateOnly = new Date(rowJson.timestamp)
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

    /**
     * Decodes an encoded path string into a sequence of LatLngs.
     */
    def decodePolyline(String encodedPath) {
        int len = encodedPath.length();

        // For speed we preallocate to an upper bound on the final length, then
        // truncate the array before returning.
        def path = []
        int index = 0;
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int result = 1;
            int shift = 0;
            int b;
            while ({
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
                b >= 0x1f
            }());
            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            result = 1;
            shift = 0;
            while ({
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
                b >= 0x1f
            }());
            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            path.add(new LatLng(lat * 1e-5, lng * 1e-5));
        }

        return path;
    }

    /**
     * Encodes a sequence of LatLngs into an encoded path string.
     */
    def encodePolyline(def path) {
        long lastLat = 0;
        long lastLng = 0;

        final StringBuffer result = new StringBuffer();

        for (def point : path) {
            long lat = Math.round(point.location.latitude * 1e5);
            long lng = Math.round(point.location.longitude * 1e5);

            long dLat = lat - lastLat;
            long dLng = lng - lastLng;

            encode(dLat, result);
            encode(dLng, result);

            lastLat = lat;
            lastLng = lng;
        }
        return result.toString();
    }

    void encode(long v, StringBuffer result) {
        v = v < 0 ? ~(v << 1) : v << 1;
        while (v >= 0x20) {
            result.append(Character.toChars((int) ((0x20 | (v & 0x1f)) + 63)));
            v >>= 5;
        }
        result.append(Character.toChars((int) (v + 63)));
    }
}
