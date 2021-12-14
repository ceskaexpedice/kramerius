package cz.incad.kramerius.auth.thirdparty.keycloack;

import cz.incad.kramerius.auth.thirdparty.impl.AbstractThirdPartyUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Keycloack3rdUser extends AbstractThirdPartyUser {

    private String hash;

    private List<String> roles = new ArrayList<>();

    public Keycloack3rdUser(String h) {
        this.hash = h;
    }

    @Override
    public String getCalculatedName() {
        return this.hash;
    }

    @Override
    public String getProperty(String key) {
        return null;
    }

    @Override
    public Set<String> getPropertyKeys() {
        return null;
    }

    public void setRoles(List<String> rls) {
        this.roles = rls;
    }

    public List<String> getRoles() {
        return roles;
    }


}
