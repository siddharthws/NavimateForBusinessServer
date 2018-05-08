package navimateforbusiness.api

import com.amazonaws.services.s3.model.ObjectMetadata
import grails.converters.JSON
import grails.io.IOUtils
import navimateforbusiness.ApiException
import navimateforbusiness.Constants
import navimateforbusiness.User

class PhotoApiController {

    def amazonS3Service
    def authService

    def get() {
        if (!authService.authenticate(request.getHeader("X-Auth-Token"))) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }

        // Get filename
        String filename = params.filename
        if (!filename) {
            throw new ApiException("Filename not found...", Constants.HttpCodes.BAD_REQUEST)
        }

        // Check if file exists
        if (!amazonS3Service.exists(filename)) {
            throw new ApiException("Invalid Filename : " + filename, Constants.HttpCodes.BAD_REQUEST)
        }

        // Get File from S3
        File imageFile = amazonS3Service.getFile(filename, filename)
        if (!imageFile || !imageFile.length()) {
            throw new ApiException("Invalid File..." + filename, Constants.HttpCodes.BAD_REQUEST)
        }

        // Set response parameters
        response.setHeader("Content-disposition", "attachment; filename=photo.jpg")
        response.contentType = grailsApplication.config.getProperty("grails.mime.types.jpg")

        // Send file as response
        imageFile.withInputStream { response.outputStream << it }
        imageFile.delete()
    }

    // APi to upload photo form app
    def upload() {
        // Check app's ID
        def id = request.getHeader("id")
        User rep = User.findById(id)
        if (!rep) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }

        // Convert multipart data to byte array
        def multipartData = params.uploadedFile
        String filename = multipartData.originalFilename
        InputStream is = multipartData.getInputStream()
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        byte[] buffer = new byte[1024]
        int bytesRead = is.read(buffer)
        while (bytesRead > 0) {
            baos.write(buffer, 0, bytesRead)
            bytesRead = is.read(buffer)
        }
        def ba = baos.toByteArray()

        // Upload to S3 with a UUID filename
        ByteArrayInputStream bis = new ByteArrayInputStream(ba)
        ObjectMetadata meta = new ObjectMetadata()
        meta.setContentLength(ba.length)
        meta.setContentType("image/jpg")
        amazonS3Service.storeInputStream(filename, bis, meta)

        // Send filename in response
        def resp=  [success: true]
        render resp as JSON
    }
}
