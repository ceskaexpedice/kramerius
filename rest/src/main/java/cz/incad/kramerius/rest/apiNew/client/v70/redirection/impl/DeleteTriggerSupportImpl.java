/*
 * Copyright (C) 2025  Inovatika
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
package cz.incad.kramerius.rest.apiNew.client.v70.redirection.impl;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.AlreadyRegistedPidsException;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestItem;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.DeleteTriggerSupport;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.introspect.IntrospectLiveResponse;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Named;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static cz.incad.kramerius.rest.apiNew.client.v70.redirection.introspect.IntrospectLiveResponse.*;

public class DeleteTriggerSupportImpl implements DeleteTriggerSupport {

    public static final Logger LOGGER = Logger.getLogger(DeleteTriggerSupportImpl.class.getName());

    private ReharvestManager reharvestManager;
    private Instances instances;
    private SolrAccess solrAccess;
    private Provider<CloseableHttpClient> apacheClientProvider;


    public DeleteTriggerSupportImpl() {
    }

    @Inject
    public void setApacheClientProvider( @Named("forward-client") Provider<CloseableHttpClient> apacheClientProvider) {
        this.apacheClientProvider = apacheClientProvider;
    }


    @Inject
    public void setSolrAccess(@Named("new-index")SolrAccess solrAccess) {
        this.solrAccess = solrAccess;
    }

    @Inject
    public void setInstances(Instances instances) {
        this.instances = instances;
    }

    @Inject
    public void setReharvestManager(ReharvestManager reharvestManager) {
        this.reharvestManager = reharvestManager;
    }

    public Instances getInstances() {
        return instances;
    }

    public SolrAccess getSolrAccess() {
        return solrAccess;
    }

    public ReharvestManager getReharvestManager() {
        return reharvestManager;
    }

    public Provider<CloseableHttpClient> getApacheClientProvider() {
        return apacheClientProvider;
    }


    /**
     * Represents the relationship between `root.pid` from the CDK system and `rootPid` from Live DL.
     * <p>
     * This record holds two identifiers:
     * - `cdkRootPid` – the root PID obtained from the CDK system.
     * - `liveRootPid` – the root PID retrieved from Live DL.
     * <p>
     * If both identifiers are the same, it indicates that the object is a real root.
     */
    public record RootPidToRootPidRelation(String cdkRootPid, String liveRootPid) {

        public boolean isRealRoot() {
            return cdkRootPid.equals(liveRootPid);
        }
    }


    @Override
    public void executeConflictTrigger(String pid) {
        try {
            // all compositeIds
            List<String> compositeIds = compositeIds(this.solrAccess.getSolrDataByPid(pid));
            if (compositeIds.size() > 1) {
                // all roots from composite id
                List<String> cdkRootPids = compositeIds.stream().map(this::extractRootPidFromCompositeId).collect(Collectors.toList());
                // plan delete tree; delete previous open item if any
                cdkRootPids.forEach(this::planConfictDeleteRoot);

                // only for sorting
                List<RootPidToRootPidRelation> sortedList = new ArrayList<>();
                List<IntrospectLiveResponse> liveResponses = new ArrayList<>();

                cdkRootPids.forEach(cdkRootPid -> {
                    LinkedHashSet<RootPidToRootPidRelation> lset = new LinkedHashSet<>();
                    IntrospectLiveResponse liveResponse = buildResponse(cdkRootPid);
                    liveResponse.getIntrospectItems().forEach(item-> {
                        lset.add(new RootPidToRootPidRelation(cdkRootPid, item.rootPid()));
                    });
                    liveResponses.add(liveResponse);
                    sortedList.addAll(lset);
                });

                sortedList.sort(Comparator.comparing(RootPidToRootPidRelation::isRealRoot).reversed());

                for (RootPidToRootPidRelation rootPidToRootPidRelation : sortedList) {
                    // cdk root pid
                    Optional<IntrospectLiveResponse> found = liveResponses.stream()
                            .filter(response -> rootPidToRootPidRelation.cdkRootPid.equals(response.getPid()))
                            .findFirst();

                    if (found.isPresent()) {
                        IntrospectLiveResponse liveResult = found.get();
                        if (!liveResult.isConflictedResult()) {
                            IntrospectLiveResponseItem pivotItem = liveResult.getPivotItem();
                            ReharvestItem reharvest = new ReharvestItem(
                                    UUID.randomUUID().toString(),
                                    "New root |conflict",
                                    "waiting_for_approve",
                                    pivotItem.pid(),
                                    pivotItem.getFirtsPidPath()
                            );
                            reharvest.setRootPid(pivotItem.rootPid());
                            reharvest.setTypeOfReharvest( pivotItem.isTopLevelModel() ?  ReharvestItem.TypeOfReharvset.root : ReharvestItem.TypeOfReharvset.children);
                            try {
                                this.reharvestManager.register(reharvest);
                            } catch (AlreadyRegistedPidsException e) {
                                throw new RuntimeException(e);
                            }

                        } else {
                            throw new RuntimeException(String.format("Conflict live result %s",liveResult.toString()));
                        }
                    }
                }
            } else {
                LOGGER.log(Level.WARNING, "");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "No reharvest manager or pid ");
        }
    }

    IntrospectLiveResponse buildResponse(String pid) {
        return new Builder()
                .withHttpClient(this.apacheClientProvider.get())
                .withInstances(instances)
                .withPid(pid)
                .build();
    }

    private void planConfictDeleteRoot(String p1) {
        ReharvestItem openItem = reharvestManager.getOpenItemByPid(p1);
        if (openItem != null) {
            reharvestManager.deregister(openItem.getId());
        }
        try {
            ReharvestItem reharvestItem = new ReharvestItem(UUID.randomUUID().toString(),
                    "Delete root|conflict ",
                    "waiting_for_approve", p1, p1);
            reharvestItem.setRootPid(p1);
            reharvestItem.setTypeOfReharvest(ReharvestItem.TypeOfReharvset.delete_root);
            this.reharvestManager.register(reharvestItem);

        } catch (AlreadyRegistedPidsException e) {
            LOGGER.log(Level.SEVERE, String.format("Pid %s has been already registred", p1));
        }
    }



    private String extractRootPidFromCompositeId(String compositeId) {
        String[] split = compositeId.split("!");
        return (split.length > 0) ? split[0] : compositeId;
    }


    @Override
    public void executeDeleteTrigger(String pid) {

        try {
            Document doc = this.solrAccess.getSolrDataByPid(pid);
            List<String> compositeIds = compositeIds(doc);
            if (compositeIds.size() == 1) {
                String parentPid = onwParentPid(doc);

                IntrospectLiveResponse liveResponse = buildResponse(parentPid);
                if (!liveResponse.isConflictedResult()) {
                    IntrospectLiveResponseItem pivotItem = liveResponse.getPivotItem();
                    if (pivotItem != null) {
                        try {
                            ReharvestItem reharvest = new ReharvestItem(
                                    UUID.randomUUID().toString(),
                                    "Delete trigger|404 ",
                                    "open",
                                    pivotItem.pid(),
                                    pivotItem.getFirtsPidPath()
                            );
                            reharvest.setRootPid(pivotItem.rootPid());
                            reharvest.setLibraries(new ArrayList<>(liveResponse.getKeys("_cdk_")));
                            reharvest.setTypeOfReharvest(pivotItem.isTopLevelModel() ? ReharvestItem.TypeOfReharvset.root : ReharvestItem.TypeOfReharvset.children);
                            reharvestManager.register(reharvest);
                        } catch (AlreadyRegistedPidsException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        String onwModelPath = ownModelPath(doc);
                        List<String> models = Arrays.stream(onwModelPath.split("/")).collect(Collectors.toList());
                        int index = models.size() -2;
                        String previousModel = index >= 0 ? models.get(index) : null;
                        if (StringUtils.isAnyString(previousModel)) {

                            List<String> topLevelModels = Lists.transform(KConfiguration.getInstance().getConfiguration().getList("fedora.topLevelModels"), Functions.toStringFunction());
                            boolean topModel =  topLevelModels.contains(previousModel);

                            try {
                                ReharvestItem reharvest = new ReharvestItem(
                                        UUID.randomUUID().toString(),
                                        "Delete trigger|404 ",
                                        "open",
                                        parentPid,
                                        ownPidPath(doc)
                                );
                                reharvest.setRootPid(rootPid(doc));
                                reharvest.setTypeOfReharvest(topModel ? ReharvestItem.TypeOfReharvset.delete_root : ReharvestItem.TypeOfReharvset.delete_tree);
                                reharvestManager.register(reharvest);
                            } catch (AlreadyRegistedPidsException e) {
                                throw new RuntimeException(e);
                            }

                        } else {

                            try {
                                ReharvestItem reharvest = new ReharvestItem(
                                        UUID.randomUUID().toString(),
                                        "Delete trigger|404 ",
                                        "open",
                                        parentPid,
                                        ownPidPath(doc)
                                );
                                reharvest.setRootPid(rootPid(doc));
                                reharvest.setTypeOfReharvest(ReharvestItem.TypeOfReharvset.delete_root);
                                reharvestManager.register(reharvest);
                            } catch (AlreadyRegistedPidsException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            } else {

               // handle conflict
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    private  String onwParentPid(Document solrDataByPid) {
        Element ownParentPidElm = XMLUtils.findElement(solrDataByPid.getDocumentElement(),
                new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) {
                        if (element.getNodeName().equals("str")) {
                            String fieldName = element.getAttribute("name");
                            return fieldName.equals("own_parent.pid");
                        }
                        return false;
                    }
                });
        return ownParentPidElm != null ? ownParentPidElm.getTextContent() : null;
    }

    @NotNull
    private List<String> compositeIds(Document solrDataByPid) {
        List<String> compositeIds = XMLUtils.getElementsRecursive(solrDataByPid.getDocumentElement(),
                new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) {
                        if (element.getNodeName().equals("str")) {
                            String fieldName = element.getAttribute("name");
                            return fieldName.equals("compositeId");
                        }
                        return false;
                    }
                }).stream().map(Element::getTextContent).collect(Collectors.toList());
        return compositeIds;
    }


    @Nullable
    private  String ownModelPath(Document solrDataByPid) {
        Element ownModelPath = XMLUtils.findElement(solrDataByPid.getDocumentElement(),
                new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) {
                        if (element.getNodeName().equals("str")) {
                            String fieldName = element.getAttribute("name");
                            return fieldName.equals("own_model_path");
                        }
                        return false;
                    }
                });
        return ownModelPath != null ? ownModelPath.getTextContent() : null;
    }

    @Nullable
    private  String ownPidPath(Document solrDataByPid) {
        Element ownModelPath = XMLUtils.findElement(solrDataByPid.getDocumentElement(),
                new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) {
                        if (element.getNodeName().equals("str")) {
                            String fieldName = element.getAttribute("name");
                            return fieldName.equals("own_pid_path");
                        }
                        return false;
                    }
                });
        return ownModelPath != null ? ownModelPath.getTextContent() : null;
    }

    @Nullable
    private  String rootPid(Document solrDataByPid) {
        Element rootPid = XMLUtils.findElement(solrDataByPid.getDocumentElement(),
                new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) {
                        if (element.getNodeName().equals("str")) {
                            String fieldName = element.getAttribute("name");
                            return fieldName.equals("root.pid");
                        }
                        return false;
                    }
                });
        return rootPid != null ? rootPid.getTextContent() : null;
    }
}
