package navimateforbusiness.api

import com.amazonaws.services.s3.model.ObjectMetadata
import grails.converters.JSON
import navimateforbusiness.enums.Role
import navimateforbusiness.util.ApiException
import navimateforbusiness.util.Constants
import navimateforbusiness.User
import navimateforbusiness.Account
import grails.io.IOUtils

class PhotoApiController {

    def amazonS3Service
    def authService

    def get() {
        if (!authService.authenticate(request.getHeader("X-Auth-Token"))) {
            // Authenticate Rep
            authenticate()
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
        response.setHeader("Content-disposition", "attachment; filename=" + filename)
        response.contentType = "application/octet-stream"

        // Send file as response
        imageFile.withInputStream { response.outputStream << it }
        imageFile.delete()
    }

    def getFile() {
        authenticate()

        // Get filename
        String filename = request.JSON.filename
        if (!filename) {
            throw new ApiException("Filename not found...", Constants.HttpCodes.BAD_REQUEST)
        }

        // Check if file exists
        if (!amazonS3Service.exists(filename)) {
            throw new ApiException("Invalid Filename : " + filename, Constants.HttpCodes.BAD_REQUEST)
        }

        // Get File from S3
        File file = amazonS3Service.getFile(filename, filename)
        if (!file || !file.length()) {
            throw new ApiException("Invalid File..." + filename, Constants.HttpCodes.BAD_REQUEST)
        }

        // Set response parameters
        response.setHeader("Content-disposition", "attachment; filename=" + filename)
        response.contentType = "application/octet-stream"

        // Send file as response
        file.withInputStream { response.outputStream << it }
        file.delete()
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

    // APi to upload photo form app
    def uploadFile() {
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
        meta.setContentType("*/*")
        amazonS3Service.storeInputStream(filename, bis, meta)

        // Send filename in response
        def resp=  [success: true]
        render resp as JSON
    }

    def uploadCompanyIcon(){
        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))

        if (!user) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }

        // Get file
        def image = request.getFile('image')
        // Get input stream
        InputStream is = image.getInputStream()
        // Generate  UUID
        def filename = UUID.randomUUID().toString()

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

        // Attach photo name to the account
        Account account = Account.findByAdmin(user)
        account.photoName = filename
        account.save(failOnError: true, flush: true)

        // Send response
        def resp=  [success: true]
        render resp as JSON
    }

    def getCompanyIcon() {
        // Authenticate Request
        if (!authService.authenticate(request.getHeader("X-Auth-Token"))) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }

        // Get user object
        def user = authService.getUserFromAccessToken(request.getHeader("X-Auth-Token"))
        Account account = Account.findByAdmin(user)

        // Get filename
        String filename = account.photoName

        if (!filename) {
            throw new ApiException("Image not found...", Constants.HttpCodes.BAD_REQUEST)
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
        response.setHeader("Content-disposition", "attachment; filename=" + filename)
        response.contentType = "application/octet-stream"

        // Send file as response
        imageFile.withInputStream { response.outputStream << it }
        imageFile.delete()
    }

    private def authenticate() {
        // Get id from request
        long id = 0L
        try {
            id = Long.parseLong(request.getHeader("id"))
        } catch (Exception e) {
            id = 0
        }

        // Find representative with this ID
        User rep = User.findByRoleAndId(Role.REP, id)
        if (!rep) {
            throw new ApiException("Unauthorized", Constants.HttpCodes.UNAUTHORIZED)
        }

        rep
    }
}
