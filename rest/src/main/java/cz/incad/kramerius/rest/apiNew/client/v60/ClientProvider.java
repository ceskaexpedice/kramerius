package cz.incad.kramerius.rest.apiNew.client.v60;

import javax.inject.Provider;

import com.sun.jersey.api.client.Client;

public class ClientProvider implements Provider<Client>{

	private Client client;
	
	@Override
	public Client get() {
		if (client == null) {
			client = Client.create();
		}
		return client;
	}
	
	
}
