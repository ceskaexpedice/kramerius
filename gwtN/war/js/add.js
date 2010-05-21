//function to open help window
function openhelp(url, part) {
  if (part!=null && part!='')
    url=url+'#'+part;
  temp=window.open(url,'HELP','width=608,height=574,menubar=0,resizable=0,scrollbars=1,status=0,titlebar=0,toolbar=0,z-lock=0,left=200,top=20');
  temp.opener=this;
  temp.focus();
}

//function to open search window
function opensearch(url) {
  temp=window.open(url,'SEARCH','width=608,height=574,menubar=0,resizable=0,scrollbars=1,status=0,titlebar=0,toolbar=0,z-lock=0,left=100,top=20');
  temp.opener=this;
  temp.focus();
}

//function to open print window with content of element with id=tisk from actual document
function printIt() { 
  winId=window.open('','printwin','width=590,height=500,scrollbars=1,resizable=0');
  winId.document.write(document.getElementById('tisk').innerHTML); 
  winId.document.close(); 
  winId.focus(); 
  if (window.print) winId.print(); 
}

// close given window and focus his opener if any set
function closeFocusOpener(win){
  if (win.opener!=null){
    win.opener.focus();
  }
  win.close();
}

//function to open window with external URL
function openexternal(url) {
  temp=window.open(url,'EXTERNAL');
  temp.opener=this;
  temp.focus();
}

// open Periodical related search result in main window
function openperiodical(type, id) {
  switch (type) {
      case (1):
        url = "PShowPeriodical.do?id="
        break;
      case (2):
        url = "PShowVolume.do?id="
        break;
      case (3):
        url = "PShowIssue.do?id="
        break;
      case (4):
        url = "PShowICP.do?id="
        break;
      default:  
        alert("error in openperiodical JavaScript function");
      return false;
  }  
  this.opener.location = url + id;
  this.opener.focus();
}

// open Monograph related search result in main window
function openmonograph(type, id) {
  switch (type) {
      case (1):
        url = "MShowMonograph.do?id="
        break;
      case (2):
        url = "MShowUnit.do?id="
        break;
      case (3):
        url = "MShowMCP.do?id="
        break;
      default:  
        alert("error in openperiodical JavaScript function");
      return false;
  }  
  this.opener.location = url + id;
  this.opener.focus();
}

/* Check if pager index is valid (is int number and in allowed range) and return true if submit is needed.
 * @param index to check
 * @param maxindex maximal allowed index
 * @param actualindex don't submit if new index is same as actual
 * @param errtext text to be shown if index is not valid
 */
function checkPagerIndex(index, maxindex, actualindex, errtext){ 
  if (isNaN(index) || index < 1 || index > maxindex){
    alert(errtext);
    return false;
  } else {
    if (index == actualindex)
      return false;
    else   
      return true;
  }
}

// disable all fields in given form
function disable(form) {
  for (i=0; i<form.length; i++)
    form.elements[i].disabled = true;
}

// resize iframe/plugin  with name or id ="docframe" and "docframe2" to has height same as browser window content area
function resizeDocFrame(){
  if (document.docframe){
    var h = document.body.clientHeight;
    if (document.all){
      if (document.all['docframe'].style.height!=h)
        document.all['docframe'].style.height=h;
    } else {
      if (document.docframe.height!=h)
        document.docframe.height=h;
    }
  }
  if (document.docframe2){
    var h = document.body.clientHeight;
    if (document.all){
      if (document.all['docframe2'].style.height!=h)
        document.all['docframe2'].style.height=h;
    } else {
      if (document.docframe2.height!=h)
        document.docframe2.height=h;
    }
  }
  window.isResized = false;
}

window.isResized = false;

// add event handler for onresize if we are in page with docframe
if (document.all){
  window.attachEvent("onresize", window_onresize);
} else {
  window.addEventListener("resize", window_onresize, false);
}

// catch resize of window event to resize document file frame/plugin
function window_onresize(event){
  if (window.isResized==false){
    window.isResized = true;
    setTimeout('resizeDocFrame()', 200);
  }
}

// show prompt dialog with query and defaultanswer, if OK dialog button used, submit url+result from prompt to active document window
function promptAndSubmitResult(url, query, defanswer){
  var retVal = prompt(query,defanswer);
  if(retVal!=null)
    document.location.href=url+retVal;
}

function initExportSelection() {
  	  var mode;
	  var description;
	  var checkSplit;
	  var splitAlgorithm;
	  var changeSuffix;
	  var extensionRow;

		mode = document.getElementsByName('mode')[0];
		description = document.getElementById('descriptionRow');
		checkSplit = document.getElementsByName('splitCheck')[0];
		splitAlgorithm = document.getElementsByName('splitAlgorithm')[0];
		if (checkSplit.value=='true') {
			splitAlgorithm.style.display='';
			description.style.display='none';
		} else {
			splitAlgorithm.style.display='none';
			description.style.display='';
		}

		changeSuffix = document.getElementsByName('changeSuffixCheck')[0];
		extensionRow = document.getElementById('extensionRow');
		if (changeSuffix.value=='true') {
			extensionRow.style.display='';
		} else {
			extensionRow.style.display='none';
		}

		if (mode.options[mode.selectedIndex].value == '2') {
			if (document.getElementById('splitCheckRow')) {
				document.getElementById('splitCheckRow').style.display='none';
			}		
			if (document.getElementById('removeSiglaRow')) {
				document.getElementById('removeSiglaRow').style.display='none';
			}		
			if (document.getElementById('changeSuffixRow')) {
				document.getElementById('changeSuffixRow').style.display='none';
			}
		} else {
			if (document.getElementById('splitCheckRow')) {
				document.getElementById('splitCheckRow').style.display='';
			}		
			if (document.getElementById('removeSiglaRow')) {
				document.getElementById('removeSiglaRow').style.display='';
			}		
			if (document.getElementById('changeSuffixRow')) {
				document.getElementById('changeSuffixRow').style.display='';
			}
		}
		
}
