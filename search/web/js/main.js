$(function() {
    $('button').button();
    $('.facet').addClass(
            'shadow10 ui-tabs ui-widget ui-widget-content ui-corner-all');
    $('.facet>div').addClass('ui-dialog-titlebar ui-widget-header');

});

// TODO: poznamka by PS - funguje pouze pro objekty nejnizsi urovne ??
function jq(myid) {
    return '#' + myid.replace(/(:|\.|\/)/g, '\\$1');
}

function showHelp(language, part) {
    var url = 'help/help.jsp?';
    if (part != null && part != '')
        url = url + '#' + part;
    temp = window
            .open(
                    url,
                    'HELP',
                    'width=608,height=574,menubar=0,resizable=0,scrollbars=1,status=0,titlebar=0,toolbar=0,z-lock=0,left=200,top=20');
    temp.opener = this;
    temp.focus();
}

// Toggle visibility advanced search option
function toggleAdv() {
    var y = $('#q').offset().top + $('#q').height() + 10;
    var x = $('#q').offset().left;
    $('#advSearch').css('left', x);
    $('#advSearch').css('top', y);
    $('#advSearch').toggle();
}

function trim10(str) {
    var whitespace = ' \n\r\t\f\x0b\xa0\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u200b\u2028\u2029\u3000';
    for (var i = 0; i < str.length; i++) {
        if (whitespace.indexOf(str.charAt(i)) === -1) {
            str = str.substring(i);
            break;
        }
    }
    for (i = str.length - 1; i >= 0; i--) {
        if (whitespace.indexOf(str.charAt(i)) === -1) {
            str = str.substring(0, i + 1);
            break;
        }
    }
    return whitespace.indexOf(str.charAt(0)) === -1 ? str : '';
}

function isScrolledIntoView(elem, view) {
    var docViewTop = $(view).offset().top;
    var docViewBottom = docViewTop + $(view).height();

    var elemTop = $(elem).offset().top;
    var elemBottom = elemTop + $(elem).height();
    // $("#test").html("elemBottom: "+elemBottom+
    // " docViewTop: "+docViewTop+
    // " elemTop: "+elemTop+
    // " docViewBottom: "+docViewBottom);
    return ((elemBottom >= docViewTop) && (elemTop <= docViewBottom));
}

function isScrolledIntoWindow(elem) {
    var docViewTop = $(window).scrollTop();
    var docViewBottom = docViewTop + $(window).height();

    var elemTop = $(elem).offset().top;
    var elemBottom = elemTop + $(elem).height();
    return ((elemBottom >= docViewTop) && (elemTop <= docViewBottom));
}

/**
 * construct url from selected pids
 */
function urlWithPids(/** String */
baseUrl, /** Array */
pids) {
    if (!pids)
        pids = this.pids;
    return baseUrl
            + "{"
            + reduce(function(base, item, status) {

                base = base + item.pid.replaceAll(":", "\\:")
                        + (status.last ? "" : ";");
                return base;
            }, "", pids) + "}";
}

/** Download original */
function DownloadOriginal() {
    this.dialog = null;
}

DownloadOriginal.prototype.download = function(structs) {

    var urlDialog = urlWithPids("inc/_download_original_dialog.jsp?pids=",
            structs);
    $.get(urlDialog, function(data) {
        if (this.dialog) {
            this.dialog.dialog('open');
        } else {
            $(document.body).append('<div id="downloadOriginal"></div>')
            this.dialog = $('#downloadOriginal').dialog({
                width : 600,
                height : 400,
                modal : true,
                title : dictionary["administrator.menu.downloadOriginal"],
                buttons : {
                    "Close" : function() {
                        $(this).dialog("close");
                    }
                }
            });
        }

        $("#downloadOriginal").html(data);
    });

}
/** Download original object */
var downloadOriginal = new DownloadOriginal();

/** 
 * Constructor for PDF object
 */
function PDF() {
    this.dialog = null;
    this.structs = null;
    this.previous = null;

    this.waitDialog = null;
    // error dialogs
    this.pagesValidationErrorDialog = null;
    this.conflictErrorDialog = null;
}


