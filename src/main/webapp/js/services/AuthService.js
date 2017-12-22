/**
 * Created by Siddharth on 23-08-2017.
 */

app.service('AuthService', function($http, $localStorage) {

    this.register = function (name, email, password, role, companyName)
    {
        return $http({
            method:     'POST',
            url:        '/api/auth/register',
            data:       {
                name:           name,
                email:          email,
                password:       password,
                role:           role.toUpperCase(),
                companyName:    companyName
            }
        })
    }

    this.login = function (email, password)
    {
        return $http({
            method:     'POST',
            url:        '/api/auth/login',
            data:       {
                email:          email,
                password:       password
            }
        })
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

    this.emailOtp = function (otp, email)
    {
        return $http({
            method:     'POST',
            url:        '/api/auth/email',
            headers:    {
                'X-Auth-Token':    $localStorage.accessToken
            },
            data:       {
                otp:          otp,
                email:        email
            }
        })
    }
})
