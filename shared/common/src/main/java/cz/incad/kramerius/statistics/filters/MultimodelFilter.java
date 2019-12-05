package cz.incad.kramerius.statistics.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultimodelFilter implements  StatisticsFilter {

    public static List<String> SELECTED_MODELS = Arrays.asList(
            "monograph",
            "periodicalvolume",
            "supplement",
            "sheetmusic",
            "manuscript",
            "archive",
            "soundrecording",
            "graphic",
            "map"
    );

    private List<String> models  = SELECTED_MODELS;


    public List<String> getModels() {
        return this.models;
    }

    public void setModels(List<String> models) {
        this.models = models;
    }

}
