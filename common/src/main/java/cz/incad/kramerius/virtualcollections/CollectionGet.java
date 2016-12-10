package cz.incad.kramerius.virtualcollections;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;

// TODO: it will be replaced in next version;
// must find out some way how to throw out CollectionManager
// this is for getting collections and returning standarnd json array
public interface CollectionGet {
    
    public JSONArray collections();
}
