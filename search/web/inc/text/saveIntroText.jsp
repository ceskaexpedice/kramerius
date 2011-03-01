<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.io.*, cz.incad.kramerius.service.*"  %>
<%@page import="com.google.inject.Injector"%>
<%

            Injector inj = (Injector) application.getAttribute(Injector.class.getName());
            TextsService ts = (TextsService) inj.getInstance(TextsService.class);

            String text = request.getParameter("text");

            out.clear();
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