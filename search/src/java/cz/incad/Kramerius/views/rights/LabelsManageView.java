package cz.incad.Kramerius.views.rights;

import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.labels.Label;
import cz.incad.kramerius.security.labels.LabelsManager;
import cz.incad.kramerius.security.labels.LabelsManagerException;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LabelsManageView extends AbstractRightsView {

    public static Logger LOGGER = Logger.getLogger(LabelsManageView.class.getName());

    @Inject
    LabelsManager labelsManager;

    @Inject
    RightsManager rightsManager;

    @Inject
    Provider<HttpServletRequest> requestProvider;


    @Inject
    @Named("new-index")
    SolrAccess solrAccess;


    public List<LabelListItemView> getLabels() {
        try {

            Document request = this.solrAccess.requestWithSelectReturningXml("facet.field=licenses&fl=licenses&q=*%3A*&rows=0&facet=on");

            Element dnntLabelsFromSolr = XMLUtils.findElement(request.getDocumentElement(), new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String name = element.getAttribute("name");
                    return name != null && name.equals("licenses");
                }
            });

            List<String> labelsUsedInSolr = XMLUtils.getElements(dnntLabelsFromSolr).stream().map(element -> {
                return element.getAttribute("name");
            }).collect(Collectors.toList());


            return this.labelsManager.getLabels().stream().map(label-> {
                List<Map<String, String>> objects = rightsManager.findObjectUsingLabel(label.getId());
                return new LabelListItemView(objects,  labelsUsedInSolr, label);
            }).collect(Collectors.toList());
        } catch (LabelsManagerException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return new ArrayList<>();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return new ArrayList<>();
        }
    }


    public class LabelListItemView {

        private Label label;
        private List<Map<String, String>> objects = null;
        private List<String> solrLabels;

        public LabelListItemView(List<Map<String, String>> rightObjects, List<String> solrLabels, Label label) {
            this.objects = rightObjects;
            this.label = label;
            this.solrLabels = solrLabels;
        }


        public List<String> getUsedPids() {
            return this.objects.stream().map(item-> {return item.get("pid");}).collect(Collectors.toList());
        }

        public boolean isUsedInRights() {
            return !getUsedPids().isEmpty();
        }

        public List<String> getSolrLabels() {
            return solrLabels;
        }


        public boolean isUsedInSolr() {
            Optional<String> any = getSolrLabels().stream().filter(label -> label.equals(getName())).findAny();
            return any.isPresent();
        }

        public int getId() {
            return this.label.getId();
        }

        public String getName() {
            return this.label.getName();
        }

        public String getDescription() {
            return this.label.getDescription();
        }

        public int getPriority() {
            return this.label.getPriority();
        }

        public String getGroup() {
            return this.label.getGroup();
        }

        public boolean isLocal() {
            return this.label.getGroup() != null && this.label.getGroup().trim().equals(LabelsManager.LOCAL_GROUP_NAME);
        }
    }
}
