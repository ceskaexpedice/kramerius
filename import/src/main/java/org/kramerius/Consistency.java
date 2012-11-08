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
package org.kramerius;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import javax.ws.rs.Consumes;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.fedora.api.FedoraAPIM;
import org.fedora.api.RelationshipTuple;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Scopes;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.TreeNodeProcessStackAware;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.imaging.ImagingModuleForTest;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.processes.annotations.Process;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

/**
 * @author pavels
 */
public class Consistency {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Consistency.class.getName());
    
    @Inject
    FedoraAccess fedoraAccess;

    FedoraAPIM port;
    
    public void checkConsitency(String rootPid, boolean repair) throws IOException, ProcessSubtreeException, LexerException, TransformerConfigurationException {
        port = fedoraAccess.getAPIM();
        TreeProcess deep = new TreeProcess(this.fedoraAccess);
        this.fedoraAccess.processSubtree(rootPid, deep);
        List<NotConsistentRelation> relations = deep.getRelations();
        if (repair) {
            LOGGER.info("deleting inconsitencies");
            for (NotConsistentRelation nRelation : relations) {
                
                
                List<String> children = nRelation.getChildren();
                List<RelationshipTuple> existingWS = port.getRelationships(nRelation.getRootPid(), null);
                for (RelationshipTuple rTuple : existingWS) {
                    if (!rTuple.isIsLiteral()) {
                        PIDParser parser = new PIDParser(rTuple.getObject());
                        parser.disseminationURI();
                        if (children.contains(parser.getObjectPid())) {
                            boolean purgeRelationship = port.purgeRelationship(rTuple.getSubject(), rTuple.getPredicate(), rTuple.getObject(), rTuple.isIsLiteral(), rTuple.getDatatype());
                            if (!purgeRelationship) throw new RuntimeException("cannot delete relation ");
                            
                        }
                    }
                }
            }
        } else {
            if (!relations.isEmpty()) {
                LOGGER.severe("Found inconsitencies ");
            }
        }
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
            LOGGER.info("exploring '"+pid+"'");
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
                LOGGER.severe("current stack "+this.pidsStack.toString()+" and missing pid "+pid+ " and current level "+level);
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

    static class NotConsistentRelation {
        
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


    static class _Module extends AbstractModule {
        @Override
        protected void configure() {
            bind(KConfiguration.class).toInstance(KConfiguration.getInstance());
            bind(FedoraAccess.class).to(FedoraAccessImpl.class).in(Scopes.SINGLETON);
        }
    }

    @Process
    public static void process(String pid, Boolean flag) throws IOException, ProcessSubtreeException, LexerException, TransformerConfigurationException {
        Injector injector = Guice.createInjector(new _Module());
        Consistency consistency = new Consistency();
        injector.injectMembers(consistency);
        consistency.checkConsitency(pid, flag.booleanValue());
    }
    
    public static void main(String[] args) throws IOException, ProcessSubtreeException, LexerException, TransformerConfigurationException {
        if (args.length ==2) {
            process(args[0], new Boolean(args[1]));
        }
    }
}
