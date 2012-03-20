package org.kramerius.importmets.valueobj;

import java.util.ArrayList;
import java.util.List;

import org.kramerius.dc.OaiDcType;
import org.kramerius.mods.ModsDefinition;

public class Foxml {

    private String pid;
    private String title;
    private ModsDefinition mods;
    private OaiDcType dc;
    private RelsExt re;
    private List<ImageRepresentation> files;

    public String getPid() {
        return pid;
    }
    public void setPid(String pid) {
        this.pid = pid;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public ModsDefinition getMods() {
        return mods;
    }
    public void setMods(ModsDefinition mods) {
        this.mods = mods;
    }
    public OaiDcType getDc() {
        return dc;
    }
    public void setDc(OaiDcType dc) {
        this.dc = dc;
    }
    public RelsExt getRe() {
        return re;
    }
    public void setRe(RelsExt re) {
        this.re = re;
    }
    public List<ImageRepresentation> getFiles() {
        return files;
    }
    public void addFiles(ImageRepresentation file) {
        if (this.files == null){
            this.files = new ArrayList<ImageRepresentation>();
        }
        this.files.add(file);
    }

}
