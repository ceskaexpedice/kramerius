<%
// same as redirect not not secured; useful for third party authentication
%>

<%
String reqAddr = "search.jsp";
if (request.getParameter("redirectURL")!=null) {
    reqAddr = request.getParameter("redirectURL");
}
// no redirect with error
reqAddr = reqAddr.replace("?error=accessdenied","");
response.sendRedirect(reqAddr);
%>