/**
 * Config initialization
 */
PDF.prototype.initConf = function() {
    if (!this.apiPDFSettings) {
        $.get("api/v5.0/pdf", bind(function(data) {
            this.apiPDFSettings = data;
        }, this));
    }
}

/**
 * Download file
 */
PDF.prototype.downloadFile = function(url) {
    var xhr = new XMLHttpRequest();
    xhr.open('GET', url, true);
    xhr.responseType = "blob";
    xhr.onreadystatechange = bind(function() {
        console.log("dialog " + this.waitDialog);
        if (xhr.readyState == 4) {
            if (xhr.status == 200) {
                var name = (function() {
                    var date = new Date();
                    return "" + date.getFullYear() + "" + date.getDate() + ""
                            + date.getMonth() + "_" + date.getHours() + ""
                            + date.getMinutes() + "" + date.getSeconds() + ".pdf";
                })();
                var blob = xhr.response;
                var burl = window.URL.createObjectURL(blob);
                var ref = $('<a/>', {
                    id : '_pdf_download_bloblink',
                    href : burl,
                    download : name,
                    style : "display:none"
                });
                ref.text("click to download");
                $("#waitPdf").append(ref);

                // JQuery issue, the code:
                // $("#_pdf_download_bloblink").trigger('click');
                // doesn't work

                $("#_pdf_download_bloblink").get(0).click();
                this.waitDialog.dialog('close');
            } else if (xhr.status == 400) {
                this.waitDialog.dialog('close');
                this.showPagesValidationError();
            } else if (xhr.status == 409) {
                this.waitDialog.dialog('close');
                this.showPagesValidationError();
            }
        }
    }, this);
    xhr.send(null);
}

PDF.prototype.showConflictError = function() {

    if (this.conflictErrorDialog) {
        this.conflictErrorDialog.dialog('open');
    } else {
        $(document.body)
                .append(
                        '<div id="conflictError">'
                                + '<div style="margin: 16px; font-family: sans-serif; font-size: 10px; ">'
                                + '<table width="100%">'
                                + '<tr><td align="center" id="conflictError_message" style="color:red">'
                                + dictionary['pdf.serverBusy.text']
                                + '</td></tr>' + '</table>' + '</div>'
                                + '</div>');
        this.conflictErrorDialog = $('#conflictError').dialog({
            width : 400,
            height : 270,
            modal : true,
            title : dictionary["pdf.serverBusy.label"],
            buttons : [ {
                text : dictionary['common.close'],
                click : bind(function() {
                    this.conflictErrorDialog.dialog("close");
                }, this)
            } ]
        });
    }

}

PDF.prototype.showPagesValidationError = function() {
    if (this.pagesValidationErrorDialog) {
        this.pagesValidationErrorDialog.dialog('open');
    } else {
        $(document.body)
                .append(
                        '<div id="pagesValidationError">'
                                + '<div style="margin: 16px; font-family: sans-serif; font-size: 10px; ">'
                                + '<table width="100%">'
                                + '<tr><td align="center" id="pagesValidationError_message" style="color:red">'
                                + dictionary['pdf.pdfError.toomuch']
                                + '</td></tr>' + '</table>' + '</div>'
                                + '</div>');
        this.pagesValidationErrorDialog = $('#pagesValidationError').dialog({
            width : 400,
            height : 270,
            modal : true,
            title : dictionary["pdf.pdfError.label"],
            buttons : [ {
                text : dictionary['common.close'],
                click : bind(function() {
                    this.pagesValidationErrorDialog.dialog("close");
                }, this)
            } ]
        });
    }
}

