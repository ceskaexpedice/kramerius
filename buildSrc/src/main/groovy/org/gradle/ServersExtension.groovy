package org.gradle;

import org.gradle.api.*;
import org.gradle.api.plugins.cargo.*;
import org.gradle.api.plugins.cargo.tasks.remote.CargoDeployRemote;
import org.gradle.api.plugins.cargo.tasks.remote.CargoUndeployRemote;


public class ServersExtension {
		    
	private List<Server> servers = new ArrayList<Server>();
        private Project project;

        public ServersExtension(Project prj) {
                this.project = prj;
        }

        public boolean checkProperty(String uname, String pswd) {
                boolean unameFlag = this.project.hasProperty(uname);
                boolean pswdFlag =  this.project.hasProperty(pswd)               
                return unameFlag && pswdFlag;
        }

        public void http(String ident, String address,int port, String unameKey, String pswdKey) {
                if (checkProperty(unameKey, pswdKey)) {
                        String uname = this.project.property(unameKey);
                        String pswd = this.project.property(pswdKey);
                        server(new Server(ident,"http",port,address,uname,pswd,"tomcat7x"));
                }
        }

        public void http(String ident, String address, int port, String unameKey, String pswdKey, String containerId) {
                if (checkProperty(unameKey, pswdKey)) {
                        String uname = this.project.property(unameKey);
                        String pswd = this.project.property(pswdKey);
                        server(new Server(ident,"http",port,address,uname,pswd, containerId));
                }
        }

        public void https(String ident, String address, int port, String unameKey, String pswdKey) {
                if (checkProperty(unameKey, pswdKey)) {
                        String uname = this.project.property(unameKey);
                        String pswd = this.project.property(pswdKey);
                        server(new Server(ident,"https",port,address,uname,pswd,"tomcat7x"));
                }
        }

        public void https(String ident, String address, int port, String unameKey, String pswdKey, String containerId) {
                if (checkProperty(unameKey, pswdKey)) {
                        String uname = this.project.property(unameKey);
                        String pswd = this.project.property(pswdKey);
                        server(new Server(ident,"https",port,address,uname,pswd, containerId));
                }
        }


        void server(final Server srv) {
                this.servers.add(srv);
                
                char firstLetter = Character.toUpperCase(srv.ident.charAt(0));
                String ident = (""+firstLetter) +  srv.ident.substring(1);
                
                CargoDeployRemote deploy = project.getTasks().create("deploy"+ident, CargoDeployRemote.class);                
                deploy.port = srv.port;
                deploy.protocol = srv.protocol;
                deploy.hostname = srv.address;
                deploy.username = srv.userName;
                deploy.password = srv.password;
                deploy.containerId = srv.containerId;        

                CargoUndeployRemote undeploy = project.getTasks().create("undeploy"+ident, CargoUndeployRemote.class);                
                undeploy.port = srv.port;
                undeploy.protocol = srv.protocol;
                undeploy.hostname = srv.address;
                undeploy.username = srv.userName;
                undeploy.password = srv.password;
                undeploy.containerId = srv.containerId;        
        }        

        public List<Server> getServers() {
                return this.servers;
        }
}


class Server {

        private String ident;        
        private String protocol;

        private int port = 8080;

	private String address;
	private String userName;
        private String password;
        private String containerId;

        
        public Server(String ident, String protocol, int port, String address, String userName, String password, String containerId) {
                this.port = port;
                this.containerId = containerId;
                this.protocol = protocol;
                this.ident = ident;
                this.address = address;
                this.userName = userName;
                this.password = password;
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
