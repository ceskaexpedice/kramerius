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
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.Initializable;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.XMLUtils;

public class AbstractPrintViewObject extends AbstractViewObject implements Initializable {

    protected List<RadioItem> items = new ArrayList<RadioItem>();

    @Named("securedFedoraAccess")
    @Inject
    protected FedoraAccess fedoraAccess;

    
    @Inject
    protected TextsService textsService;
    
    @Inject
    protected Provider<Locale> localesProvider;
    
    @Inject
    protected ResourceBundleService resourceBundleService;
    
    protected String header;
    protected String desc;

    
    @Override
    public void init() {
        try {
            RadioItem selection = new RadioItem(Type.selection, "selection", true);
            this.items.add(selection);
            List params = getPidsParams();
            for (int i = 0; i < params.size(); i++) {
                Object pid = params.get(i);
                if (this.fedoraAccess.isImageFULLAvailable(pid.toString())) {
                    selection.addPid(pid.toString());
                } else {
                    String id = pid.toString().replace(":", "_");
                    RadioItem radioItem = new RadioItem(Type.master,i+"_"+id, false);
                    radioItem.addPid(pid.toString());
                    this.items.add(radioItem);
                }
            }

            
            String xml = this.textsService.getText("first_page_xml", this.localesProvider.get());
            Document doc = XMLUtils.parseDocument(new ByteArrayInputStream(xml.getBytes()), false);
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



    public class RadioItem {
        
        private Type type;
        private List<String> pids;
        private String id;
        private boolean checked = false;
        
        private RadioItem(Type type, String id, boolean checked) {
            super();
            this.type = type;
            this.pids = new ArrayList<String>();
            this.id = id;
            this.checked = checked;
        }
        
        public boolean isChecked() {
            return this.checked;
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
        
        public boolean isMaster() {
            return this.type == Type.master;
        }
        
        public String getId() {
            return id;
        }
        
        public List<String> getPids() {
            return new ArrayList<String>(this.pids);
        }
        
        public String getName() throws IOException {
            String first = this.pids.get(0);
            Document dcf = AbstractPrintViewObject.this.fedoraAccess.getDC(first);
            String firstTitle = DCUtils.titleFromDC(dcf);

            if (type == Type.selection) {
                if (this.pids.size() > 1) {
                    String last = this.pids.get(this.pids.size()-1);
                    Document dcl = AbstractPrintViewObject.this.fedoraAccess.getDC(last);
                    String lastTitle = DCUtils.titleFromDC(dcl);
                    return firstTitle +" - "+lastTitle;
                } else {
                    return firstTitle;
                }
            } else {
                return firstTitle;
            }
        }
        
        public Type getType() {
            return type;
        }
    }
    
    static enum Type {
        selection,
        master;
    }


}

