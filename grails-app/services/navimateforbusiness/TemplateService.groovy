package navimateforbusiness

import grails.gorm.transactions.Transactional

@Transactional
class TemplateService {
    // ----------------------- Dependencies ---------------------------//
    // ----------------------- Getters ---------------------------//
    // Method to get all templates for a user
    def getForUser(User user, int type) {
        // Get all unremoved templates for user's account and given type
        def templates = Template.findAllByAccountAndIsRemovedAndType(user.account, false, type)

        // Sort templates in order of IDs
        templates = templates.sort {it -> it.id}

        // Return sorted templates
        return templates
    }

    // Method to get lead for user by id
    def getForUserById(User user, long id) {
        // Get lead by this id
        Template template = Template.findByAccountAndIsRemovedAndId(user.account, false, id)

        template
    }

    // Method to get lead for user by name
    def getForUserByName(User user, String name, int type) {
        // Get template by this name and type
        Template template = Template.findByAccountAndIsRemovedAndTypeAndName(user.account, false, type, name)

        template
    }

    // ----------------------- Public APIs ---------------------------//
    // ----------------------- Private APIs ---------------------------//
}
