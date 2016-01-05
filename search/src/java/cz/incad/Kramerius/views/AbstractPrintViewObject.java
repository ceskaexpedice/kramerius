/*
 * Copyright (C) 2010 Pavel Stastny
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.Kramerius.views;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.Initializable;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.model.DCConent;
import cz.incad.kramerius.document.model.utils.DCContentUtils;
import cz.incad.kramerius.document.model.utils.DescriptionUtils;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class AbstractPrintViewObject extends AbstractViewObject implements Initializable {

    protected List<RadioItem> items = new ArrayList<RadioItem>();

    @Named("securedFedoraAccess")
    @Inject
    protected FedoraAccess fedoraAccess;


    @Inject
    protected TextsService textsService;

    @Inject
    protected Locale locale;

    @Inject
    protected ResourceBundleService resourceBundleService;

    @Inject
    protected SolrAccess solrAccess;

    protected String header;
    protected String desc;


    @Override
    public void init() {
        try {

            SelectionRadioItem selection = new SelectionRadioItem("selection", true);
          
            List params = getPidsParams();
            for (int i = 0; i < params.size(); i++) {
                Object pid = params.get(i);
                if (this.fedoraAccess.isImageFULLAvailable(pid.toString())) {
                    selection.addPid(pid.toString());
                } else {
                    String id = pid.toString().replace(":", "_");
                    MasterRadioItem radioItem = new MasterRadioItem(i+"_"+id, false);
                    radioItem.addPid(pid.toString());
                    this.items.add(radioItem);
                }
            }

            if (!selection.getPids().isEmpty()) {
                this.items.add(0,selection);
            }

            boolean checked = false;
            for (RadioItem itm : this.items) {
                if (itm.isChecked()) {
                    checked = true;
                    break;
                }
            }

            if ((!checked) && (!this.items.isEmpty())) {
                this.items.get(0).setChecked(true);
            }

            String xml = this.textsService.getText("first_page_nolines_xml", this.locale);
            Document doc = XMLUtils.parseDocument(new ByteArrayInputStream(xml.getBytes("UTF-8")), false);
            Element head = XMLUtils.findElement(doc.getDocumentElement(), "head");
            Element desc = XMLUtils.findElement(doc.getDocumentElement(), "desc");
            this.header = head.getTextContent();
            this.desc = desc.getTextContent();
        } catch (RecognitionException e) {
            throw new RuntimeException(e);
        } catch (TokenStreamException e) {
            throw new RuntimeException(e);
        } catch (DOMException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }

    }



    public String getHeader() throws RecognitionException, TokenStreamException, IOException, ParserConfigurationException, SAXException {
        return header;
    }

    public String getDesc() throws RecognitionException, TokenStreamException, IOException, ParserConfigurationException, Exception {
        return desc;
    }

    public List<RadioItem> getItems() throws RecognitionException, TokenStreamException, IOException, ParserConfigurationException, SAXException {
        return items;
    }

    
    public abstract class RadioItem {

        protected List<String> pids;
        protected String id;
        protected boolean checked = false;

        public RadioItem( String id, boolean checked) {
            super();
            this.pids = new ArrayList<String>();
            this.id = id;
            this.checked = checked;
        }

        public boolean isChecked() {
            return this.checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public String getCheckedAttribute() {
            return this.checked ? " checked='checked' " : "";
        }

        public void addPid(String pid) {
            this.pids.add(pid);
        }

        public void removePid(String pid) {
            this.pids.remove(pid);
        }


        public String getId() {
            return id;
        }

        public List<String> getPids() {
            return new ArrayList<String>(this.pids);
        }


        public Map<String, List<DCConent>> getDCS() throws IOException {
            return DCContentUtils.getDCS(fedoraAccess, solrAccess, getPids());
        }


        public String[] getDescriptions() throws IOException {
            return DescriptionUtils.getDescriptions(AbstractPrintViewObject.this.resourceBundleService.getResourceBundle("labels", locale), getDCS(), getPids());
        }        

        public boolean isDescriptionDefined() {
            // more heuristic
            return !isPidsMoreThenOne();
        }

        public boolean isPidsMoreThenOne() {
            return getPids().size() > 1;
        }

        public String[] getDetailedItemNames() throws IOException {
            ResourceBundle resBundle = resourceBundleService.getResourceBundle("labels", locale);
            if (isPidsMoreThenOne()) {
                List<String> names = new ArrayList<String>();
                for (String pid : this.pids) {
                    names.add(getDetailedItemName(resBundle, pid));
                }
                return (String[]) names.toArray(new String[names.size()]);
            } else return new String[0];
        }

        public String getName() throws IOException {
            ResourceBundle resBundle = resourceBundleService.getResourceBundle("labels", locale);
            if (isPidsMoreThenOne()) {
                // vice pidu -> napis a podtim seznam polozek
                Map<String, List<DCConent>> dcs = getDCS();
                List<String> pids = getPids();
                if (pids.size() == 1) {
                    List<DCConent> contents = dcs.get(pids.get(0));
                    DCConent content = contents.get(0);
                    return content.getTitle();
                } else {
                    
                    List<String> models = new ArrayList<String>();
                    for (String pid : pids) {
                        DCConent dc = dcs.get(pid).get(0);
                        String type = dc.getType();
                        if (!models.contains(type)) { models.add(type); }
                    }


                    StringBuilder builder = new StringBuilder();
                    for (int i = 0,ll=models.size(); i < ll; i++) {
                        if (i > 0) builder.append(",");
                        builder.append(resBundle.getString("fedora.model."+models.get(i)+"s"));
                    }

                    if (builder.length() > 0) builder.append(": ");

                    String first = pids.get(0);
                    String last = pids.get(pids.size() - 1);

                    builder.append(dcs.get(first).get(0).getTitle()).append(" - ");
                    builder.append(dcs.get(last).get(0).getTitle());

                    return builder.toString();
                }

            } else {
                String onePid = getPids().get(0);
                return getDetailedItemName(resBundle, onePid);
            }

        }

        public String getDetailedItemName(ResourceBundle resBundle, String onePid) throws IOException {
            StringBuilder builder = new StringBuilder();
            List<DCConent> contents = getDCS().get(onePid);
            for (int i = 0,ll=contents.size(); i < ll; i++) {
                DCConent dcConent = contents.get(i);
                String model = dcConent.getType();
                String i18n = null;
                String resBundleKey = "fedora.model."+model;
                if (resBundle.containsKey(resBundleKey)) {
                    i18n = resBundle.getString(model);
                } else {
                    i18n = model;
                }
                if (i > 0) builder.append(" | ");
                builder.append(i18n).append(":").append(dcConent.getTitle());
            }

            return builder.toString();
        }


        public abstract Type getType();

        public abstract boolean isMaster();

        public abstract boolean isInvalidOption();

        public boolean isOffPDFCheck() {
            boolean turnOff = KConfiguration.getInstance().getConfiguration().getBoolean("turnOffPdfCheck");
            return turnOff;
        }
        
    }


    public class SelectionRadioItem extends RadioItem {

        public SelectionRadioItem(String id, boolean checked) {
            super(id, checked);
            
        }

        @Override
        public Type getType() {
            return Type.selection;
        }

        @Override
        public boolean isMaster() {
            return false;
        }

        @Override
        public boolean isInvalidOption() {
            boolean turnOff = KConfiguration.getInstance().getConfiguration().getBoolean("turnOffPdfCheck");
            if (turnOff) return false; // valid
            int maxPage = KConfiguration.getInstance().getConfiguration().getInt("generatePdfMaxRange");
            return getPids().size() > maxPage;
        }   
        
    }

    public class MasterRadioItem extends RadioItem {

        public MasterRadioItem(String id, boolean checked) {
            super(id, checked);
        }

        @Override
        public boolean isMaster() {
            return true;
        }

        @Override
        public Type getType() {
            return Type.master;
        }

        @Override
        public boolean isInvalidOption() {
            return false;
        }
    }


    static enum Type {
        selection,
        master;
    }
}

