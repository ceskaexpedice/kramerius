package com.qbizm.kramerius.imptool.poc.valueobj;

import java.util.LinkedList;
import java.util.List;

/**
 * Value objekt pro naplneni dat RELS-EXT
 * 
 * @author xholcik
 */
public class RelsExt {

  public static final String HAS_MODEL = "hasModel";

  public static final String HAS_UNIT = "hasUnit";

  public static final String HAS_PAGE = "hasPage";

  public static final String HAS_ITEM = "hasItem";

  public static final String HAS_INT_COMP_PART = "hasIntCompPart";

  public static final String HAS_VOLUME = "hasVolume";

  public static final String IS_ON_PAGE = "isOnPage";

  private final String pid;

  private final List<Relation> relations = new LinkedList<Relation>();

  public RelsExt(String pid, String model) {
    super();
    this.pid = pid;
    this.addRelation(HAS_MODEL, model);
  }

  public void addRelation(String key, String id) {
    relations.add(new Relation(key, id));
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

    public Relation(String key, String id) {
      super();
      this.key = key;
      this.id = id;
    }

    public String getKey() {
      return key;
    }

    public String getId() {
      return id;
    }

  }

}
