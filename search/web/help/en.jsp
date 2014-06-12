<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page isELIgnored="false" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tlds/cmn.tld" prefix="view" %>

<view:object name="helpViewObject" clz="cz.incad.Kramerius.views.help.HelpViewObject"></view:object>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
    <title>Kramerius - Help</title>
    <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
    <META HTTP-EQUIV="Content-Language" CONTENT="cs">
    <META NAME="description" CONTENT="Help for Kramerius ">
    <META NAME="keywords" CONTENT="periodicals, library, book, publication, kramerius">

    <META NAME="Copyright" content="">
    <LINK REL="StyleSheet" HREF="../css/styles.css" type="text/css">
    <SCRIPT LANGUAGE="JavaScript" TYPE="text/javascript" src="add.js"></SCRIPT>
</head>
<body marginwidth="0" marginheight="0" leftmargin="0" topmargin="0">

<table id="help" class="header ui-corner-top-8" >
<tr><td> <img src="../img/logo.png" border="0" /></td>
<td width="500px"> </td> 

<td>version:${helpViewObject.version}</td>
<td>revision:<a href="https://github.com/ceskaexpedice/kramerius/commit/${helpViewObject.revision}"> ${fn:substring(helpViewObject.revision, 0, 4)}...</a></td></tr>

</tr>
</table>

<table cellpadding="0" cellspacing="0" border="0" height="100%">
  <tr>
    <td width="13" background="img/main_bg_grey.gif" height="100%"><img src="img/empty.gif" width="13" height="1" alt="" border="0"></td>
    <td valign="top" width="100%">

    <table cellpadding="0" cellspacing="0" border="0" width="100%">
      <tr>
        <td height="20" width="100%" class="mainHeadGrey"><div align="right"><a href="javascript:print();"   class="mainServ">PRINT</a>&nbsp; - &nbsp;<a href="javascript:openhelp=window.close();" class="mainServ">CLOSE WINDOW</a><a name="top">&nbsp;</a>&nbsp;</div></td>
      </tr>
      <tr>
        <td width="100%" colspan="3"><img src="img/empty.gif" width="1" height="2" alt="" border="0"></td>
      </tr>

      <tr>
        <td width="100%" class="mainHeadGrey"><img src="img/empty.gif" width="1" height="1" alt="" border="0"></td>
      </tr>
      <tr>
        <td valign="top" align="left" width="100%">
        <table cellpadding="0" cellspacing="0" border="0" width="100%">
          <tr>
            <td><img src="img/empty.gif" width="15" height="1" alt="" border="0"></td>
            <td>


<table cellpadding="0" cellspacing="0" border="0" width="100%">
<tr>
  <td class="mainHeadGrey" height="1"><img src="img/empty.gif" width="1" height="1" alt="" border="0"></td>
</tr>
</table>
<br><div align="right"><a href="#top" name="a" class="mainServ">TOP</a></div>
<strong>What do I find in the Digital library</strong><br><br>

    Digital library can contain digitized documents (e.g. rare documents digitized in the national programs
    Memoriae Mundi Series Bohemica  and Kramerius) or electronic documents created directly in the digital format.<p>
    Access to the digital library is restricted with access rights.
    Metadata, ie. bibliographical and other descriptions of the documents are available to anyone, as well as are the graphical data
    of the documents not protected by the copyright law. Graphical data protected by the copyright law are available only to users
    accessing the application from the computers at the premises of the library.
<br><br>
<table cellpadding="0" cellspacing="0" border="0" width="100%">
<tr>
  <td class="mainHeadGrey" height="1"><img src="img/empty.gif" width="1" height="1" alt="" border="0"></td>
</tr>
</table>
<br><div align="right"><a href="#top" name="b" class="mainServ">TOP</a></div>
<strong>What do I need to use the Digital library?</strong><br><br>
Any modern web browser, preferably Firefox 3, Safari or Google Chrome.
You need to install additional plugin module to display DjVu and PDF files.
The recommended display resolution is 1024x768 or more pixels.
<p>
<br><div align="right"><a href="#top" name="a" class="mainServ">TOP</a></div>
<strong>Main screen</strong> <br><br>
The main screen is used for searching the documents in the digital library. <p>
<b>Page header</b><p>
The large input field in the middle of the page header can be used to perform full text search in the digital library documents:
type in the desired word and click on the magnifying glass symbol. You can use the wildcard character * representing any part of the word or ? representing single letter.
You may use the logical operators AND, OR and NOT as well. To search for exact sentence enclose the required words with double quotes.<p>

