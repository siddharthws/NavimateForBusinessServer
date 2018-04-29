/**
 * Created by Siddharth on 28-04-2018.
 */

app.controller("DashboardLoadingCtrl",
    function ($rootScope, $localStorage, $state,
              ToastService, TemplateService, TeamService) {
        /*------------------------------------ INIT --------------------------------*/
        var vm = this

        // Error Flag
        var bError = false

        // Syncing flags for each individual item that needs to be synced
        var bTemplateSync = false, bManagerSync = false

        /*------------------------------------ Private methods --------------------------------*/
        function syncCb() {
            // Check for error
            if (!bError) {
                // Check for each sync complete
                if (bTemplateSync && bManagerSync) {
                    // Set App Loaded Flag
                    $rootScope.bAppLoaded = true

                    // Open dashboard with required state
                    if ($localStorage.lastKnownState) {
                        $state.go($localStorage.lastKnownState)
                    } else {
                        $state.go('dashboard.team-manage')
                    }
                }
            }
        }

        function errorCb() {
            // Set error flag
            bError = true

            // Show error toast
            ToastService.toast("Unable to load data !!!")

            // Go to login screen
            $state.go('login')
        }
        /*------------------------------------ Post INIT --------------------------------*/
        // Sync Templates
        TemplateService.sync().then(
            // Success
            function () {
                bTemplateSync = true
                syncCb()
            },
            // Error
            function () {
                errorCb()
            }
        )

        // Sync managers for admin
        if ($localStorage.role >= Constants.Role.CC) {
            TeamService.syncManagers().then(
                function () {
                    // Set sync flag
                    bManagerSync = true
                    syncCb()
                },
                function () {
                    errorCb()
                }
            )
        } else {
            // Set sync flag
            bManagerSync = true
        }

    })
