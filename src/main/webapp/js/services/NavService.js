/**
 * Created by Siddharth on 27-03-2018.
 */

app.service('NavService', function(ObjMenu, ObjTab) {
    /* ----------------------------- INIT --------------------------------*/
    var vm = this

    // Create each menu item with its respective tab
    vm.team = new ObjMenu(
        "TEAM",
        "ic_team.png",
        Constants.Role.MANAGER,
        [
            new ObjTab("MANAGE", "team-manage", Constants.Role.MANAGER)
        ]
    )

    vm.leads = new ObjMenu(
        "LEADS",
        "ic_lead.png",
        Constants.Role.MANAGER,
        [
            new ObjTab("MANAGE", "leads-manage", Constants.Role.MANAGER)
        ]
    )

    vm.tasks = new ObjMenu(
        "TASKS",
        "ic_briefcase.png",
        Constants.Role.MANAGER,
        [
            new ObjTab("MANAGE", "tasks-manage", Constants.Role.MANAGER)
        ]
    )

    vm.reports = new ObjMenu(
        "REPORTS",
        "ic_report.png",
        Constants.Role.MANAGER,
        [
            new ObjTab("SUBMISSIONS", "reports-submission", Constants.Role.MANAGER),
            //new ObjTab("MOVEMENT", "reports-movement", Constants.Role.MANAGER)
        ]
    )

    vm.templates = new ObjMenu(
        "TEMPLATES",
        "ic_template.png",
        Constants.Role.ADMIN,
        [
            new ObjTab("FORM", "templates-form", Constants.Role.ADMIN),
            new ObjTab("LEAD", "templates-lead", Constants.Role.ADMIN),
            new ObjTab("TASK", "templates-task", Constants.Role.ADMIN)
        ]
    )

    vm.company = new ObjMenu(
        "COMPANY",
        "ic_company.png",
        Constants.Role.ADMIN,
        [
            new ObjTab("SETTINGS", "company-settings", Constants.Role.ADMIN)
        ]
    )

    // Set active menu item
    vm.activeMenu = vm.team

    /* ----------------------------- APIs --------------------------------*/

})