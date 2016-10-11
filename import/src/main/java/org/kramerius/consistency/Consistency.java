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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import javax.xml.transform.TransformerConfigurationException;

import org.fedora.api.FedoraAPIM;
import org.fedora.api.RelationshipTuple;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Scopes;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.TreeNodeProcessStackAware;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.statistics.StatisticsAccessLogSupport;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

/**
 * Constitency check process
 * @author pavels
 */
public class Consistency {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Consistency.class.getName());
    
    @Inject
    FedoraAccess fedoraAccess;

    FedoraAPIM port;

   /**
    * Check consitency of fedora objects
    * @param rootPid Root pid
    * @param repair Flag determine if the process should delete broken references
    * @throws IOException IO error has been occured
    * @throws ProcessSubtreeException Processing tree error has been occured
    * @throws LexerException PID Parsing error has been occured
    */
    public List<NotConsistentRelation> checkConsitency(String rootPid, boolean repair) throws IOException, ProcessSubtreeException, LexerException {
        TreeProcess deep = new TreeProcess(this.fedoraAccess);
        this.fedoraAccess.processSubtree(rootPid, deep);
        List<NotConsistentRelation> relations = deep.getRelations();
        if (repair) {
            port = fedoraAccess.getAPIM();
            LOGGER.fine("deleting inconsitencies");
            for (NotConsistentRelation nRelation : relations) {
                List<String> children = nRelation.getChildren();
                List<RelationshipTuple> existingWS = port.getRelationships(nRelation.getRootPid(), null);
                for (RelationshipTuple rTuple : existingWS) {
                    if (!rTuple.isIsLiteral()) {
                        PIDParser parser = new PIDParser(rTuple.getObject());
                        parser.disseminationURI();
                        if (children.contains(parser.getObjectPid())) {
                            LOGGER.fine("delete relationship "+rTuple.getSubject()+" "+rTuple.getPredicate()+" "+rTuple.getObject());
                            boolean purgeRelationship = port.purgeRelationship(rTuple.getSubject(), rTuple.getPredicate(), rTuple.getObject(), rTuple.isIsLiteral(), rTuple.getDatatype());
                            if (!purgeRelationship) throw new RuntimeException("cannot delete relation ");
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
    static class TreeProcess implements TreeNodeProcessor, TreeNodeProcessStackAware{
        
        private final FedoraAccess fa;
        private List<NotConsistentRelation> relations = new ArrayList<Consistency.NotConsistentRelation>();
        private Stack<String> pidsStack = null;
        
        public TreeProcess(FedoraAccess fa) {
            super();
            this.fa = fa;
        }

        @Override
        public void process(String pid, int level) throws ProcessSubtreeException {
            LOGGER.fine("exploring '"+pid+"'");
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
            try {
                this.fa.getRelsExt(pid);
                return false;
            } catch (IOException e) {
                //e.printStackTrace();
                LOGGER.fine("deleting relation  to nonexisting pid "+pid);
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
            bind(KConfiguration.class).toInstance(KConfiguration.getInstance());
            bind(FedoraAccess.class).to(FedoraAccessImpl.class).in(Scopes.SINGLETON);
            bind(StatisticsAccessLog.class).to(NoStatistics.class).in(Scopes.SINGLETON);
        }
    }
    
    public static class NoStatistics implements StatisticsAccessLog {

        @Override
        public StatisticReport[] getAllReports() {
            return new StatisticReport[0];
        }

        @Override
        public StatisticReport getReportById(String reportId) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void reportAccess(String pid, String streamName) throws IOException {
        }

        @Override
        public boolean isReportingAccess(String pid, String streamName) {
            return true;
        }

        @Override
        public void processAccessLog(ReportedAction reportedAction, StatisticsAccessLogSupport sup) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void reportAccess(String pid, String streamName, String actionName) throws IOException {
            // TODO Auto-generated method stub
            
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
        Injector injector = Guice.createInjector(new _Module());
        Consistency consistency = new Consistency();
        injector.injectMembers(consistency);
        List<NotConsistentRelation> inconsitencies = consistency.checkConsitency(pid, flag.booleanValue());
        
    }
    
    public static void main(String[] args) throws IOException, ProcessSubtreeException, LexerException, TransformerConfigurationException {
        if (args.length ==2) {
            process(args[0], new Boolean(args[1]));
        }
    }
}
