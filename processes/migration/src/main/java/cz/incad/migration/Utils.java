package cz.incad.migration;

import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {

    public static Logger LOGGER = Logger.getLogger(Utils.class.getName());

    static DocumentBuilderFactory FACTORY;
    static DocumentBuilder BUILDER;
    static {
        try {
            FACTORY = DocumentBuilderFactory.newInstance();
            FACTORY.setNamespaceAware(true);
            BUILDER = FACTORY.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    static DOMImplementationRegistry DOMIMPLREG;
    static LSSerializer SERIALIZER;
    static DOMImplementationLS DOMIMPL;
    static {
        try {
            DOMIMPLREG = DOMImplementationRegistry.newInstance();
            DOMIMPL = (DOMImplementationLS) DOMIMPLREG.getDOMImplementation("LS");
            SERIALIZER = DOMIMPL.createLSSerializer();
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    static MessageDigest MD5;
    static {
        try {
            MD5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }


    public static String encode(String uri) {
        StringBuilder out = new StringBuilder();
        // encode char-by-char because we only want to borrow
        // URLEncoder.encode's behavior for some characters
        for (int i = 0; i < uri.length(); i++) {
            char c = uri.charAt(i);
            if (c >= 'a' && c <= 'z') {
                out.append(c);
            } else if (c >= '0' && c <= '9') {
                out.append(c);
            } else if (c >= 'A' && c <= 'Z') {
                out.append(c);
            } else if (c == '-' || c == '=' || c == '(' || c == ')'
                    || c == '[' || c == ']' || c == ';') {
                out.append(c);
            } else if (c == ':') {
                out.append("%3A");
            } else if (c == ' ') {
                out.append("%20");
            } else if (c == '+') {
                out.append("%2B");
            } else if (c == '_') {
                out.append("%5F");
            } else if (c == '*') {
                out.append("%2A");
            } else if (c == '.') {
                if (i == uri.length() - 1) {
                    out.append("%2E");
                } else {
                    out.append('.');
                }
            } else {
                try {
                    out.append(URLEncoder.encode(Character.toString(c), "UTF-8"));
                } catch (UnsupportedEncodingException wontHappen) {
                    throw new RuntimeException(wontHappen);
                }
            }
        }
        return out.toString();
    }

    public static String asHex(byte[] var0) {
        StringBuffer var1 = new StringBuffer(var0.length * 2);

        for(int var2 = 0; var2 < var0.length; ++var2) {
            if ((var0[var2] & 255) < 16) {
                var1.append("0");
            }

            var1.append(Long.toString((long)(var0[var2] & 255), 16));
        }

        return var1.toString();
    }



    static File directory(File targetDir, String hex, int patternLength, int numberOfPatterns) {
        List<String> list = new ArrayList<>();
        for(int i=0;i<numberOfPatterns;i++) {
            int from = i*patternLength;
            int to = from + patternLength;
            list.add(hex.substring(from, to));
        }
        File directory = new File(targetDir.getAbsoluteFile()+"/"+list.stream().reduce("",(i, c)->{
            return i+"/"+c;
        }));
        return directory;
    }


}

