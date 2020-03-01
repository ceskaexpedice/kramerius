
function Statistics() { 
    this.reportDialogs = []; 
    this.dialog = null; 
    this.contextDialog = null;
}

Statistics.prototype._url=function(report,format) {
    var url = 'stats?format='+format+'&report='+report;
    return url;
}

Statistics.prototype._datefilter=function(url,dateFrom, dateTo) {
    var retval = url;
    if (dateFrom !== null) {
      retval = retval + '&dateFrom='+dateFrom;   
    }
    if (dateTo !== null) {
      retval = retval + "&dateTo="+dateTo;   
    }
    return retval;
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
                width:  700,
                height:  510,
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
Statistics.prototype.reloadLangReport=function(action, visibility, dateFrom, dateTo,type, val, offset, size, ipAddresses, uniqueIPAddresses) {
    var url = "inc/admin/_statistics_langs.jsp?type=lang&val="+val+"&offset="+offset+"&size="+size+"&ipaddresses="+ipAddresses+"&uniqueipaddresses="+uniqueIPAddresses;
    if (action !== null) {
        url = url +'&action='+action;
    }
    url= this._datefilter(url,dateFrom, dateTo);
    $.get(url, bind(function(data) {
        $("#statistic_report_lang").html(data);
    },this));
}


Statistics.prototype.langCSV=function(action, visibility, dateFrom, dateTo, ipAddresses, uniqueIPAddresses) {
    var url = this._url('lang','CSV'); // 'stats?format=CSV&report=lang';
    url = url + "&visibility="+visibility;
    url = url + "&ipaddresses=" + ipAddresses;
    url = url + "&uniqueipaddresses=" + uniqueIPAddresses;
    
    if(console) {
        console.log(" langCSV "+url);
    }
    
    if (action !== null) {
        url = url +'&action='+action;
        url= this._datefilter(url,dateFrom, dateTo);
        window.open(url, '_blank');
    } else {
        url= this._datefilter(url,dateFrom, dateTo);
        window.open(url, '_blank');
    }
}

Statistics.prototype.langXML=function(action, visibility, dateFrom, dateTo, ipAddresses, uniqueIPAddresses) {
    var url = this._url('lang','XML'); // 'stats?format=CSV&report=lang';
    url = url + "&visibility="+visibility;
    url = url + "&ipaddresses=" + ipAddresses;
    url = url + "&uniqueipaddresses=" + uniqueIPAddresses;
    
    if(console) {
        console.log(" langCSV "+url);
    }
    if (action !== null) {
        url = url +'&action='+action;
        url= this._datefilter(url,dateFrom, dateTo);
        console.log(url);
        window.open(url, '_blank');
    } else {
        url= this._datefilter(url,dateFrom, dateTo);
        console.log(url);
        window.open(url, '_blank');
    }
}

Statistics.prototype.showLangReport = function(action, visibility, dateFrom, dateTo, ipAddresses, uniqueIPAddresses) {
    if(console) {
        console.log(" action "+action);
    }
    var url = "inc/admin/_statistics_langs.jsp?type=lang&val=x"+"&visibility="+visibility;
    if (action) url = url + "&action="+action;
    url = this._datefilter(url,dateFrom, dateTo);
    url = url + "&ipaddresses=" + ipAddresses;
    url = url + "&uniqueipaddresses=" + uniqueIPAddresses;
    
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
Statistics.prototype.reloadAuthorsReport=function(action,visibility,dateFrom, dateTo, type, val, offset, size, ipAddresses, uniqueIPAddresses) {
    var url = "inc/admin/_statistics_authors.jsp?type=author&val="+val+"&offset="+offset+"&size="+size+"&visibility="+visibility+
                                                "&size="+size+"&ipaddresses="+ipAddresses+"&uniqueipaddresses="+uniqueIPAddresses;
    if (action !== null) {
        url = url + '&action='+action;
    }
    url = this._datefilter(url,dateFrom, dateTo);
    $.get(url, bind(function(data) {
        $("#statistic_report_author").html(data);
    },this));
}


Statistics.prototype.showAuthorReport = function(action, visibility, dateFrom, dateTo, ipAddresses, uniqueIPAddresses) {
    var url = "inc/admin/_statistics_authors.jsp?type=author&val=x"+"&visibility="+visibility;
    if (action !== null) {
        url = url + '&action='+action;
    }
    url = this._datefilter(url,dateFrom, dateTo);
    url = url + "&ipaddresses=" + ipAddresses;
    url = url + "&uniqueipaddresses=" + uniqueIPAddresses;
    
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

Statistics.prototype.authorCSV=function(action, visibility, dateFrom, dateTo, ipAddresses, uniqueIPAddresses) {
    var url = this._url('author','CSV'); // 'stats?format=CSV&report=author';
    url = url + "&visibility="+visibility;
    url = url + "&ipaddresses=" + ipAddresses;
    url = url + "&uniqueipaddresses=" + uniqueIPAddresses;
    
    if(console) {
        console.log(" authorCSV "+url);
    }
    
    if (action !== null) {
        url = url +'&action='+action;
        url = this._datefilter(url,dateFrom, dateTo);
        console.log(url);
        window.open(url, '_blank');
    } else {
        url = this._datefilter(url,dateFrom, dateTo);
        console.log(url);
        window.open(url, '_blank');
    }
}

Statistics.prototype.authorXML=function(action,visibility, dateFrom, dateTo, ipAddresses, uniqueIPAddresses) {
    var url = this._url('author','XML'); // 'stats?format=CSV&report=author';
    url = url + (action === null ? '':'');    
    url = url + "&visibility="+visibility;
    url = url + "&ipaddresses=" + ipAddresses;
    url = url + "&uniqueipaddresses=" + uniqueIPAddresses;
    
    if(console) {
        console.log(" authorXML "+url);
    }
    if (action !== null) {
        url = url +'&action='+action;
		url = this._datefilter(url,dateFrom, dateTo);
        window.open(url, '_blank');
    } else {
		url = this._datefilter(url,dateFrom, dateTo);
        window.open(url, '_blank');
    }
}

/**~ Author **/




/** Selected pids **/
Statistics.prototype.pidsCSV=function(action,visibility,pids, dateFrom, dateTo) {
    var url = this._url('pids','CSV'); // 'stats?format=CSV&report=model';
    if (action !== null) {
        url = url + '&action='+action;
    }
    url = url + '&filteredValue=';   
    url = reduce(function(base, item, status) {
        base = base+item+ (status.last ? "": ",");
        return base;
    }, url,pids); 
	url = this._datefilter(url,dateFrom, dateTo);
    if (console) console.log(' url is '+url);    
    window.open(url, '_blank');
}

Statistics.prototype.pidsXML=function(action,visibility, pids, dateFrom, dateTo) {
    var url = this._url('pids','XML'); // 'stats?format=CSV&report=model';
    if (action !== null) {
        url = url + '&action='+action;
    }
    url = url + '&filteredValue=';   
    url = reduce(function(base, item, status) {
        base = base+item+ (status.last ? "": ",");
        return base;
    }, url,pids); 
	url = this._datefilter(url,dateFrom, dateTo);
    if (console) console.log(' url is '+url);    
    window.open(url, '_blank');
}



Statistics.prototype.reloadPidsReport=function(action,visibility, dateFrom, dateTo,type, val, offset,size) {
    var url = "inc/admin/_statistics_pids.jsp?type=pids&val="+val+"&offset="+offset+"&size="+size;
    url = reduce(function(base, item, status) {
        base = base+item+ (status.last ? "": ",");
        return base;
    }, url,pids); 
    if (action !== null) {
        url = url + '&action='+action;
    }
	url = this._datefilter(url,dateFrom, dateTo);

    if (console) console.log("url "+url);
    $.get(url, bind(function(data) {
        $("#_statistics_pids").html(data);
    },this));
}

Statistics.prototype.showPidsReport = function(action,visibility,pids, dateFrom, dateTo) {
    var url = "inc/admin/_statistics_pids.jsp?type=pids&val=";
    url = reduce(function(base, item, status) {
        base = base+item+ (status.last ? "": ",");
        return base;
    }, url,pids); 
    if (action !== null) {
        url = url + '&action='+action;
    }
	url = this._datefilter(url,dateFrom, dateTo);
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
Statistics.prototype.showModelReport = function(action, visibility, dateFrom, dateTo, model, ipAddresses, uniqueIPAddresses) {
    if(console) {
        console.log(" action "+action +" and model "+model + " uniqueIPAddresses " + uniqueIPAddresses);
    }
    var url = "inc/admin/_statistics_model.jsp?type=model&val="+model+"&visibility="+visibility;
    url = this._datefilter(url,dateFrom,dateTo);
	if (action) url = url + "&action="+action;
    url = url + "&ipaddresses=" + ipAddresses;
    url = url + "&uniqueipaddresses=" + uniqueIPAddresses;
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

Statistics.prototype.reloadModelReport=function(action,visibility, dateFrom, dateTo,type, val, offset, size, ipAddresses, uniqueIPAddresses) {
    var url = "inc/admin/_statistics_model.jsp?type="+type+"&val="+val+"&visibility="+visibility+"&offset="+offset+"&size="+size+
                                               "&ipaddresses="+ipAddresses+"&uniqueipaddresses="+uniqueIPAddresses;
    if(console) {
        console.log(" action "+action +" and model "+val + " ipAddressa"+ ipAddresses);
    }
    if (action) url = url + "&action="+action;
	url = this._datefilter(url,dateFrom,dateTo);
	$.get(url, bind(function(data) {
        $("#statistic_report_model").html(data);
    },this));
}

Statistics.prototype.modelCSV=function(action,visibility,filteredVal, dateFrom, dateTo, ipAddresses, uniqueIPAddresses) {
    var url = this._url('model','CSV'); // 'stats?format=CSV&report=model';
    if (action !== null) {
        url = url +'&action='+action;
    }
    url = url + "&visibility="+visibility;
    url = url + "&ipaddresses=" + ipAddresses;
    url = url + "&uniqueipaddresses=" + uniqueIPAddresses;
    if(console) {
        console.log(" modelCSV "+url);
    }
    if (filteredVal !== null) {
        url = url +'&filteredValue='+filteredVal;
		url= this._datefilter(url,dateFrom, dateTo);
        window.open(url, '_blank');
    } else {
        if (console) console.log('no filtered val defined ');
    }
}


Statistics.prototype.modelXML=function(action,visibility, filteredVal,dateFrom, dateTo, ipAddresses, uniqueIPAddresses) {
    var url = this._url('model','XML'); // 'stats?format=XML&report=model';
    if (action !== null) {
        url = url +'&action='+action;
    }
    url = url + "&visibility="+visibility;
    url = url + "&ipaddresses=" + ipAddresses;
    url = url + "&uniqueipaddresses=" + uniqueIPAddresses;
    if (filteredVal !== null) {
        url = url +'&filteredValue='+filteredVal;
		url= this._datefilter(url,dateFrom, dateTo);
        window.open(url, '_blank');
    } else {
        if (console) console.log('no filtered val defined ');
    }
}
/** ~Model */


var statistics = new Statistics();