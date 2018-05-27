package navimateforbusiness

import grails.gorm.transactions.Transactional
import navimateforbusiness.util.ApiException
import navimateforbusiness.util.Constants

@Transactional
class PagingService {
    // ----------------------- Dependencies ---------------------------//
    // ----------------------- Public APIs ---------------------------//
    def apply (objects, pager) {
        def pagedObjects = []

        // Get number of objects to be returned
        def count = Math.min(pager.count, objects.size() - pager.startIdx)

        // Validate count
        if (count < 0) {
            throw new ApiException("Invalid pager", Constants.HttpCodes.BAD_REQUEST)
        }

        // Populate array with paged objects
        for (int i = 0; i < count; i++) {
            pagedObjects.push(objects[i + pager.startIdx])
        }

        pagedObjects
    }

    // ----------------------- Private APIs ---------------------------//
}
