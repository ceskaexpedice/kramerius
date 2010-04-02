package cz.incad;

import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

import org.fedora.api.Datastream;
import org.fedora.api.FedoraAPIM;
import org.fedora.api.FedoraAPIMService;
import org.fedora.api.ObjectFactory;

import cz.incad.utils.LoggingHandler;
import cz.incad.utils.WSSupport;

public class TestWS {

	public static void main(String[] args) {
		FedoraAPIMService service = null;
		FedoraAPIM port = null;
		Authenticator.setDefault(new Authenticator() { 
	        protected PasswordAuthentication getPasswordAuthentication() { 
	           return new PasswordAuthentication("fedoraAdmin", "fedoraAdmin".toCharArray()); 
	         }
        });
	
        String spec = "http://localhost:8080/fedora/wsdl?api=API-M";
	    try {
			service = new FedoraAPIMService(new URL(spec),
	                new QName("http://www.fedora.info/definitions/1/0/api/", "Fedora-API-M-Service"));
	    } catch (MalformedURLException e) {
	        System.out.println(e);
	        e.printStackTrace();
	    }
	    port = service.getPort(FedoraAPIM.class);
	    
	    ((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "fedoraAdmin");
	    ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "fedoraAdmin");
	    Binding binding = ((BindingProvider) port).getBinding();
	    List<Handler> chain = binding.getHandlerChain();
	    if (chain == null) {
	    	chain = new ArrayList<Handler>();
	    }
    	chain.add(new LoggingHandler());
	    binding.setHandlerChain(chain);
	   
	    Datastream ds = port.getDatastream("uuid:774b8240-9280-11de-a08b-000d606f5dc6", "DC", null);
	    System.out.println(ds.getChecksum());
	    System.out.println(ObjectFactory.class.getResource("ObjectFactory.class"));
	}
}
