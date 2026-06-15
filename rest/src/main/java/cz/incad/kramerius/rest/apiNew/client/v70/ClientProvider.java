package cz.incad.kramerius.rest.apiNew.client.v70;

import jakarta.inject.Provider;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

public class ClientProvider implements Provider<Client> {

	private Client client;

	@Override
	public Client get() {
		if (client == null) {
			client = ClientBuilder.newClient();
		}
		return client;
	}
}