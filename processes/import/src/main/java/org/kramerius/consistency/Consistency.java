/*
 * Copyright (C) 2012 Pavel Stastny
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
package org.kramerius.consistency;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.TreeNodeProcessStackAware;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.pid.LexerException;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.RelsExtRelation;
import org.ceskaexpedice.akubra.utils.ProcessSubtreeException;
import org.ceskaexpedice.akubra.utils.RelsExtUtils;
import org.ceskaexpedice.akubra.utils.TreeNodeProcessor;

import javax.xml.transform.TransformerConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * Constitency check process
 * @author pavels
 */
public class Consistency {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Consistency.class.getName());

    /* TODO AK_NEW
    @Inject
    @Named("rawFedoraAccess")
    FedoraAccess fedoraAccess;

     */
    @Inject
    AkubraRepository akubraRepository;

    /**
     * Check consitency of fedora objects
     * @param rootPid Root pid
     * @param repair Flag determine if the process should delete broken references
     * @throws IOException IO error has been occured
     * @throws ProcessSubtreeException Processing tree error has been occured
     * @throws LexerException PID Parsing error has been occured
     */
    public List<NotConsistentRelation> checkConsitency(String rootPid, boolean repair) throws IOException, ProcessSubtreeException, LexerException {
        TreeProcess deep = new TreeProcess(akubraRepository);
        RelsExtUtils.processSubtree(rootPid, deep, akubraRepository);
        List<NotConsistentRelation> relations = deep.getRelations();
        if (repair) {
            LOGGER.fine("deleting inconsitencies");
            for (NotConsistentRelation nRelation : relations) {
                List<String> children = nRelation.getChildren();
                List<RelsExtRelation> relationsList = akubraRepository.relsExtGet(nRelation.rootPid).getRelations(null);
                for (RelsExtRelation t : relationsList) {

                    if (children.contains(t.getResource())) {
                        akubraRepository.relsExtRemoveRelation(nRelation.rootPid, t.getLocalName(), t.getNamespace(), t.getResource());
                        if (akubraRepository.relsExtRelationExists(nRelation.rootPid, t.getLocalName(), t.getNamespace())) {
                            throw new RuntimeException("cannot delete relation ");
                        }
                    }

                }
            }
        }
        return relations;
    }


    /**
     * Walks trough tree and finds non exist relations
     * @author pavels
     *
     */
    static class TreeProcess implements TreeNodeProcessor, TreeNodeProcessStackAware {

        private final AkubraRepository akubraRepository;
        private List<NotConsistentRelation> relations = new ArrayList<Consistency.NotConsistentRelation>();
        private Stack<String> pidsStack = null;

        public TreeProcess(AkubraRepository akubraRepository) {
            super();
            this.akubraRepository = akubraRepository;
        }

        @Override
        public void process(String pid, int level) throws ProcessSubtreeException {
            LOGGER.fine("exploring '" + pid + "'");
        }

        @Override
        public boolean breakProcessing(String pid, int level) {
            return false;
        }


        @Override
        public void changeProcessingStack(Stack<String> pidStack) {
            this.pidsStack = pidStack;
        }

        @Override
        public boolean skipBranch(String pid, int level) {
            if (akubraRepository.objectExists(pid)) {
                return false;
            } else {
                LOGGER.fine("deleting relation  to nonexisting pid " + pid);
                String peek = this.pidsStack.peek();
                this.relations.add(new NotConsistentRelation(peek, Arrays.asList(pid)));
                return true;
            }
        }

        /**
         * @return the relations
         */
        public List<NotConsistentRelation> getRelations() {
            return relations;
        }
    }

    /**
     * Non consistent relation class
     * @author pavels
     *
     */
    public static class NotConsistentRelation {

        private String rootPid;
        private List<String> children;

        public NotConsistentRelation(String rootPid, List<String> children) {
            super();
            this.rootPid = rootPid;
            this.children = children;
        }

        /**
         * @return the children
         */
        public List<String> getChildren() {
            return children;
        }

        /**
         * @return the rootPid
         */
        public String getRootPid() {
            return rootPid;
        }

    }

    /**
     * @param objectPidsPath
     * @return
     */
    static ObjectPidsPath withoutRepository(ObjectPidsPath objectPidsPath) {
        if (objectPidsPath.contains(SpecialObjects.REPOSITORY.getPid())) {
            return objectPidsPath.cutHead(1);
        } else return objectPidsPath;
    }

    /** guice module */
    public static class _Module extends AbstractModule {
        @Override
        protected void configure() {


//            bind(StatisticsAccessLog.class).annotatedWith(Names.named("database")).to(GenerateDeepZoomCacheModule.NoStatistics.class).in(Scopes.SINGLETON);
//            bind(StatisticsAccessLog.class).annotatedWith(Names.named("dnnt")).to(GenerateDeepZoomCacheModule.NoStatistics.class).in(Scopes.SINGLETON);


        }
    }

    /**
     * Main process method
     * @param pid Root pid
     * @param flag Control flag 
     * @throws IOException
     * @throws ProcessSubtreeException
     * @throws LexerException
     */
    @Process
    public static void process(String pid, Boolean flag) throws IOException, ProcessSubtreeException, LexerException {
        Injector injector = Guice.createInjector(new SolrModule(), new RepoModule(), new NullStatisticsModule());
        Consistency consistency = new Consistency();
        injector.injectMembers(consistency);
        List<NotConsistentRelation> inconsitencies = consistency.checkConsitency(pid, flag.booleanValue());

    }

    public static void main(String[] args) throws IOException, ProcessSubtreeException, LexerException, TransformerConfigurationException {
        if (args.length == 2) {
            process(args[0], Boolean.valueOf(args[1]));
        }
    }
}
