package cz.incad.kramerius.auth.thirdparty.social.utils;

import org.brickred.socialauth.Profile;

import cz.incad.kramerius.auth.thirdparty.impl.AbstractThirdPartyUser;
import cz.incad.kramerius.security.utils.UserUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class OpenId3rdUser extends AbstractThirdPartyUser {

    private Profile p;
    private String calcName;
    private final Map<String, String> overrides = new HashMap<>();

    public OpenId3rdUser(String calcName, Profile p) {
        super();
        this.p = p;
        this.calcName = calcName;
    }

    @Override
    public String getCalculatedName() {
        return this.calcName;
    }

    @Override
    public Set<String> getPropertyKeys() {
        Set<String> keys = new HashSet<>(Arrays.asList(UserUtils.FIRST_NAME_KEY, UserUtils.LAST_NAME_KEY));
        keys.addAll(overrides.keySet());
        return keys;
    }

    @Override
    public String getProperty(String key) {
        if (overrides.containsKey(key)) return overrides.get(key);
        if (key.equals(UserUtils.FIRST_NAME_KEY)) {
            return p.getFirstName();
        } else if (key.equals(UserUtils.LAST_NAME_KEY)) {
            return p.getLastName();
        } else
            return null;
    }

    @Override
    public void setProperty(String key, String value) {
        overrides.put(key, value);
    }
}
