package cz.incad.Kramerius.views.inc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.MostDesirable;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class MostDesirableViewObject {

    public static final Logger LOGGER = Logger.getLogger(MostDesirableViewObject.class.getName());
    
    public static final int NUMBER_OF_DESIRABLE_ITEMS = 18;
    
    @Inject
    MostDesirable mostDesirable;


    
    public List<String> getPids() {

        List<String> pids = new ArrayList<String>();
        List<String> list = KConfiguration.getInstance().getConfiguration().getList("most.desirable.models", 
                Arrays.asList(
                    "monograph",
                    "periodical",
                    "soundrecording",
                    "manuscript",
                    "map",
                    "sheetmusic",
                    "volume",
                    "periodicalitem",
                    "monographunit",
                    "internalpart",
                    "article",
                    "supplement",
                    "page")
        );

        for (String model : list) {
            List<String> found = this.mostDesirable.getMostDesirable(NUMBER_OF_DESIRABLE_ITEMS, 0, model);
            pids.addAll(found);
            if (pids.size() >= NUMBER_OF_DESIRABLE_ITEMS) {
                break;
            }
        }
        return pids;
    }
}
