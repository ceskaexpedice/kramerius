/**
 * Viewers controll
 * @constructor
 */
function ViewersControll() {}

ViewersControll.prototype = {

        'ctx': {
                "viewers":[],
                "session":{}
        },

        'instantiate':function(name) {
                return eval('(function() { return new '+ name+'();})();');      
        },
    
        'forbiddenCheck':function(name, okFunc, failFunc) {
            var fromProto = eval('(function() { return '+ name+'.prototype.forbiddenCheck;})()');      
            var fromObj = eval('(function() { return '+ name+'.forbiddenCheck;})()');      
            if (fromProto) {
                fromProto.apply(null, [okFunc, failFunc]);
            } else if (fromObj) {
                fromObj.apply(null, [okFunc, failFunc]);
            } else {
                // allowed
                okFunc.apply(null, []);
            }       
        },

        'findByName':function(name) {
                var found = _.reduce(this.ctx.viewers, function(memo, value, index) {
                        if (memo == null) {
                                if (value.name===name) {
                                        memo = value;
                                }                                
                        }
                        return memo;
                },null);
                return found;
        },

        'select':function(data) {
                var found = _.reduce(this.ctx.viewers, function(memo, value, index) {
                        if (memo == null) {

                                var fromProto = eval('(function() { return '+ value.object+'.prototype.isEnabled;})()');      
                                var fromObj = eval('(function() { return '+ value.object+'.isEnabled;})()');      

                                if (fromProto) {
                                        var flag = fromProto.apply(null, [data]);
                                        if (flag) {
                                                memo = value;
                                        }
                                } else if (fromObj) {
                                        var flag = fromObj.apply(null, [data]);
                                        if (flag) {
                                                memo = value;
                                        }
                                }
                        }
                        return memo;
                }, null);
                
                return found;
        },

        'initalizeViewers':function(viewers) {
                this.ctx["viewers"]=viewers;
        },
        

        'loadSessionInitialization':function(sessionObject) {
            if (sessionObject["viewers"]) {
                this.ctx.session = sessionObject["viewers"];
            }
        },
        'storeSessionInitialization':function() {
            K5.api.storeToSession("viewers",this.ctx.session);
        }
}


