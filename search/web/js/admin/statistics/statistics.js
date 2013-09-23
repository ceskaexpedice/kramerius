
function Statistics() { 
    this.reportDialogs = []; 
    this.dialog = null; 
    this.contextDialog = null;
}

Statistics.prototype._url=function(report,format) {
    var url = 'stats?format='+format+'&report='+report;
    return url;
}

/** Show context dialog **/
Statistics.prototype.showContextDialog = function() {
    $.get("inc/admin/_statistics_context_container.jsp", bind(function(data) {
        if (this.contextDialog) {
            this.contextDialog.dialog('open');
        } else {
            var pdiv = '<div id="statistic_context"></div>';
            $(document.body).append(pdiv);
            this.contextDialog = $("#statistic_context").dialog({
                bgiframe: true,
                width:  600,
                height:  450,
                modal: true,
                title: dictionary['statistics.main_dialog'],
                buttons: [{
                              text:dictionary['common.close'],
                              click:function() {
                                 $(this).dialog("close"); 
                              }
                }]
            });
        }        
        $("#statistic_context").html(data);
    },this));    
}

/** Show main dialog **/
Statistics.prototype.showDialog = function() {
    $.get("inc/admin/_statistics_container.jsp", bind(function(data) {
        if (this.dialog) {
            this.dialog.dialog('open');
        } else {
            var pdiv = '<div id="statistic"></div>';
            $(document.body).append(pdiv);
            this.dialog = $("#statistic").dialog({
                bgiframe: true,
                width:  600,
                height:  450,
                modal: true,
                title: dictionary['statistics.main_dialog'],
                buttons: [{
                              text:dictionary['common.close'],
                              click:function() {
                                 $(this).dialog("close"); 
                              }
                }]
            });
        }        
        $("#statistic").html(data);
    },this));    
}

/** Lang **/
Statistics.prototype.reloadLangReport=function(action,type, val, offset,size) {
    var url = "inc/admin/_statistics_langs.jsp?type=lang&val="+val+"&offset="+offset+"&size="+size;
    if (action !== null) {
        url = url +'&action='+action;
    }
    $.get(url, bind(function(data) {
        $("#statistic_report_lang").html(data);
    },this));
}


Statistics.prototype.langCSV=function(action) {
    var url = this._url('lang','CSV'); // 'stats?format=CSV&report=lang';
    if (action !== null) {
        url = url +'&action='+action;
        window.open(url, '_blank');
    } else {
        window.open(url, '_blank');
    }
}

Statistics.prototype.langXML=function(action) {
    var url = this._url('lang','XML'); // 'stats?format=CSV&report=lang';
    if (action !== null) {
        url = url +'&action='+action;
        console.log(url);
        window.open(url, '_blank');
    } else {
        console.log(url);
        window.open(url, '_blank');
    }
}

Statistics.prototype.showLangReport = function(action) {
    if(console) {
        console.log(" action "+action);
    }
    var url = "inc/admin/_statistics_langs.jsp?type=lang&val=x";
    if (action) url = url + "&action="+action;
    $.get(url, bind(function(data) {
        var dDialog = this.reportDialogs['lang'];
        if (dDialog) {
            dDialog.dialog('open');
        } else {
            var pdiv = '<div id="statistic_report_lang"></div>';
            $(document.body).append(pdiv);
            dDialog = $("#statistic_report_lang").dialog({
                bgiframe: true,
                width:  800,
                height:  600,
                modal: true,
                title: dictionary ['statistics.report.lang'],
                buttons: [{
                              text:dictionary['common.close'],
                              click:function() { $(this).dialog("close"); }
                }]
            });
        }
        $("#statistic_report_lang").html(data);
    },this));    
    
}
/**~ Lang **/



/** Author **/
Statistics.prototype.reloadAuthorsReport=function(action, type, val, offset,size) {
    var url = "inc/admin/_statistics_authors.jsp?type=author&val="+val+"&offset="+offset+"&size="+size;
    if (action !== null) {
        url = url + '&action='+action;
    }
    $.get(url, bind(function(data) {
        $("#statistic_report_author").html(data);
    },this));
}


