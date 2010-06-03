<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.io.*, cz.incad.kramerius.service.*"  %>
<%@page import="com.google.inject.Injector"%>

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

         	Injector inj = (Injector)application.getAttribute(Injector.class.getName());
         	TextsService ts = (TextsService)inj.getInstance(TextsService.class);	

            String text = request.getParameter("text");

            if (text == null) {
                out.println("invalid text");
            } else {
                String lang = request.getParameter("language");
                if (lang == null || lang.length() == 0) {
                    lang = "cs";
                }
                try {
                    ts.writeText("intro", ts.findLocale(lang), text);
                    out.println("success");
                } catch (Exception e) {
                    out.println(e.getMessage());

                }
            }
%>