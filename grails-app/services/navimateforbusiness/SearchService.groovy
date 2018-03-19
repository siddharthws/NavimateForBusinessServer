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
            if (lead.name.toLowerCase().contains(text.toLowerCase())) {
                searchedObjects.push(lead)
            }
        }

        searchedObjects
    }

    // Method to search in user objects
    def searchUsers (List<User> team, String text) {
        def searchedObjects = []

        // Iterate through leads
        team.each {user ->
            // Search title
            if (user.name.toLowerCase().contains(text.toLowerCase())) {
                searchedObjects.push(user)
            }
        }

        searchedObjects
    }

    // ----------------------- Private APIs ---------------------------//
}
