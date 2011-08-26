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
package cz.incad.kramerius.document.impl;

import static cz.incad.kramerius.utils.BiblioModsUtils.getPageNumber;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.document.DocumentService;
import cz.incad.kramerius.document.model.AbstractPage;
import cz.incad.kramerius.document.model.AbstractRenderedDocument;
import cz.incad.kramerius.document.model.ImagePage;
import cz.incad.kramerius.document.model.OutlineItem;
import cz.incad.kramerius.document.model.RenderedDocument;
import cz.incad.kramerius.document.model.TextPage;
import cz.incad.kramerius.impl.AbstractTreeNodeProcessorAdapter;
import cz.incad.kramerius.pdf.utils.TitlesUtils;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;

public class DocumentServiceImpl implements DocumentService {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DocumentServiceImpl.class.getName());
    
    private FedoraAccess fedoraAccess;
    private Provider<Locale> localeProvider;
    private ResourceBundleService resourceBundleService;
    private SolrAccess solrAccess;
    
    @Inject
    public DocumentServiceImpl(@Named("securedFedoraAccess") FedoraAccess fedoraAccess, SolrAccess solrAccess, KConfiguration configuration, Provider<Locale> localeProvider, TextsService textsService, ResourceBundleService resourceBundleService) {
        super();
        this.fedoraAccess = fedoraAccess;
        this.localeProvider = localeProvider;
        this.resourceBundleService = resourceBundleService;
        this.solrAccess = solrAccess;
        try {
            this.init();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    
    
    
    private void init() throws IOException {
        
    }

    

    public void buildRenderingDocumentAsFlat(final AbstractRenderedDocument renderedDocument, final String pidFrom,  final int howMany ) throws IOException, ProcessSubtreeException {
        if (pidFrom != null && fedoraAccess.isImageFULLAvailable(pidFrom)) {
            
            ObjectPidsPath[] path = solrAccess.getPath(pidFrom);
            String[] pathFromLeafToRoot = path[0].getPathFromLeafToRoot();
            String parent = pathFromLeafToRoot[pathFromLeafToRoot.length -2];
            
            fedoraAccess.processSubtree(parent, new TreeNodeProcessor() {
                private int index = 0;
                private boolean acceptingState = false;
                
                @Override
                public void process(String pid, int level) throws ProcessSubtreeException {
                    try{
                        if (fedoraAccess.isImageFULLAvailable(pid)) {
                            if (pid.equals(pidFrom) || (pidFrom == null)) {
                                acceptingState = true;
                            }
                            if (acceptingState) {
                                if (index < howMany) {
                                    renderedDocument.addPage(createPage(renderedDocument, pid));
                                }
                                index += 1;
                                if (index>=howMany) {
                                    acceptingState = false;
                                }
                            }
                        }
                    } catch (LexerException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
                
                @Override
                public boolean breakProcessing(String pid, int level) {
                    return index >= howMany;
                }
            });
            
            
        } else {

              fedoraAccess.processSubtree(pidFrom, new TreeNodeProcessor() {

                private int index = 0;

                @Override
                public void process(String pid, int level) throws ProcessSubtreeException {
                    try{
                        if (fedoraAccess.isImageFULLAvailable(pid)) {
                            if (index < howMany) {
                                renderedDocument.addPage(createPage(renderedDocument, pid));
                            }
                            index += 1;
                        }
                    } catch (LexerException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }

                @Override
                public boolean breakProcessing(String pid, int level) {
                    return index >= howMany;
                }
                  
              });
        }
    }

    
    
    public void buildRenderingDocumentAsTree(/*org.w3c.dom.Document relsExt,*/ final AbstractRenderedDocument renderedDocument , final String pid) throws IOException, ProcessSubtreeException {
        fedoraAccess.processSubtree(pid, new AbstractTreeNodeProcessorAdapter() {
            
            private int previousLevel = -1;
            private OutlineItem currOutline = null;
            @Override
            public void processUuid(String pageUuid, int level) {
                
                try {
                    AbstractPage page = createPage(renderedDocument, pageUuid);
                    renderedDocument.addPage(page);
                    if (previousLevel == -1) {
                        // first
                        this.currOutline = createOutlineItem(renderedDocument.getOutlineItemRoot(), page.getOutlineDestination(), page.getOutlineTitle(), level);
                        StringBuffer buffer = new StringBuffer();
                        this.currOutline.debugInformations(buffer, 0);
                    } else if (previousLevel == level) {
                        this.currOutline = this.currOutline.getParent();
                        this.currOutline = createOutlineItem(this.currOutline, page.getOutlineDestination(), page.getOutlineTitle(), level);

                        StringBuffer buffer = new StringBuffer();
                        this.currOutline.debugInformations(buffer, 0);

                    } else if (previousLevel < level) {
                        // dolu
                        this.currOutline = createOutlineItem(this.currOutline, page.getOutlineDestination(), page.getOutlineTitle(), level);

                        StringBuffer buffer = new StringBuffer();
                        this.currOutline.debugInformations(buffer, 0);

                    } else if (previousLevel > level) {
                        // nahoru // za poslednim smerem nahoru
                        //this.currOutline = this.currOutline.getParent();
                        int diff = previousLevel - level;
                        for (int i = 0; i < diff; i++) {
                            this.currOutline = this.currOutline.getParent();
                        }
                            
                        
                        StringBuffer buffer = new StringBuffer();
                        this.currOutline.debugInformations(buffer, 0);
                        
                        this.currOutline = this.currOutline.getParent();
                        this.currOutline = createOutlineItem(this.currOutline, page.getOutlineDestination(), page.getOutlineTitle(), level);
                        
                    }

                    previousLevel = level;
                } catch (DOMException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    throw new RuntimeException(e);
                } catch (LexerException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    throw new RuntimeException(e);
                }
                
            }
            

            private OutlineItem createOutlineItem(OutlineItem parent, String objectId, String biblioModsTitle, int level) {
                OutlineItem item = new OutlineItem();
                item.setDestination(objectId);

                
                item.setTitle(biblioModsTitle);
                
                parent.addChild(item);
                item.setParent(parent);
                item.setLevel(level);
                return item;
            }

        });
    }

    
    protected AbstractPage createPage( final AbstractRenderedDocument renderedDocument,
            String pid)
            throws LexerException, IOException {
//      
        org.w3c.dom.Document biblioMods = fedoraAccess.getBiblioMods(pid);
        org.w3c.dom.Document dc = fedoraAccess.getDC(pid);
        String modelName = fedoraAccess.getKrameriusModelName(pid);
        
        AbstractPage page = null;
        
        if (fedoraAccess.isImageFULLAvailable(pid)) {
            
            page = new ImagePage(modelName, pid);
            page.setOutlineDestination(pid);
            
            page.setBiblioMods(biblioMods);
            page.setDc(dc);
            
            
            String pageNumber = getPageNumber(biblioMods);
            if (pageNumber.trim().equals("")) {
                throw new IllegalStateException(pid);
            }
            page.setPageNumber(pageNumber);
            //renderedDocument.addPage(page);
            Element part = XMLUtils.findElement(biblioMods.getDocumentElement(), "part", FedoraNamespaces.BIBILO_MODS_URI);
            String attribute = part.getAttribute("type");
            if (attribute != null) {
                ResourceBundle resourceBundle = resourceBundleService.getResourceBundle("base", localeProvider.get());
                String key = "pdf."+attribute;
                if (resourceBundle.containsKey(key)) {
                    page.setOutlineTitle(page.getPageNumber()+" "+resourceBundle.getString(key));
                } else {
                    page.setOutlineTitle(page.getPageNumber());
                    //throw new RuntimeException("");
                }
            }
            if ((renderedDocument.getUuidTitlePage() == null) && ("TitlePage".equals(attribute))) {
                renderedDocument.setUuidTitlePage(pid);
            }

            if ((renderedDocument.getUuidFrontCover() == null) && ("FrontCover".equals(attribute))) {
                renderedDocument.setUuidFrontCover(pid);
            }

            if ((renderedDocument.getUuidBackCover() == null) && ("BackCover".equals(attribute))) {
                renderedDocument.setUuidBackCover(pid);
            }

            if (renderedDocument.getFirstPage() == null)  {
                renderedDocument.setFirstPage(pid);
            }
            

        } else {
            // metadata
            page = new TextPage(modelName, pid);
            page.setOutlineDestination(pid);
//          String title = DCUtils.titleFromDC(dc);
//          if ((title == null) || title.equals("")) {
//              title = BiblioModsUtils.titleFromBiblioMods(biblioMods);
//              title = BiblioModsUtils.getTitle(biblioMods, fedoraAccess.getKrameriusModelName(objectId));
//          }
            //if (title.trim().equals("")) throw new IllegalArgumentException(objectId+" has no title ");
            
            page.setBiblioMods(biblioMods);
            page.setDc(dc);
            
            page.setOutlineTitle(TitlesUtils.title(pid, solrAccess, fedoraAccess));
        }
        return page;
    }



    
    

    @Override
    public AbstractRenderedDocument buildDocumentFromSelection(String[] selection) throws IOException, ProcessSubtreeException {
        

        try {
            final AbstractRenderedDocument renderedDocument = new RenderedDocument("selection", selection[0]);
            for (String pid : selection) {
                renderedDocument.addPage(createPage(renderedDocument, pid));
            }
            
            /*
            renderedDocument.setDocumentTitle(TitlesUtils.title(leaf, this.solrAccess, this.fedoraAccess));
            renderedDocument.setUuidTitlePage(path.getLeaf());
            renderedDocument.setUuidMainTitle(path.getRoot());
            */
            
            return renderedDocument;
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }




    @Override
    public AbstractRenderedDocument buildDocumentAsFlat(ObjectPidsPath path, String pidFrom, int howMany) throws IOException, ProcessSubtreeException {
        String leaf = path.getLeaf();
        final AbstractRenderedDocument renderedDocument = new RenderedDocument(fedoraAccess.getKrameriusModelName(leaf), pidFrom);

        renderedDocument.setDocumentTitle(TitlesUtils.title(leaf, this.solrAccess, this.fedoraAccess));
        renderedDocument.setUuidTitlePage(path.getLeaf());
        renderedDocument.setUuidMainTitle(path.getRoot());
        
        buildRenderingDocumentAsFlat(renderedDocument, pidFrom, howMany);
        return renderedDocument;
    }




    @Override
    public AbstractRenderedDocument buildDocumentAsTree(ObjectPidsPath path, String pidFrom) throws IOException, ProcessSubtreeException {
        String leaf = path.getLeaf();
        String modelName = fedoraAccess.getKrameriusModelName(leaf);

        final AbstractRenderedDocument renderedDocument = new RenderedDocument(modelName, pidFrom);

        renderedDocument.setDocumentTitle(TitlesUtils.title(leaf, this.solrAccess, this.fedoraAccess));
        renderedDocument.setUuidTitlePage(path.getLeaf());
        renderedDocument.setUuidMainTitle(path.getRoot());
        
        
        TextPage dpage = new TextPage(modelName, path.getRoot());
        dpage.setOutlineDestination("desc");
        dpage.setOutlineTitle("Popis");
        renderedDocument.addPage(dpage);
        OutlineItem item = new OutlineItem();
        item.setLevel(1); item.setParent(renderedDocument.getOutlineItemRoot()); 
        item.setTitle("Popis"); item.setDestination("desc");
        renderedDocument.getOutlineItemRoot().addChild(item);

        buildRenderingDocumentAsTree(renderedDocument, pidFrom);

        return renderedDocument;
    }



    

    
}
