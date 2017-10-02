package navimateforbusiness

class BootStrap {

    def init = { servletContext ->
        //initFakeDb()
    }
    def destroy = {
    }

    def initFakeDb() {
        // Init Account
        Account account = new Account(  name: 'fake company pvt. ltd.')
        account.save()

        // Init Admin / Manager
        User manUser = new User(    name:           'Fake Manager',
                                    phoneNumber:    '+919999997777',
                                    password:       'fakepass',
                                    account:        account,
                                    role:           navimateforbusiness.Role.ADMIN,
                                    status:         navimateforbusiness.UserStatus.ACTIVE)
        manUser.save(flush: true, failOnError: true)

        // Init Rep
        User repUser = new User(    name:           'Fake Rep',
                                    phoneNumber:    '+919999996666',
                                    account:        account,
                                    manager:        manUser,
                                    role:           navimateforbusiness.Role.REP,
                                    status:         navimateforbusiness.UserStatus.ACTIVE)
        repUser.save(flush: true, failOnError: true)


        // Init Fake Lead
        Lead lead = new Lead(       title:    'Fake Lead Company 1',
                                    description:       'Fake Lead 1',
                                    phone:      '+919999995555',
                                    account:    account,
                                    manager:    manUser,
                                    address:    'Fake Address, Fake Street, Fake city',
                                    latitude:   18.765234,
                                    longitude:  73.734556)
        lead.save()

        // Init Form
        Form form = new Form(       name:       "FAKE_FORM_TEMPLATE",
                                    account:    account,
                                    owner:      manUser,
                                    data:       "{ [ {'type' : 1, 'title' : 'notes', 'data' : 'default notes'}, {'type' : 2, 'title' : 'sales', 'data' : 0} ] }")
        form.save()

        // Init Task
        Task task = new Task(       account:    account,
                                    manager:    manUser,
                                    rep:        repUser,
                                    lead:       lead,
                                    template:   form)
        task.save()
    }
}
