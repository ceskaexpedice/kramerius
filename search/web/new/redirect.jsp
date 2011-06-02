<%
String reqAddr = "s.jsp";
if (request.getParameter("redirectURL")!=null) {
    //reqAddr = request.getParameter("redirectURL");
}
response.sendRedirect(reqAddr);
%>

