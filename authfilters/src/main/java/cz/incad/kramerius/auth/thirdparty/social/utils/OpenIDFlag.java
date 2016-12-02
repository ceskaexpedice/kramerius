package cz.incad.kramerius.auth.thirdparty.social.utils;

import java.util.Iterator;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.Configuration;
import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.SocialAuthConfig;
import org.brickred.socialauth.SocialAuthManager;
import org.brickred.socialauth.util.SocialAuthUtil;

import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public enum OpenIDFlag {
    
    
    UNTOUCHED {
        @Override
        public OpenIDFlag next(HttpServletRequest req) throws Exception {
            SocialAuthManager authManager = createSocialAuthManager();
            req.getSession(true).setAttribute(MANAGER_KEY, authManager);
            req.getSession(true).setAttribute(STATE_KEY, LOGIN_INITIALIZED.name());
            return LOGIN_INITIALIZED;
        }

        public SocialAuthManager createSocialAuthManager() throws Exception {
            SocialAuthConfig config = SocialAuthConfig.getDefault();
            Configuration confObject = KConfiguration.getInstance().getConfiguration();
            Iterator<String> keys = confObject.getKeys();
            Properties props = new Properties();
            while (keys.hasNext()) {
                String key = keys.next();
                if (key.startsWith("oauth.")) {
                    String value = confObject.getString(key);
                    String reducedKey = StringUtils.minus(key, "oauth.");
                    props.put(reducedKey, value);
                }
            }
            config.load(props);
            SocialAuthManager manager = new SocialAuthManager();
            manager.setSocialAuthConfig(config);
            return manager;
        }


    },
    LOGIN_INITIALIZED {

        @Override
        public OpenIDFlag next(HttpServletRequest req) throws Exception {
            SocialAuthManager authManager = this.authManager(req);
            AuthProvider provider = authManager.connect(SocialAuthUtil.getRequestParametersMap(req));
            Profile profile = provider.getUserProfile();
            req.getSession(true).setAttribute(PROFILE_KEY, profile);
            req.getSession(true).setAttribute(STATE_KEY, LOGGED.name());
            return LOGGED;
        }
        
    },
    LOGGED {
        @Override
        public OpenIDFlag next(HttpServletRequest req) throws Exception {
            req.getSession(true).setAttribute(STATE_KEY, STORED.name());
            return OpenIDFlag.STORED;
            
        }
    },
    
    STORED {
        @Override
        public OpenIDFlag next(HttpServletRequest req) throws Exception {
            req.getSession(true).setAttribute(STATE_KEY, STORED.name());
            return OpenIDFlag.STORED;
        }
    };

    public  SocialAuthManager authManager(HttpServletRequest req) throws Exception {
        Object attribute = req.getSession(true).getAttribute(MANAGER_KEY);
        return (SocialAuthManager) (attribute != null ? attribute : null);
    }

    public  Profile profile(HttpServletRequest req) throws Exception {
        Object attribute = req.getSession(true).getAttribute(PROFILE_KEY);
        return (Profile) (attribute != null ? attribute : null);
    }


    public abstract OpenIDFlag next(HttpServletRequest req) throws Exception;

    public static OpenIDFlag flagFromRequest(HttpServletRequest req) {
        String attribute = (String) req.getSession(true).getAttribute(STATE_KEY);
        if (attribute != null) {
            return OpenIDFlag.valueOf(attribute);
        } else return OpenIDFlag.UNTOUCHED;
    }
    
    public static final String PROFILE_KEY = "oauthIdProfile";
    public static final String MANAGER_KEY = "oauthIdManager";
    public static final String STATE_KEY = "oauthIdState";
}