package com.qbizm.kramerius.imptool.poc.valueobj;

import java.util.LinkedList;
import java.util.List;

/**
 * Value objekt pro naplneni dat RELS-EXT
 * 
 * @author xholcik
 */
public class RelsExt {

    
    public static final String ITEM_ID = "itemID";
    
    public static final String HAS_MODEL = "hasModel";

    public static final String HAS_UNIT = "hasUnit";

    public static final String HAS_PAGE = "hasPage";

    public static final String HAS_ITEM = "hasItem";

    public static final String HAS_INT_COMP_PART = "hasIntCompPart";

    public static final String HAS_VOLUME = "hasVolume";

    public static final String HAS_DONATOR = "hasDonator";

    public static final String IS_ON_PAGE = "isOnPage";

    public static final String POLICY = "policy";

    public static final String HANDLE = "handle";

    public static final String CONTRACT = "contract";
    
    public static final String FILE = "file";
    
    public static final String ISBN = "isbn";
    
    public static final String ISSN = "issn";
    
    public static final String EXTID = "extid";

    public static final String TILES_URL = "tiles-url";
    
    

    private final String pid;

    private final List<Relation> relations = new LinkedList<Relation>();

    public RelsExt(String pid, String model) {
        super();
        this.pid = pid;
        this.addRelation(HAS_MODEL, model, false);
        this.addRelation(ITEM_ID,pid,true);
    }

    public void addRelation(String key, String id, boolean literal) {
        if (id == null || "".equals(id))
            return;
        relations.add(new Relation(key, id, literal));
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public String getPid() {
        return pid;
    }

    public class Relation {

        private final String key;

        private final String id;

        private final boolean literal;

        public Relation(String key, String id, boolean literal) {
            super();
            this.key = key;
            this.id = id;
            this.literal = literal;
        }

        public String getKey() {
            return key;
        }

        public String getId() {
            return id;
        }

        public boolean isLiteral() {
            return literal;
        }

    }

}
