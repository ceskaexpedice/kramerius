package cz.incad.Kramerius.views.rights;

import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.licenses.License;
import cz.incad.kramerius.security.licenses.LicensesManager;
import cz.incad.kramerius.security.licenses.LicensesManagerException;
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
    LicensesManager licensesManager;

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


            return this.licensesManager.getLabels().stream().map(label-> {
                List<Map<String, String>> objects = rightsManager.findObjectUsingLabel(label.getId());
                return new LabelListItemView(objects,  labelsUsedInSolr, label);
            }).collect(Collectors.toList());
        } catch (LicensesManagerException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return new ArrayList<>();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return new ArrayList<>();
        }
    }


    public class LabelListItemView {

        private License license;
        private List<Map<String, String>> objects = null;
        private List<String> solrLabels;

        public LabelListItemView(List<Map<String, String>> rightObjects, List<String> solrLabels, License license) {
            this.objects = rightObjects;
            this.license = license;
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
            return this.license.getId();
        }

        public String getName() {
            return this.license.getName();
        }

        public String getDescription() {
            return this.license.getDescription();
        }

        public int getPriority() {
            return this.license.getPriority();
        }

        public String getGroup() {
            return this.license.getGroup();
        }

        public boolean isLocal() {
            return this.license.getGroup() != null && this.license.getGroup().trim().equals(LicensesManager.LOCAL_GROUP_NAME);
        }
    }
}