PDF.prototype.renderPDF = function() {
    var u = null;
    var selected = $("#pdf input:checked");
    if (selected.length >= 1) {
        var pidsstring = selected.val();
        var id = selected.attr("id");
        id = id.substring(0, id.length - "_radio".length);
        if (id == "selection") {
            var selectedPids = pidsstring.slice(1, pidsstring.length - 1)
                    .split(",");
            var reducedString = reduce(function(base, element, status) {
                if (!status.first) {
                    base = base + ",";
                }
                return base + element.trim();
            }, "", selectedPids);
            u = "api/v5.0/pdf/selection?pids=" + reducedString;
        } else {
            var selectedPids = pidsstring.slice(1, pidsstring.length - 1)
                    .split(",");
            var howMany = parseInt($("#" + id + "_input").val());

            if (this.apiPDFSettings.pdfMaxRange === "unlimited") {
                u = "api/v5.0/pdf/parent?pid=" + selectedPids[0];
            } else {
                u = "api/v5.0/pdf/parent?pid=" + selectedPids[0] + "&number="
                        + howMany;
            }
        }
        // device, IMAGE,TEXT
        u = u + "&pageType="
                + $("#pdfsettings input[name=device]:checked").val();
        // page format
        u = u + "&format=" + $("#pdfsettings_ereader option:selected").val();

        if (this.waitDialog) {
            this.waitDialog.dialog('open');
        } else {
            $(document.body)
                    .append(
                            '<div id="waitPdf">'
                                    + '<div style="margin: 16px; font-family: sans-serif; font-size: 10px; ">'
                                    + '<table width="100%">'
                                    + '<tr><td align="center"><img src="img/loading.gif" height="16px" width="16px"/></td></tr>'
                                    + '<tr><td align="center" id="waitPdf_message">'
                                    + dictionary['pdf.waitDialog.message']
                                    + '</td></tr>' + '</table>' + '</div>'
                                    + '</div>');
            this.waitDialog = $('#waitPdf').dialog({
                width : 400,
                height : 270,
                modal : true,
                title : dictionary["generatePdfTitle"],
                buttons : [ {
                    text : dictionary['common.close'],
                    click : bind(function() {
                        this.dialog.dialog("close");
                    }, this)
                } ]
            });
        }

        this.downloadFile(u);
    }
}

PDF.prototype.generate = function(objects) {
    this.devconf = null;
    this.initConf();

    this.structs = objects;
    var urlDialog = urlWithPids("inc/_pdf_dialog.jsp?pids=", objects);
    $.get(urlDialog, bind(function(data) {

        if (this.dialog) {
            this.dialog.dialog('open');
        } else {
            $(document.body).append('<div id="pdf"></div>')
            this.dialog = $('#pdf').dialog({
                width : 600,
                height : 500,
                modal : true,
                title : dictionary["generatePdfTitle"],
                buttons : [ {
                    text : dictionary['pdf.dialog.button.generate'],
                    click : bind(function() {
                        this.renderPDF();
                        this.dialog.dialog("close");
                    }, this)
                },

                {
                    text : dictionary['common.close'],
                    click : function() {
                        $(this).dialog("close");
                    }
                } ]

            });
        }

        $('#pdf').html(data);

    }, this));
}

// TODO: On keyup -> checkup
PDF.prototype.onKeyup = function(id, type, pidsstring) {
    var val = $("#" + id + "_input").val();
    if (!isNaN(val)) {
        var n = parseInt($("#" + id + "_input").val());
        if (this.apiPDFSettings.pdfMaxRange !== "unlimited") {
            if (n <= this.apiPDFSettings.pdfMaxRange) {
                $("#" + id + "_error").text("");
            } else {
                $("#" + id + "_error").text(
                        dictionary["pdf.validationError.toomuch"]);
            }
        } else {
            $("#" + id + "_error").text("");
        }
    } else {
        $("#" + id + "_error").text(dictionary["pdf.validationError.nan"]);
    }
}

PDF.prototype.onChange = function(id, type, pidsstring) {
    if (this.previous) {
        $(this.previous).hide();
    }
    $("#" + id + "_option").show();
    this.previous = "#" + id + "_option";

    $(".pdfSelected").removeClass("pdfSelected");
    $("#" + id).addClass("pdfSelected");
}

/*
 * 
 * PDF.prototype.onSettingsChange = function(type) {
 * if(this.deviceSelection[type]) { var invf = this.deviceSelection[type];
 * invf.call(this); } }
 * 
 * PDF.prototype.onFormatChange = function() { var val = $("#pdfsettings_ereader
 * select option:selected").val(); if (this.rectangleSelections[val]) { var invf =
 * this.rectangleSelections[val]; this.devconf = invf.call(this); } }
 */

