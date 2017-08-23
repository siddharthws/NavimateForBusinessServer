/**
 * Created by Siddharth on 23-08-2017.
 */

app.service('AuthService', function($http, $localStorage) {

    this.register = function (name, phoneNumber, password)
    {
        return $http({
            method:     'POST',
            url:        '/api/auth/register',
            data:       {
                name:           name,
                phoneNumber:    phoneNumber,
                password:       password
            }
        })
    }

    this.login = function (phoneNumber, password)
    {
        return $http({
            method:     'POST',
            url:        '/api/auth/login',
            data:       {
                phoneNumber:    phoneNumber,
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
})
