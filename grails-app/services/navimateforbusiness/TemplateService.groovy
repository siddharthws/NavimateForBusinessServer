package navimateforbusiness

import grails.gorm.transactions.Transactional

@Transactional
class TemplateService {
    // ----------------------- Dependencies ---------------------------//
    // Method to get all templates for a user
    def getForUser(User user, int type) {
        // Get all unremoved templates for user's account and given type
        def templates = Template.findAllByAccountAndIsRemovedAndType(user.account, false, type)

        // Sort templates in order of IDs
        templates = templates.sort {it -> it.id}

        // Return sorted templates
        return templates
    }

    // ----------------------- Public APIs ---------------------------//
    // ----------------------- Private APIs ---------------------------//
}
