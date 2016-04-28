package cz.incad.kramerius.auth;

import javax.servlet.Filter;

public interface ExtAuthFilter extends Filter {

    public static final String THIRD_PARTY_AUTHENTICATED_USER_KEY = "third_party_user";
}