/** PDF object */
var pdf = new PDF();

// font constants
/*
 * PDF.PDF_FONT_STANDARD = 0; PDF.PDF_FONT_ITALIC = 1; PDF.PDF_FONT_BOLD = 2;
 * PDF.PDF_FONT_BOLDITALIC = 3;
 */

/** Print object */
function Print() {
    this.dialog = null;
    this.structs = null;
    this.previous = null;

    this.informDialog = null;
}

Print.prototype.printTitle = function() {
    var u = null;
    var selected = $("#print input:checked");
    if (selected.length >= 1) {

        var pidsstring = selected.val();
        var id = selected.attr("id");
        id = id.substring(0, id.length - "_radio".length);
        if (id == "selection") {
            var selectedPids = pidsstring.slice(1, pidsstring.length - 1)
                    .split(",");
            selectedPids = map(function(elm) {
                return {
                    model : '',
                    pid : elm.trim()
                }
            }, selectedPids);
            u = urlWithPids("print?action=SELECTION&pids=", selectedPids);
        } else {
            var selectedPids = pidsstring.slice(1, pidsstring.length - 1)
                    .split(",");
            u = "print?action=PARENT&pidFrom=" + selectedPids[0];
        }

        if (this.informDialog) {
            this.informDialog.dialog('open');
        } else {
            $(document.body).append('<div id="printInfo"></div>')
            this.informDialog = $('#printInfo').dialog({
                width : 200,
                height : 120,
                modal : true,
                title : dictionary["administrator.dialogs.print"],
                buttons : {
                    "Close" : function() {
                        $(this).dialog("close");
                    }
                }
            });

        }
        $(document.body).append('<div id="printInfo"></div>');

        $('#printInfo').html(
                '<table><tr><td aling="center" halign="center">'
                        + dictionary['print.info.waiting.message']
                        + '</td></tr></table>');

        $.get(u, bind(function(data) {
            $('#printInfo').html(
                    '<table><tr><td aling="center" halign="center">'
                            + dictionary['print.info.done.message']
                            + '</td></tr></table>');

        }, this));

    } else {
        throw new Error("No print option selected !");
    }
}

Print.prototype.onChange = function(id, type, pidsstring) {
    this.selectedType = type;
    this.selectedPids = pidsstring.slice(1, pidsstring.length - 1).split(",");
}

Print.prototype.print = function(objects) {
    this.structs = objects;
    var urlDialog = urlWithPids("inc/_print_dialog.jsp?pids=", objects);
    $.get(urlDialog, bind(function(data) {
        if (this.dialog) {
            this.dialog.dialog('open');
        } else {
            $(document.body).append('<div id="print"></div>')
            this.dialog = $('#print').dialog({
                width : 600,
                height : 500,
                modal : true,
                title : dictionary["administrator.dialogs.print"],
                buttons : {
                    "Generuj" : bind(function() {
                        this.printTitle();
                        this.dialog.dialog("close");
                    }, this),

                    "Close" : function() {
                        $(this).dialog("close");
                    }
                }
            });
        }
        $('#print').html(data);

    }, this));
}

var print = new Print();

function RegisterUser() {
    this.dialog = null;
    this.checkDialog = null;
    this.badCaptchaDialog = null;

}

RegisterUser.prototype.badCaptcha = function() {
    if (this.badCaptchaDialog) {
        this.badCaptchaDialog.dialog('open');
    } else {
        $(document.body).append('<div id="badCaptcha"></div>');
        $("#badCaptcha").html(
                '<div> <h3> '
                        + dictionary['registeruser.message.badcaptchatitle']
                        + '</h3><div> '
                        + dictionary['registeruser.message.badcaptcha']
                        + '</div></div>');
        this.badCaptchaDialog = $('#badCaptcha').dialog({
            width : 400,
            height : 250,
            modal : true,
            title : dictionary['registeruser.message.badcaptchadialogtitle'],
            buttons : [ {
                text : dictionary['common.close'],
                click : function() {
                    $(this).dialog("close");
                }
            } ]
        });

    }

}

