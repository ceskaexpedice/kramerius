function Statistics() { 
    this.reportDialogs = []; 
    this.dialog = null; 
}

/**  */
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



Statistics.prototype.reloadLangReport=function(type, val, offset,size) {
    $.get("inc/admin/_statistics_langs.jsp?type=lang&val="+val+"&offset="+offset+"&size="+size, bind(function(data) {
        $("#statistic_report_lang").html(data);
    },this));
}

/** lang report */
Statistics.prototype.showLangReport = function() {
    $.get("inc/admin/_statistics_langs.jsp?type=lang&val=x", bind(function(data) {
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
                              click:function() {
                                 $(this).dialog("close"); 
                              }
                }]
            
            });
        }
        
        $("#statistic_report_lang").html(data);
    },this));    
    
}


Statistics.prototype.reloadAuthorsReport=function(type, val, offset,size) {
    $.get("inc/admin/_statistics_authors.jsp?type=author&val="+val+"&offset="+offset+"&size="+size, bind(function(data) {
        $("#statistic_report_author").html(data);
    },this));
}


/** dates range report */
Statistics.prototype.showAuthorReport = function() {
    $.get("inc/admin/_statistics_authors.jsp?type=author&val=x", bind(function(data) {
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

Statistics.prototype.reloadDatesRangeReport=function(type, val, offset,size) {
    $.get("inc/admin/_statistics_datesrange.jsp?type=dates&val="+val+"&offset="+offset+"&size="+size, bind(function(data) {
        $("#statistic_report_dates").html(data);
    },this));
}

/** dates range report */
Statistics.prototype.showDatesRangeReport = function(from, to) {
    $.get("inc/admin/_statistics_datesrange.jsp?type=dates&val="+from+"-"+to, bind(function(data) {
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
                              click:function() {
                                 $(this).dialog("close"); 
                              }
                }]
            
            });
        }
        
        $("#statistic_report_dates").html(data);
    },this));    
    
}


Statistics.prototype.reloadModelReport=function(type, val, offset,size) {
    $.get("inc/admin/_statistics_model.jsp?type="+type+"&val="+val+"&offset="+offset+"&size="+size, bind(function(data) {
        $("#statistic_report_model").html(data);
    },this));
}

/** show model report */
Statistics.prototype.showModelReport = function(model) {
    $.get("inc/admin/_statistics_model.jsp?type=model&val="+model, bind(function(data) {
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


var statistics = new Statistics();