Statistics.prototype.showAuthorReport = function(action) {
    var url = "inc/admin/_statistics_authors.jsp?type=author&val=x";
    if (action !== null) {
        url = url + '&action='+action;
    }
    $.get(url, bind(function(data) {
        var dDialog = this.reportDialogs['author'];
        if (dDialog) {
            dDialog.dialog('open');
        } else {
            var pdiv = '<div id="statistic_report_author"></div>';
            $(document.body).append(pdiv);
            dDialog = $("#statistic_report_author").dialog({
                bgiframe: true,
                width:  800,
                height:  600,
                modal: true,
                title: dictionary['statistics.report.authors'],
                buttons: [{
                              text:dictionary['common.close'],
                              click:function() {
                                 $(this).dialog("close"); 
                        }
                }]
            });
        }
        
        $("#statistic_report_author").html(data);
    },this));    
    
}

Statistics.prototype.authorCSV=function(action) {
    var url = this._url('author','CSV'); // 'stats?format=CSV&report=author';
    if (action !== null) {
        url = url +'&action='+action;
        console.log(url);
        window.open(url, '_blank');
    } else {
        console.log(url);
        window.open(url, '_blank');
    }
}

Statistics.prototype.authorXML=function(action) {
    var url = this._url('author','XML'); // 'stats?format=CSV&report=author';
    url = url + (action === null ? '':'');    
    if (action !== null) {
        url = url +'&action='+action;
        window.open(url, '_blank');
    } else {
        window.open(url, '_blank');
    }
}

/**~ Author **/


/** Dates range **/
Statistics.prototype.dateDurationCSV=function(action,dateFrom, dateTo) {
    var url = this._url('dates','CSV'); // 'stats?format=CSV&report=model';
    if (action !== null) {
        url = url + '&action='+action;
    }
    if (dateFrom !== null && dateTo !== null) {
        url = url + '&filteredValue='+dateFrom+"-"+dateTo;   
        if (console) console.log(' url is '+url);    
        window.open(url, '_blank');
    }
}

Statistics.prototype.dateDurationXML=function(action,dateFrom, dateTo) {
    var url = this._url('dates','XML'); // 'stats?format=CSV&report=model';
    if (action !== null) {
        url = url + '&action='+action;
    }
    if (dateFrom !== null && dateTo !== null) {
        url = url + '&filteredValue='+dateFrom+"-"+dateTo;   
        if (console) console.log(' url is '+url);    
        window.open(url, '_blank');
    }
}



Statistics.prototype.reloadDatesRangeReport=function(action,type, val, offset,size) {
    var url = "inc/admin/_statistics_datesrange.jsp?type=dates&val="+val+"&offset="+offset+"&size="+size;
    if (action !== null) {
        url = url + '&action='+action;
    }
    if (console) console.log("url "+url);
    $.get(url, bind(function(data) {
        $("#statistic_report_dates").html(data);
    },this));
}

Statistics.prototype.showDatesRangeReport = function(action, from, to) {
    var url = "inc/admin/_statistics_datesrange.jsp?type=dates&val="+from+"-"+to;
    if (action !== null) {
        url = url + '&action='+action;
    }
    $.get(url, bind(function(data) {
        var dDialog = this.reportDialogs['dates'];
        if (dDialog) {
            dDialog.dialog('open');
        } else {
            var pdiv = '<div id="statistic_report_dates"></div>';
            $(document.body).append(pdiv);
            dDialog = $("#statistic_report_dates").dialog({
                bgiframe: true,
                width:  800,
                height:  600,
                modal: true,
                title: dictionary['statistics.report.dates'],
                buttons: [{
                              text:dictionary['common.close'],
                              click:function() { $(this).dialog("close"); }
                }]
            });
        }
        $("#statistic_report_dates").html(data);
    },this));    
}
/** ~Dates range */


/** Selected pids **/
Statistics.prototype.pidsCSV=function(action,pids) {
    var url = this._url('pids','CSV'); // 'stats?format=CSV&report=model';
    if (action !== null) {
        url = url + '&action='+action;
    }
    url = url + '&filteredValue=';   
    url = reduce(function(base, item, status) {
        base = base+item+ (status.last ? "": ",");
        return base;
    }, url,pids); 
    if (console) console.log(' url is '+url);    
    window.open(url, '_blank');
}

