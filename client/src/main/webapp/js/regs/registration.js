/**
 * Register new user
 * 
 * @constructor
 */
function Registration() {
}

Registration.prototype = {

    register : function(user, okfunc, failCaptcha) {

        var successFunction = _.bind(function(data) {
            okfunc.apply(null, []);
        }, this);

        var errorFunction = _.bind(function(data) {
            var dataObject = eval('(' + data.responseText + ')');
            if (dataObject && dataObject.error == 'bad_captcha') {
                failCaptcha.apply(null, []);
            }
        }, this);

        var encodedData = Base64.encode(JSON.stringify(user));

        $.ajax({
            type : "POST",
            dataType : "json",
            'url' : 'reguser?action=create',
            'data' : {
                'encodedData' : encodedData
            },
            success : successFunction,
            error : errorFunction
        });

    }
}
