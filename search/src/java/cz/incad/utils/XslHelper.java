package cz.incad.utils;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import java.io.IOException;

/**
 *
 * @author Alberto
 */


public class XslHelper {


    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    @Inject

    public boolean contains(String content, String query) {
        String simpleContent = removePunctuation(content);
        String[] words = query.split(" ");
        for (String word : words) {
            if (simpleContent.equalsIgnoreCase(removePunctuation(word))) {
                return true;
            }
        }
        return false;
    }

    private String removePunctuation(String s) {
        StringBuffer sb = new StringBuffer();
//Character.is
        for (int i = 0; i < s.length(); i++) {
//            if ((s.charAt(i) >= 65 && s.charAt(i) <= 90) || (s.charAt(i) >= 97
//                    && s.charAt(i) <= 122)) {
//
//                sb = sb.append(s.charAt(i));
//            }
            if(Character.isLetterOrDigit(s.charAt(i))) sb = sb.append(s.charAt(i));
        }
//        System.out.println(sb.toString());
        return sb.toString();
    }

    public String findFirstPagePid(String uuid){
            return FedoraUtils.findFirstPagePid("uuid:" + uuid);
    }

    public String findFirstViewablePid(String uuid) throws IOException{
            return fedoraAccess.findFirstViewablePid(uuid);
    }
}