Statistics.prototype.pidsXML=function(action, pids) {
    var url = this._url('pids','XML'); // 'stats?format=CSV&report=model';
    if (action !== null) {
        url = url + '&action='+action;
    }
    url = url + '&filteredValue=';   
    url = reduce(function(base, item, status) {
        base = base+item+ (status.last ? "": ",");
        return base;
    }, url,pids); 
    if (console) console.log(' url is '+url);    
    window.open(url, '_blank');
}



Statistics.prototype.reloadPidsReport=function(action,type, val, offset,size) {
    var url = "inc/admin/_statistics_pids.jsp?type=pids&val="+val+"&offset="+offset+"&size="+size;
    url = reduce(function(base, item, status) {
        base = base+item+ (status.last ? "": ",");
        return base;
    }, url,pids); 
    if (action !== null) {
        url = url + '&action='+action;
    }
    if (console) console.log("url "+url);
    $.get(url, bind(function(data) {
        $("#_statistics_pids").html(data);
    },this));
}

Statistics.prototype.showPidsReport = function(action,pids) {
    var url = "inc/admin/_statistics_pids.jsp?type=pids&val=";
    url = reduce(function(base, item, status) {
        base = base+item+ (status.last ? "": ",");
        return base;
    }, url,pids); 
    if (action !== null) {
        url = url + '&action='+action;
    }
    $.get(url, bind(function(data) {
        var dDialog = this.reportDialogs['dates'];
        if (dDialog) {
            dDialog.dialog('open');
        } else {
            var pdiv = '<div id="_statistics_pids"></div>';
            $(document.body).append(pdiv);
            dDialog = $("#_statistics_pids").dialog({
                bgiframe: true,
                width:  800,
                height:  600,
                modal: true,
                title: dictionary['statistics.report.pids'],
                buttons: [{
                              text:dictionary['common.close'],
                              click:function() { $(this).dialog("close"); }
                }]
            });
        }
        $("#_statistics_pids").html(data);
    },this));    
}
/** ~pids */


/** Model **/
Statistics.prototype.showModelReport = function(action,model) {
    if(console) {
        console.log(" action "+action +" and model "+model);
    }
    var url = "inc/admin/_statistics_model.jsp?type=model&val="+model;
    if (action) url = url + "&action="+action;
    $.get(url, bind(function(data) {
        var modelDialog = this.reportDialogs['model'];
        if (modelDialog) {
            modelDialog.dialog('open');
        } else {
            var pdiv = '<div id="statistic_report_model"></div>';
            $(document.body).append(pdiv);
            modelDialog = $("#statistic_report_model").dialog({
                bgiframe: true,
                width:  800,
                height:  600,
                modal: true,
                title: dictionary['statistics.report.model'],
                buttons: [{
                              text:dictionary['common.close'],
                              click:function() {
                                 $(this).dialog("close"); 
                              }
                }]
            
            });
        }
        
        $("#statistic_report_model").html(data);
    },this));    
}

Statistics.prototype.reloadModelReport=function(action,type, val, offset,size) {
    var url = "inc/admin/_statistics_model.jsp?type="+type+"&val="+val+"&offset="+offset+"&size="+size;
    if(console) {
        console.log(" action "+action +" and model "+val);
    }
    if (action) url = url + "&action="+action;
    $.get(url, bind(function(data) {
        $("#statistic_report_model").html(data);
    },this));
}
Statistics.prototype.modelCSV=function(action,filteredVal) {
    var url = this._url('model','CSV'); // 'stats?format=CSV&report=model';
    if (action !== null) {
        url = url +'&action='+action;
    }
    if (filteredVal !== null) {
        url = url +'&filteredValue='+filteredVal;
        window.open(url, '_blank');
    } else {
        if (console) console.log('no filtered val defined ');
    }
}

Statistics.prototype.modelXML=function(action, filteredVal) {
    var url = this._url('model','XML'); // 'stats?format=XML&report=model';
    if (action !== null) {
        url = url +'&action='+action;
    }
    if (filteredVal !== null) {
        url = url +'&filteredValue='+filteredVal;
        window.open(url, '_blank');
    } else {
        if (console) console.log('no filtered val defined ');
    }
}
/** ~Model */


var statistics = new Statistics();