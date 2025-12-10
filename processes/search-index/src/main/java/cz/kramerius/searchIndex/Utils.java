package cz.kramerius.searchIndex;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

public class Utils {

    /**
     * @param password
     * @return
     * @throws NoSuchAlgorithmException
     * @see <a href="http://www.planetcobalt.net/sdb/solr_password_hash.shtml">http://www.planetcobalt.net/sdb/solr_password_hash.shtml</a>
     */
    public static String buildHashOfSaltAndPasswordForSolr(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        final Random r = new SecureRandom();
        byte[] salt = new byte[32];
        r.nextBytes(salt);

        digest.reset();
        digest.update(salt);
        byte[] btPass = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        digest.reset();
        btPass = digest.digest(btPass);

        return Base64.encodeBase64String(btPass) + " " + Base64.encodeBase64String(salt);
    }

}
