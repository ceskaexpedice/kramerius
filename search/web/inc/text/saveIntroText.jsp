<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.io.*, cz.incad.kramerius.TextsService"  %>
<%
            /*
            String text = request.getParameter("text");
            
            if(text==null){
            out.println("invalid text");
            }else{
            String lang = request.getParameter("language");
            if(lang==null || lang.length()==0){
            lang = "cs";
            }
            String nameOfTextFile = this.getServletContext().getRealPath("inc/text/intro_"+lang+".jsp");
            try {   
            PrintWriter pw = new PrintWriter(new FileOutputStream(nameOfTextFile));
            pw.println(text);
            pw.close();
            out.println("success");
            } catch(IOException e) {
            out.println(e.getMessage());
            }
            
            
            
            }
             */

            String text = request.getParameter("text");

            if (text == null) {
                out.println("invalid text");
            } else {
                String lang = request.getParameter("language");
                if (lang == null || lang.length() == 0) {
                    lang = "cs";
                }
                try {
                    TextsService.writeText("intro", lang, text);
                    out.println("success");
                } catch (Exception e) {
                    out.println(e.getMessage());

                }
            }
%>