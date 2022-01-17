package cz.kramerius.searchIndex.indexer;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.ContextAwareAuthScheme;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PreemptiveAuthInterceptor implements HttpRequestInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(PreemptiveAuthInterceptor.class);

    protected ContextAwareAuthScheme authScheme = new BasicScheme();

    @Override
    public void process(final HttpRequest request, final HttpContext context)
            throws HttpException, IOException {
        final AuthState authState = (AuthState) context.getAttribute(HttpClientContext.TARGET_AUTH_STATE);

        if (authState != null && authState.getAuthScheme() == null) {
            final CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(HttpClientContext.CREDS_PROVIDER);
            final HttpHost targetHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
            final Credentials creds = credsProvider.getCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()));
            if (creds == null) {
                //ignore because the PreemptiveAuthInterceptor is added even to nonauthenticated client HttpClientUtil.addRequestInterceptor(new PreemptiveAuthInterceptor());
                //throw new HttpException("No credentials for preemptive authentication");
            } else {
                request.addHeader(authScheme.authenticate(creds, request, context));
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("authState is null. No preemptive authentication.");
        }
    }

    public ContextAwareAuthScheme getAuthScheme() {
        return authScheme;
    }

    public void setAuthScheme(final ContextAwareAuthScheme authScheme) {
        this.authScheme = authScheme;
    }

}