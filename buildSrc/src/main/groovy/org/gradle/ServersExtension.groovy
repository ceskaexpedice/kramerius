package org.gradle;

import org.gradle.api.*;
import org.gradle.api.plugins.cargo.*;
//import org.gradle.api.plugins.cargo.tasks.remote.CargoDeployRemote;
//import org.gradle.api.plugins.cargo.tasks.remote.CargoUndeployRemote;


public class ServersExtension {
		    
	private List<Server> servers = new ArrayList<Server>();
        private Project project;

        public ServersExtension(Project prj) {
                this.project = prj;
        }

        public boolean checkProperty(String ... args) {
		for (String arg: args) {
			if (!this.project.hasProperty(arg)) return false;
		}

		return true;
        }
		
		/*
        public void http(String ident, String address,String portKey, String unameKey, String pswdKey) {
                if (checkProperty(unameKey, pswdKey,portKey)) {
                        String uname = this.project.property(unameKey);
                        String pswd = this.project.property(pswdKey);
                        String port = this.project.property(portKey);
                        server(new Server(ident,"http",port,address,uname,pswd,"tomcat7x"));
                }
        }

        public void http(String ident, String address, String portKey, String unameKey, String pswdKey, String containerId) {
                if (checkProperty(unameKey, pswdKey,portKey)) {
                        String uname = this.project.property(unameKey);
                        String pswd = this.project.property(pswdKey);
                        String port = this.project.property(portKey);
                        server(new Server(ident,"http",port,address,uname,pswd, containerId));
                }
        }

        public void https(String ident, String address, String portKey, String unameKey, String pswdKey) {
                if (checkProperty(unameKey, pswdKey,portKey)) {
                        String uname = this.project.property(unameKey);
                        String pswd = this.project.property(pswdKey);
                        String port = this.project.property(portKey);
                        server(new Server(ident,"https",port,address,uname,pswd,"tomcat7x"));
                }
        }

        public void https(String ident, String address, String portKey, String unameKey, String pswdKey, String containerId) {
                if (checkProperty(unameKey, pswdKey, portKey)) {
                        String uname = this.project.property(unameKey);
                        String pswd = this.project.property(pswdKey);
                        String port = this.project.property(portKey);
                        server(new Server(ident,"https",port,address,uname,pswd, containerId));
                }
        }


        void server(final Server srv) {
                this.servers.add(srv);
                
                char firstLetter = Character.toUpperCase(srv.ident.charAt(0));
                String ident = (""+firstLetter) +  srv.ident.substring(1);
                
                CargoDeployRemote deploy = project.getTasks().create("deploy"+ident, CargoDeployRemote.class);                
                deploy.port = Integer.parseInt(srv.port);
                deploy.protocol = srv.protocol;
                deploy.hostname = srv.address;
                deploy.username = srv.userName;
                deploy.password = srv.password;
                deploy.containerId = srv.containerId;        

                CargoUndeployRemote undeploy = project.getTasks().create("undeploy"+ident, CargoUndeployRemote.class);                
                undeploy.port = Integer.parseInt(srv.port);
                undeploy.protocol = srv.protocol;
                undeploy.hostname = srv.address;
                undeploy.username = srv.userName;
                undeploy.password = srv.password;
                undeploy.containerId = srv.containerId;        
        } 
		 */

        public List<Server> getServers() {
                return this.servers;
        }
}


class Server {

        private String ident;        
        private String protocol;

        private String port = "8080";

	private String address;
	private String userName;
        private String password;
        private String containerId;

        
        public Server(String ident, String protocol, String port, String address, String userName, String password, String containerId) {
                this.port = port;
                this.containerId = containerId;
                this.protocol = protocol;
                this.ident = ident;
                this.address = address;
                this.userName = userName;
                this.password = password;
        }


                

        public String getPort() {
                return this.port;
        }

        public String getProtocol() {
                return this.protocol;
        }

        public String getIdent() {
                return this.ident;
        }
        
        public String getAddress() {
                return this.address;
        }

        public void setAddress(String ad) {
                this.address = ad;        
        }

        public String getUserName() {
                return this.userName;
        }
        
        public void setUserName(String u) {
                this.userName = u;
        }

        public String getPassword() {
                return this.password;
        }
        
        public void setPassword(String pswd) {
                this.password = pswd;
        }
}
