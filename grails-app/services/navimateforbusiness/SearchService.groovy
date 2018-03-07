package navimateforbusiness

import grails.gorm.transactions.Transactional

@Transactional
class SearchService {
    // ----------------------- Dependencies ---------------------------//
    // ----------------------- Public APIs ---------------------------//
    def searchLeads (List<Lead> leads, String text) {
        def searchedObjects = []

        // Iterate through leads
        leads.each {lead ->
            // Search title
            if (lead.title.toLowerCase().contains(text.toLowerCase())) {
                searchedObjects.push(lead)
            }
        }

        searchedObjects
    }

    // ----------------------- Private APIs ---------------------------//
}
