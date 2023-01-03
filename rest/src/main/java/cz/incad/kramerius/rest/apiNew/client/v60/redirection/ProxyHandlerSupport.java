package cz.incad.kramerius.rest.apiNew.client.v60.redirection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public abstract class ProxyHandlerSupport {

	public static final boolean DEBUG = false;
	
	public static final Logger LOGGER = Logger.getLogger(ProxyHandlerSupport.class.getName());

	protected String source;
	protected Client client;
	protected SolrAccess solrAccess;
	protected User user;
	protected String remoteAddr;
	protected Instances instances;

	public ProxyHandlerSupport(Instances instances, User user, Client client, SolrAccess solrAccess, String source,
			String remoteAddr) {
		this.source = source;
		this.client = client;
		this.solrAccess = solrAccess;
		this.user = user;
		this.remoteAddr = remoteAddr;
		this.instances = instances;
	}

	public Response buildRedirectResponse(String url) throws ProxyHandlerException {
		try {
			LOGGER.info(String.format("Redirecting to %s", url));
			return Response.temporaryRedirect(new URL(url).toURI()).build();
		} catch (MalformedURLException | URISyntaxException e) {
			throw new ProxyHandlerException(e);
		}
	}

	/**
	 * Build rewsponse with HEAD method
	 * @param url
	 * @return
	 * @throws ProxyHandlerException
	 */
	public Response buildForwardResponseHEAD(String url) throws ProxyHandlerException {
		WebResource.Builder b = buidFowrardResponse(url);
		ClientResponse clientResponseHead = b.head();
		if (clientResponseHead.getStatus() == 200) {
			return Response.status(200).build();
		} else {
			return Response.status(clientResponseHead.getStatus()).build();
		}
	}

	public ClientResponse forwardedResponse(String url) throws ProxyHandlerException {
		WebResource.Builder b = buidFowrardResponse(url);
		ClientResponse response = b.get(ClientResponse.class);
		if (response.getStatus() == 200) {
			return response;
		} else {
			throw new ProxyHandlerException("Bad response; status code "+response.getStatus());
		}
	}
	
	public Response buildForwardResponseGET(String url) throws ProxyHandlerException {
		WebResource.Builder b = buidFowrardResponse(url);
		ClientResponse response = b.get(ClientResponse.class);
		if (response.getStatus() == 200) {

			InputStream is = response.getEntityInputStream();
			MultivaluedMap<String, String> headers = response.getHeaders();

			StreamingOutput stream = new StreamingOutput() {
				public void write(OutputStream output) throws IOException, WebApplicationException {
					try {
						IOUtils.copy(is, output);
					} catch (Exception e) {
						throw new WebApplicationException(e);
					}
				}
			};

			ResponseBuilder respEntity = Response.status(200).entity(stream);
			headers.keySet().forEach(key -> {
				List<String> values = headers.get(key);
				values.stream().forEach(val -> {
					respEntity.header(key, val);
				});
			});

			return respEntity.build();
		} else {
			return Response.status(response.getStatus()).build();
		}
	}

	protected void mockSession() {
		if (!user.getSessionAttributes().containsKey("shib-session-id")) {
			this.user.addSessionAttribute("shib-session-id", "_dd68cbd66641c9b647b05509ac0241fa");
		}
		if (!user.getSessionAttributes().containsKey("shib-session-expires")) {
			this.user.addSessionAttribute("shib-session-expires", "1592847906");
		}
		if (!user.getSessionAttributes().containsKey("shib-identity-provider")) {
			this.user.addSessionAttribute("shib-identity-provider",
					"https://shibboleth.mzk.cz/simplesaml/metadata.xml");
		}
		if (!user.getSessionAttributes().containsKey("shib-authentication-method")) {
			this.user.addSessionAttribute("shib-authentication-method",
					"urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");
		}

		if (!user.getSessionAttributes().containsKey("shib-handler")) {
			this.user.addSessionAttribute("shib-handler", "https://dnnt.mzk.cz/Shibboleth.sso");
		}
		if (!user.getSessionAttributes().containsKey("remote_user")) {
			this.user.addSessionAttribute("remote_user", "all_users@mzk.cz");
		}
		if (!user.getSessionAttributes().containsKey("affiliation")) {
			this.user.addSessionAttribute("affiliation", "member@mzk.cz");
		}
		if (!user.getSessionAttributes().containsKey("entitlement")) {
			this.user.addSessionAttribute("entitlement", "cokoliv");
		}
		if (!user.getSessionAttributes().containsKey("edupersonuniqueid")) {
			this.user.addSessionAttribute("edupersonuniqueid", "user@mzk.cz");
		}
	}

	protected WebResource.Builder buidFowrardResponse(String url) {
		String prefixHeaders = KConfiguration.getInstance().getConfiguration().getString("cdk.shibboleth.forward.headers");
		
		Map<String, String> attributes = this.user.getSessionAttributes();
		String header = attributes.keySet().stream().map(key -> {
			return "header_" + key + "=" + attributes.get(key);
		}).collect(Collectors.joining("|"));

		if (this.remoteAddr != null) {
			header = header + "|" + "header_ip_address=" + this.remoteAddr;
		}
		
		if (StringUtils.isAnyString(prefixHeaders)) {
			header = prefixHeaders+header;
		}
		
		LOGGER.fine(String.format("Requesting %s", url));
		WebResource r = client.resource(url);
		LOGGER.info("CDK_TOKEN_PARAMETERS = "+header);
		return r.header("CDK_TOKEN_PARAMETERS", header);
	}

	protected String baseUrl() {
		String baseurl = KConfiguration.getInstance().getConfiguration()
				.getString("cdk.collections.sources." + this.source + ".baseurl");
		return baseurl;
	}
}