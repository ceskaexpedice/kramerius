package cz.incad.kramerius;

import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.core.repository.KnownDatastreams;
import org.ceskaexpedice.akubra.core.repository.RepositoryException;
import org.ceskaexpedice.akubra.utils.Dom4jUtils;
import org.ceskaexpedice.akubra.utils.ProcessingIndexUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class LicenseHelper {

    private static final Logger LOGGER = Logger.getLogger(LicenseHelper.class.getName());

    static String RELS_EXT_RELATION_LICENSE = "license";
    static String RELS_EXT_RELATION_CONTAINS_LICENSE = "containsLicense";
    static String[] RELS_EXT_RELATION_LICENSE_DEPRECATED = new String[]{
            "licenses",
            "licence", "licences",
            "dnnt-label", "dnnt-labels"
    };
    static String[] RELS_EXT_RELATION_CONTAINS_LICENSE_DEPRECATED = new String[]{
            "containsLicenses",
            "containsLicence", "containsLicences",
            "contains-license", "contains-licenses",
            "contains-licence", "contains-licenses",
            "contains-dnnt-label", "contains-dnnt-labels",
    };

    static String SOLR_FIELD_LICENSES = "licenses";
    static String SOLR_FIELD_CONTAINS_LICENSES = "contains_licenses";
    static String SOLR_FIELD_LICENSES_OF_ANCESTORS = "licenses_of_ancestors";

    static boolean removeRelsExtRelationAfterNormalization(String pid, String relationName, String[] wrongRelationNames, String value, AkubraRepository repository) {
        return repository.doWithWriteLock(pid, () -> {
            if (!repository.datastreamExists(pid, KnownDatastreams.RELS_EXT.toString())) {
                throw new RepositoryException("RDF record (datastream RELS-EXT) not found for " + pid);
            }
            InputStream inputStream = repository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT.toString());
            Document relsExt = Dom4jUtils.streamToDocument(inputStream, true);
            Element rootEl = (Element) Dom4jUtils.buildXpath("/rdf:RDF/rdf:Description").selectSingleNode(relsExt);
            boolean relsExtNeedsToBeUpdated = false;

            //normalize relations with deprecated/incorrect names, possibly including relation we want to remove
            relsExtNeedsToBeUpdated |= normalizeIncorrectRelationNotation(wrongRelationNames, relationName, rootEl, pid);

            //remove relation if found (even multiple times with same licence)
            List<Node> relationEls = Dom4jUtils.buildXpath("rel:" + relationName).selectNodes(rootEl);
            for (Node relationEl : relationEls) {
                String content = relationEl.getText();
                if (content.equals(value)) {
                    LOGGER.info(String.format("removing relation '%s' from RELS-EXT of %s", relationName, pid));
                    relationEl.detach();
                    relsExtNeedsToBeUpdated = true;
                }
            }

            //update RELS-EXT in repository if there was a change
            if (relsExtNeedsToBeUpdated) {
                //System.out.println(Dom4jUtils.docToPrettyString(relsExt));
                ByteArrayInputStream bis = new ByteArrayInputStream(relsExt.asXML().getBytes(Charset.forName("UTF-8")));
                repository.updateXMLDatastream(pid, KnownDatastreams.RELS_EXT.toString(), "text/xml", bis);
                LOGGER.info(String.format("RELS-EXT of %s has been updated", pid));
            }
            return relsExtNeedsToBeUpdated;
        });

    }

    /*
   Normalizuje vazby v nactenem rels-ext. Napr. nahradi vsechny relace dnnt-labels za license. Dalsi zpracovani (pridavani/odebirani) uz ma korektne zapsana data.
    */
    static boolean normalizeIncorrectRelationNotation(String[] wrongRelationNames, String correctRelationName, Element rootEl, String pid) {
        boolean updated = false;
        for (String wrongRelationName : wrongRelationNames) {
            List<Node> deprecatedRelationEls = Dom4jUtils.buildXpath("rel:" + wrongRelationName).selectNodes(rootEl);
            for (Node relationEl : deprecatedRelationEls) {
                String valueOfRelationBeingFixed = relationEl.getText();
                LOGGER.info(String.format("found incorrect notation (%s) in RELS-EXT of object %s and value '%s', fixing by replacing with %s", wrongRelationName, pid, valueOfRelationBeingFixed, correctRelationName));
                relationEl.detach(); //setName() pracuje spatne s jmenymi prostory - zpusobi duplikaci atributu xmlns
                Element newRelationEl = rootEl.addElement(correctRelationName, Dom4jUtils.getNamespaceUri("rel"));
                newRelationEl.addText(valueOfRelationBeingFixed);
                updated = true;
            }
        }
        return updated;
    }

    static boolean ownsLicenseByRelsExt(String pid, String license, AkubraRepository repository) throws IOException {
        return repository.doWithWriteLock(pid, () -> {
            if (!repository.datastreamExists(pid, KnownDatastreams.RELS_EXT.toString())) {
                throw new RepositoryException("RDF record (datastream RELS-EXT) not found for " + pid);
            }
            InputStream inputStream = repository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT.toString());
            Document relsExt = Dom4jUtils.streamToDocument(inputStream, true);
            Element rootEl = (Element) Dom4jUtils.buildXpath("/rdf:RDF/rdf:Description").selectSingleNode(relsExt);
            //look for rels-ext:license
            for (Node relationEl : Dom4jUtils.buildXpath("rel:" + RELS_EXT_RELATION_LICENSE).selectNodes(rootEl)) {
                String content = relationEl.getText();
                if (content.equals(license)) {
                    System.out.println("found rels-ext:license for " + pid);
                    return true;
                }
            }
            //look for rels-ext:license (deprecated notation)
            for (String relation : RELS_EXT_RELATION_CONTAINS_LICENSE_DEPRECATED) {
                for (Node relationEl : Dom4jUtils.buildXpath("rel:" + relation).selectNodes(rootEl)) {
                    LOGGER.warning(String.format("found depracated notation '%s' for %s, should be replaced with '%s'", relation, pid, RELS_EXT_RELATION_LICENSE));
                    String content = relationEl.getText();
                    if (content.equals(license)) {
                        System.out.println("found rels-ext:license for " + pid);
                        return true;
                    }
                }
            }
            return false;
        });
    }

    static List<String> getLicensesByRelsExt(String pid, AkubraRepository repository)  {
        return repository.doWithWriteLock(pid, () -> {
            if (!repository.datastreamExists(pid, KnownDatastreams.RELS_EXT.toString())) {
                throw new RepositoryException("RDF record (datastream RELS-EXT) not found for " + pid);
            }
            InputStream inputStream = repository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT.toString());
            Document relsExt = Dom4jUtils.streamToDocument(inputStream, true);
            Element rootEl = (Element) Dom4jUtils.buildXpath("/rdf:RDF/rdf:Description").selectSingleNode(relsExt);
            List<String> result = new ArrayList<>();
            //look for rels-ext:license
            for (Node relationEl : Dom4jUtils.buildXpath("rel:" + RELS_EXT_RELATION_LICENSE).selectNodes(rootEl)) {
                String license = relationEl.getText();
                result.add(license);
                LOGGER.info("found rels-ext:license " + license);
            }
            //look for rels-ext:license (deprecated notation)
            for (String relation : RELS_EXT_RELATION_CONTAINS_LICENSE_DEPRECATED) {
                for (Node relationEl : Dom4jUtils.buildXpath("rel:" + relation).selectNodes(rootEl)) {
                    LOGGER.warning(String.format("found depracated notation '%s' for %s, should be replaced with '%s'", relation, pid, RELS_EXT_RELATION_LICENSE));
                    String license = relationEl.getText();
                    result.add(license);
                    LOGGER.info("found rels-ext:license " + license);
                }
            }
            return result;
        });
    }

    static boolean containsLicenseByRelsExt(String pid, String license, AkubraRepository repository) throws RepositoryException, IOException {
        return repository.doWithWriteLock(pid, () -> {
            if (!repository.datastreamExists(pid, KnownDatastreams.RELS_EXT.toString())) {
                throw new RepositoryException("RDF record (datastream RELS-EXT) not found for " + pid);
            }
            InputStream inputStream = repository.getDatastreamContent(pid, KnownDatastreams.RELS_EXT.toString());
            Document relsExt = Dom4jUtils.streamToDocument(inputStream, true);
            Element rootEl = (Element) Dom4jUtils.buildXpath("/rdf:RDF/rdf:Description").selectSingleNode(relsExt);

            //look for rels-ext:containsLicense
            for (Node relationEl : Dom4jUtils.buildXpath("rel:" + RELS_EXT_RELATION_CONTAINS_LICENSE).selectNodes(rootEl)) {
                String content = relationEl.getText();
                if (content.equals(license)) {
                    System.out.println("found rels-ext:containsLicense for " + pid);
                    return true;
                }
            }
            //look for rels-ext:containsLicense (deprecated notation)
            for (String relation : RELS_EXT_RELATION_CONTAINS_LICENSE_DEPRECATED) {
                for (Node relationEl : Dom4jUtils.buildXpath("rel:" + relation).selectNodes(rootEl)) {
                    LOGGER.warning(String.format("found depracated notation '%s' for %s, should be replaced with '%s'", relation, pid, RELS_EXT_RELATION_CONTAINS_LICENSE));
                    String content = relationEl.getText();
                    if (content.equals(license)) {
                        System.out.println("found rels-ext:containsLicense for " + pid);
                        return true;
                    }
                }
            }
            return false;
        });
    }

    /**
     * Returns list of pids of own ancestors of an object (@param pid), that don't have another source of license but this object (@param pid)
     * Object is never source of license for itself. Meaning that if it has rels-ext:license, but no rels-ext:containsLicense, it is considered not having source of license.
     */
    static List<String> getPidsOfOwnAncestorsWithoutAnotherSourceOfLicense(String pid, AkubraRepository repository, String license) throws IOException {
        List<String> result = new ArrayList<>();
        String pidOfChild = pid;
        String pidOfParent;
        while ((pidOfParent = ProcessingIndexUtils.getPidsOfParents(pidOfChild, repository).getLeft()) != null) {
            String pidToBeIgnored = pidOfChild.equals(pid) ? null : pidOfChild; //only grandparent of original pid can be ignored, because it has been already anylized in this loop, but not the original pid
            boolean hasAnotherSourceOfLicense = hasAnotherSourceOfLicense(pidOfParent, pid, pidToBeIgnored, license, repository);
            boolean ownsLicense = ownsLicenseByRelsExt(pidOfParent, license, repository);
            if (!hasAnotherSourceOfLicense) { //add this to the list
                result.add(pidOfParent);
            }
            if (hasAnotherSourceOfLicense) { //this has source for itself and thus for it's ancestors
                break;
            }
            if (ownsLicense) { //is itself source for it's ancestors (but not necessarily for itself, meaning it doesn't necessarily have rels-ext:containsLicense)
                break;
            }
            pidOfChild = pidOfParent;
        }
        return result;
    }

    /**
     * Searches object's tree for object, that owns the license (rels-ext:licenses). The tree is not searched completely, only paths labeled with rels-ext:containsLicense are traversed.
     * Two objects are ignored in different way.
     *
     * @param pid
     * @param pidOfObjectNotCountedAsSource this object's subtree WILL be searched. But if it itself owns the license, that won't be reason for this method to return true.
     *                                      This is because we are looking for ANOTHER source of license, not this object. But the source can be even somewhere in this object's subtree.
     * @param pidOfChildToBeIgnored         this object will be completely ignored, i.e. it's ownership of the license won't be checked and it's subtree won't be searched. Because it has been analyzed already.
     */
    static boolean hasAnotherSourceOfLicense(String pid, String pidOfObjectNotCountedAsSource, String pidOfChildToBeIgnored, String license, AkubraRepository repository) throws IOException {
        List<String> pidsOfOwnChildren = ProcessingIndexUtils.getPidsOfChildren(pid, repository).getLeft();
        for (String pidOfChild : pidsOfOwnChildren) {
            if (!pidOfChild.equals(pidOfChildToBeIgnored)) { //this one will be completly ignored, because it has already been analyzed
                if (!pidOfChild.equals(pidOfObjectNotCountedAsSource) && LicenseHelper.ownsLicenseByRelsExt(pidOfChild, license, repository)) { // child (and not the one that's not counted) owns the license, source found
                    return true;
                }
                if (LicenseHelper.containsLicenseByRelsExt(pidOfChild, license, repository)) { //child has descendant, that owns the license
                    if (hasAnotherSourceOfLicense(pidOfChild, pidOfObjectNotCountedAsSource, null, license, repository)) { // found child's descendant (and not the one that's not counted) that has a source
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