The Advanced search link opens the form for searching in the documents metadata (MODS).
You can search by ISBN/ISSN, author, title, date of publishing and  MDC/DDC classifications.
You can also limit the selection to public documents only. The search is started by clicking the OK button.
The Advanced search window can be closed by clicking the Close button.<p>
In the right corner of the page header are the links to this help text, to switch the user interface language and to login to the administration services.<p>
<b>Time line box</b><p>
The Time line box show the histogram of the document counts published in each  year.
You can limit the search time interval by typing the dates into the fields at the top of the diagram, selecting them from popup calendars or by dragging the vertical handles.
Both limits can be confirmed by clicking at the magnifying glass symbol next to the From and To fields.<p>
<b>Authors and Titles tabs</b><p>
These tabs can be used for quick search of the document by it's title or author.
The list of found documents is continuously updated as you type in additional letters. The numbers in parentheses denote count of the corresponding documents. Clicking on the item in the list selects it for display.
The character case is ignored, diacritical marks must be entered properly.<p>
<b>Navigation tab</b><p>
Navigation tab contains boxes that allow to filter the search results according to the given criteria.
By clicking on any item in the box you can search the documents of the corresponding value.
The numbers in the parentheses next to each item show the count of the corresponding documents in the digital library.<p>
<b>Custom, Latest and Most desirable tabs</b><p>
The tab Latest contains the icons of title pages of the documents recently added to the digital library.
Click at the corresponding icon to display the required document.
Likewise, the tab Most desirable contains icons of the most frequently used documents.
The Custom tab contains icons of interesting documents selected by the application administrator.<p>
<br><br>
<br><div align="right"><a href="#top" name="a" class="mainServ">TOP</a></div>
<strong>Search results</strong> <br><br>
This page is diplayed after spawning the search from the main screen.
The right-side box contains the list of found documents grouped by the top-level titles (typically monographs or periodicals.)
Items inside the groups can be displayed by clicking at the count text in the lower right corner of the title frame.
Subsequent items in the list are loaded automatically by scrolling to the end of current list.
The titles can be sorted by relevance or alphabetically by clicking at the corresponding link at the top of the box.
Clicking the control in the top right corner of the box toggles between one and two columns display.<p>
The boxes at the left contain items for selection of the subcategories within the found documents. Their function is similar to the Navigation tab on the main screen.<p>
The left part of the screen is filled with the same time line box as the main screen.<br><br>

<br><div align="right"><a href="#top" name="a" class="mainServ">TOP</a></div>
<strong>Title display</strong><br><br>
The third level screen is used to display the document's metadata and to browse it's structure.<p>
At the top there is a bar with icons of all pages in the document or the selected part.
Icons can be scrolled by the arrows at the edges or by the scrollbar bellow.
You can view the thumbnail icons with the scrollbar, while clicking on the arrows (or pressing the arrow keys) switches the page preview.
The icon bar is not diplayed for the documents with the single PDF file.<p>
The center of the screen is filled with the preview of the selected page.<br>
If the access to the documet contents is restricted by the copyright law, the preview is replaced by the informational message.
Other tabs in the central area display MODS metadata for the selected item and possibly other data streams.<p>
The box to the left of the preview is used for browsing of the document structure.<br>
The document structure is displayed as a tree in the first tab.<br>
The second tab contains input box for fultext search within the currently selected document.<br>
The third tab of the box contains menu with operations that can be applied to the document selected in the first tab.<p>
The following operations are available:<br>
<b>Persistent URL:</b> Dsiplays URL of the document, that can be used as the bookmark for direct access to the document.<br>
<b>Generate PDF:</b> Allows to generate and download the PDF document with the page range starting with the selected page. The number of pages in the PDF document may be limited.<br>
<b>Download original</b> (only for pages): Downloads the full size scanned document.<br>
<b>METS record:</b> Opens the window with XML in the format Fedora METS for the selected document.<p>

<br><br>


<br><br>

            </td>
            <td><img src="img/empty.gif" width="15" height="1" alt="" border="0"></td>
          </tr>

        </table>
        </td>
      </tr>
    </table>
    </td>
  </tr>
</table>
</body>
</html>
