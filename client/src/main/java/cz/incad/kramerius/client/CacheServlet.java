/*
 * Copyright (C) 2014 Alberto Hernandez
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.client;


import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;

public class CacheServlet extends HttpServlet {
    public static final Logger LOGGER = Logger.getLogger(CacheServlet.class.getName());
    private void proccesRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        resp.setContentType("text/plain;charset=utf-8");
        if("save".equals(action)){
            saveToFile(req, resp);
        }else if("get".equals(action)){
            loadFromFile(req, resp);
        }else{
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    private String getPath(){
        String path = System.getProperty("user.home") + File.separator + ".kramerius4" + 
                    File.separator + "k5client" + File.separator + 
                    File.separator + "cache" + File.separator;
        File destFolder = new File(path);
        if (!destFolder.exists()) destFolder.mkdirs();
        return path;
    }
    
    private void saveToFile(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        try {
            String filename = getPath() + req.getParameter("f");
            String content = req.getParameter("c");
            File f = new File(filename);
            FileUtils.writeStringToFile(f, content, "UTF-8");
            
            resp.getWriter().write("File saved");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not save file: " + e.toString());
        }
    }
    
    private void loadFromFile(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        
        try {
            String filename = getPath() + req.getParameter("f");
            File f = new File(filename);
            resp.getWriter().write(FileUtils.readFileToString(f, "UTF-8"));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not load file: " + e.toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        proccesRequest(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        proccesRequest(req, resp);
    }
}
