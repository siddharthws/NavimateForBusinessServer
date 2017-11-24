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

        // Convert to bytes & delete the file
        byte[] ba = IOUtils.copyToByteArray(imageFile)
        imageFile.delete()

        // Send bytes in response
        def resp = [image: ba]
        render resp as JSON
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
        String filename = UUID.randomUUID().toString() + ".jpg"
        ByteArrayInputStream bis = new ByteArrayInputStream(ba)
        ObjectMetadata meta = new ObjectMetadata()
        meta.setContentLength(ba.length)
        meta.setContentType("image/jpg")
        amazonS3Service.storeInputStream(filename, bis, meta)

        // Send filename in response
        def resp=  [filename: filename]
        render resp as JSON
    }
}