RegisterUser.prototype.emailCheck = function() {
    if (this.checkDialog) {
        this.checkDialog.dialog('open');
    } else {
        $(document.body).append('<div id="checkEmailRegisterUser"></div>');
        $("#checkEmailRegisterUser").html(
                '<div> <h3> '
                        + dictionary['registeruser.message.checkmailtitle']
                        + '</h3><div> '
                        + dictionary['registeruser.message.checkemail']
                        + '</div></div>');

        this.checkDialog = $('#checkEmailRegisterUser').dialog({
            width : 400,
            height : 250,
            modal : true,
            title : dictionary['registeruser.message.checkemaildialogtitle'],
            buttons : [ {
                text : dictionary['common.close'],
                click : function() {
                    $(this).dialog("close");
                }
            } ]
        });
    }
}

RegisterUser.prototype.register = function() {
    $.get("inc/_register_new_user.jsp",
            bind(function(data) {
                    if (this.dialog) {
                        this.dialog.dialog('open');
                    } else {
                        $(document.body).append('<div id="registerUser"></div>');
                        this.dialog = $('#registerUser').dialog(
                                {
                                    width : 600,
                                    height : 550,
                                    modal : true,
                                    title : dictionary["registeruser.title"],
                                    buttons : [
                                    // create button
                                    {
                                        text : dictionary['common.create'],
                                        click : bind(function() {
                                                    // validation...
                                                    var resultFunc = bind(function(result) {
                                                                if (result) {
                                                                    console.log("result is :"+result);
                                                                    var data = regUserValidate.grabData();
                                                                    $.ajax({    type : "POST",
                                                                                url : "users?action=registernew",
                                                                                data : {
                                                                                    'loginName' : data.loginName,
                                                                                    'email' : data.email,
                                                                                    'password' : data.pswd,
                                                                                    'name' : data.name,
                                                                                    'captcha' : data.captcha
                                                                                },
                                                                                contentType : "application/x-www-form-urlencoded;charset=UTF-8",
                                                                                dataType : "json",
                                                                                success : bind(function() {
                                                                                            this.emailCheck();
                                                                                          },this),
                                                                                error : bind(function(data) {
                                                                                            var dataObject = eval('('
                                                                                                    + data.responseText
                                                                                                    + ')');
                                                                                            if (dataObject
                                                                                                    && dataObject.error == 'bad_captcha') {
                                                                                                this.badCaptcha();
                                                                                            }
                                                                                        },this)
                                                                        });
                                                                        this.dialog.dialog("close");
                                                                    }
                                                            }, this);

                                                            regUserValidate.validate(resultFunc);
                                                        },this)
                                                    },
                                                    {
                                                        text : dictionary['common.close'],
                                                        click : function() {
                                                                $(this).dialog("close");
                                                                    }
                                                    }
                                                ]
                                        });
                                }

                                $("#registerUser").html(data);
        },this));
}

var registerUser = new RegisterUser();

/* profile functionality */
function Profile() {
}

Profile.prototype.modify = function(func, okfunc) {
    $.get("profile?action=GET", function(data) {
        data = func(data);
        var encodedData = Base64.encode(JSON.stringify(data));
        $.post("profile?action=POST", {
            'encodedData' : encodedData
        }, okfunc, "json");
    });
}

/** common message functionality */
function Message(id) {
    this.dialog = null;
    this.id = id;
}

Message.prototype.show = function() {
    if (this.dialog) {
        this.dialog.dialog('open');
    } else {
        $(document.body).append(
                "<div id='message_dialog_" + this.id + "'></div>");
        this.dialog = $("#message_dialog_" + this.id).dialog({
            bgiframe : true,
            width : 350,
            height : 150,
            modal : true,
            title : '',
            buttons : [ {
                text : "Ok",
                click : function() {
                    $(this).dialog("close");
                }
            } ]
        });
    }
    $("#message_dialog_" + this.id)
            .html(
                    "<div style='height: 6em; width:350px; display: table-cell; vertical-align: middle; text-align:center;'>"
                            + dictionary['message.text.' + this.id] + "</div>");
}
