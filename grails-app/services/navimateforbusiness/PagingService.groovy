package navimateforbusiness

import grails.gorm.transactions.Transactional

@Transactional
class PagingService {
    // ----------------------- Dependencies ---------------------------//
    // ----------------------- Public APIs ---------------------------//
    def apply (objects, pager) {
        def pagedObjects = []

        // Get number of objects to be returned
        def count = Math.min(pager.count, objects.size() - pager.startIdx)

        // Populate array with paged objects
        for (int i = 0; i < count; i++) {
            pagedObjects.push(objects[i + pager.startIdx])
        }

        pagedObjects
    }

    // ----------------------- Private APIs ---------------------------//
}
