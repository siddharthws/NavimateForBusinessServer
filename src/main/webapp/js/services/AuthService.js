/**
 * Created by Siddharth on 23-08-2017.
 */

app.service('AuthService', function($q, $http, $localStorage) {

    this.validateRegistration = function (regInfo)
    {
        // Create deferred object
        var deferred = $q.defer()

        $http({
            method:     'POST',
            url:        '/api/auth/validateRegistration',
            data:       {
                name:           regInfo.name,
                email:          regInfo.email,
                password:       regInfo.password,
                role:           regInfo.role.id,
                companyName:    regInfo.companyName
            }
        }).then(
            function (response) {
                // Resolve Promise
                deferred.resolve(response.data.otp)
            },
            function (error) {
                // Reject promise
                deferred.reject(error.data.error)
            }
        )

        // Return promise
        return deferred.promise
    }

    this.register = function (regInfo)
    {
        // Create deferred object
        var deferred = $q.defer()

        $http({
            method:     'POST',
            url:        '/api/auth/register',
            data:       {
                name:           regInfo.name,
                email:          regInfo.email,
                password:       regInfo.password,
                role:           regInfo.role.id,
                companyName:    regInfo.companyName
            }
        }).then(
            function (response) {
                // Resolve Promise
                deferred.resolve()
            },
            function (error) {
                // Reject promise
                deferred.reject(error.data.error)
            }
        )

        // Return promise
        return deferred.promise
    }

    this.login = function (email, password)
    {
        // Create deferred object
        var deferred = $q.defer()

        $http({
            method:     'POST',
            url:        '/api/auth/login',
            data:       {
                email:          email,
                password:       password
            }
        }).then(
            function (response) {
                // Save information in local storage
                saveLoginInfo(response)

                // Resolve Promise
                deferred.resolve()
            },
            function (error) {
                // Reject promise
                deferred.reject(error.data.error)
            }
        )

        // Return promise
        return deferred.promise
    }

    this.forgotPassword = function (email) {
        // Create deferred object
        var deferred = $q.defer()

        $http({
            method:     'POST',
            url:        '/api/auth/forgotPassword',
            data:       {
                email:    email
            }
        }).then(
            function (response) {
                deferred.resolve()
            },
            function (error) {
                deferred.reject(error.data.error)
            }
        )

        // Return promise
        return deferred.promise
    }

    this.logout = function ()
    {
        return $http({
            method:     'GET',
            url:        '/api/auth/logout',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            }
        })
    }

    function saveLoginInfo(response) {
        // Save access token
        $localStorage.accessToken = response.data.accessToken

        // Save user information
        $localStorage.id    = response.data.id
        $localStorage.name  = response.data.name
        $localStorage.role  = response.data.role

        // Save Company Information
        $localStorage.companyName = response.data.companyName

        // Save Admin Specific Information
        if ($localStorage.role == Constants.Role.ADMIN) {
            $localStorage.apiKey = response.data.apiKey
            $localStorage.startHr = response.data.startHr
            $localStorage.endHr = response.data.endHr
        }
    }
})
