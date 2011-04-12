/*
 * contains functions for printing
 */

/** Prints one page identified by uuid */
function printOnePage(level) {
	var u = "pdf?uuidFrom=" + viewerOptions.uuid+"&howMany="+1+"&redirectURL="+escape(window.location.href);
	window.location.href = u;
}

/** Prints more pages, started from current selected page  */
var printMorePagesDialog =  null;
function printMorePages(level) {
	if (!printMorePagesDialog) {                
        $.get("i18n?action=text&name=first_page_html", function(xml) {
        	// dotaz na text
        	var head = $(xml).find('head').text();
            var body = $(xml).find('desc').text();
            
    		// create dialog content
    		var pdfDialog = tag("div", [
    		    h3(head),
    		    div(body,"pdf_desc"),
    		    tag("div",[
	    		    tag("div",[
		               strong(dictionary['pdf.numberOfPages']),
		               span("","pdf_dialog_content_validation", "color:red;font-style:italic;"),
		               br(),
		               text("30","getPdfStart",""+ viewerOptions.pdfMaxRange,"text_1")
	                ],{id:"texts",style:"padding-top:5px;"})
	 	        ],{id:"pdf_dialog_content"})
            ],{id:"pdf_dialog"});
    		
    		// print dialog to document
    		$(document.body).append(renderHTML(pdfDialog));

    		// text validaton
    		$('#text_1').keyup(function() {
    			if ( /\d+/.test($("#text_1").val())  ) {
        			var v = parseInt($("#text_1").val());
        			if (v > generatePdfMaxRange) {
        				$("#pdf_dialog_content_validation").text(dictionary['pdf.validationError.toomuch']);
        			} else {
        				$("#pdf_dialog_content_validation").text("");
        			}
    			} else {
    				$("#pdf_dialog_content_validation").text(dictionary['pdf.validationError.nan']);
    			}
			});
    		
    		// create jquery dialog
            printMorePagesDialog = $("#pdf_dialog").dialog({
    	        bgiframe: true,
    	        width: 400,
    	        height: 300,
    	        modal: true,
    	        title: dictionary['generatePdfTitle'],

                buttons: {
                    "Ok": function() {
            			if (/\d+/.test($("#text_1").val()) ) {
            				var u = "pdf?uuidFrom=" + viewerOptions.uuid+"&howMany="+$("#text_1").val()+"&redirectURL="+escape(window.location.href);
                			window.location.href = u;
            			}
                    	$(this).dialog("close");
                    } ,
                    "Cancel": function() {
                        $(this).dialog("close"); 
                    } 
                }
            });
        });
	} else {
		printMorePagesDialog.dialog('open');
	}
}

