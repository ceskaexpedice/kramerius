/*
 * Copyright (C) 2016 Pavel Stastny
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

package cz.incad.feedrepo.impl.jackrabbit;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.LoginException;
import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeTypeDefinition;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;
import javax.jcr.version.VersionException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.JcrUtils;
import org.xml.sax.SAXException;

import cz.incad.feedrepo.RepoAbstraction;
import cz.incad.feedrepo.RepoAbstractionException;
import cz.incad.feedrepo.RepositoryObjectAbstraction;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.repo.impl.JackRabbitUtils;
import cz.incad.kramerius.utils.FedoraUtils;

/**
 * @author pavels
 */
public class JackRabbitRepo implements RepoAbstraction {

    public static final long TIMEOUT = 20000;

    public static final Logger LOGGER = Logger.getLogger(JackRabbitRepo.class.getName());

//    static String JACKRABBIT_FOLDER = System.getProperty("jackrabbit.folder",
//            (System.getProperty("user.home") + File.separator + "jck_repo"));

//    public static final String USER = "admin";
//    public static final String PASSWORD = "admin";

    private Repository repo;
    private JackRabbitRepoListener listener;
    private Session currentSession;
    private Object lock = new Object();

    private AtomicInteger inserted = new AtomicInteger(0);
    private AtomicInteger processed = new AtomicInteger(0);

    /*
     * (non-Javadoc)
     * 
     * @see cz.incad.fcrepo.RepoAbstraction#open()
     */
    @Override
    public void open() throws RepoAbstractionException {
        try {
            this.currentSession = this.repo.login(new SimpleCredentials(JackRabbitUtils.getUser(), JackRabbitUtils.getPassword().toCharArray()));
            this.namespaces();
            this.listener = new JackRabbitRepoListener(this.currentSession);
            EventListener listener = new EventListener() {
                @Override
                public void onEvent(EventIterator evt) {
                    try {
                        // synchronization lock
                        synchronized (JackRabbitRepo.this.lock) {
                            while (evt.hasNext()) {
                                Event nextEvent = evt.nextEvent();
                                int evtType = nextEvent.getType();
                                String path = nextEvent.getPath();
                                switch (evtType) {
                                case Event.NODE_ADDED:
                                    if (path.contains(FedoraUtils.RELS_EXT_STREAM)) {
                                        processed.incrementAndGet();
                                        try {
                                            JackRabbitRepo.this.listener.onRELSEXTAdded(path);
                                        } catch (PathNotFoundException e) {
                                            LOGGER.log(Level.SEVERE, e.getMessage());
                                        } catch (RepositoryException e) {
                                            LOGGER.log(Level.SEVERE, e.getMessage());
                                        } catch (ParserConfigurationException e) {
                                            LOGGER.log(Level.SEVERE, e.getMessage());
                                        } catch (SAXException e) {
                                            LOGGER.log(Level.SEVERE, e.getMessage());
                                        } catch (IOException e) {
                                            LOGGER.log(Level.SEVERE, e.getMessage());
                                        } catch (NoSuchElementException e) {
                                            LOGGER.log(Level.SEVERE, e.getMessage());
                                        } catch (IllegalStateException e) {
                                            LOGGER.log(Level.SEVERE, e.getMessage());
                                        } catch (Exception e) {
                                            LOGGER.log(Level.SEVERE, e.getMessage());
                                        }
                                    }
                                    break;
                                }
                            }
                            JackRabbitRepo.this.lock.notify();
                        }

                    } catch (RepositoryException e1) {
                        LOGGER.log(Level.SEVERE, e1.getMessage());
                    }
                }
            };
            ObservationManager observationManager = this.currentSession.getWorkspace().getObservationManager();
            observationManager.addEventListener(listener, Event.NODE_ADDED, "/", true, null, null, false);

        } catch (LoginException e) {
            throw new RepoAbstractionException(e);
        } catch (RepositoryException e) {
            throw new RepoAbstractionException(e);
        } catch (IllegalStateException e2) {
            throw new RepoAbstractionException(e2);
        } catch (UnsupportedOperationException e2) {
            throw new RepoAbstractionException(e2);
        } catch (Exception e2) {
            throw new RepoAbstractionException(e2);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see cz.incad.fcrepo.RepoAbstraction#close()
     */
    @Override
    public void close() throws RepoAbstractionException {
        if (this.currentSession != null) {
            this.currentSession.logout();
            this.currentSession = null;
        }
    }

    /**
     * @throws RepositoryException
     * 
     */
    public JackRabbitRepo() {
        super();
        try {
            File f = new File(JackRabbitUtils.getJackRabbitFolder());
            if (!f.exists()) {
                f.mkdirs();
            }
            this.repo = JcrUtils.getRepository(new File(JackRabbitUtils.getJackRabbitFolder()).toURI().toString());
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }

    }

    private synchronized void namespaces() throws AccessDeniedException, NamespaceException,
            UnsupportedRepositoryOperationException, RepositoryException {
        NamespaceRegistry registry = this.currentSession.getWorkspace().getNamespaceRegistry();
        List<String> list = Arrays.asList(registry.getURIs());
        if (!list.contains(FedoraNamespaces.KRAMERIUS_URI)) {
            registry.registerNamespace("kramerius", FedoraNamespaces.KRAMERIUS_URI);
        }
        if (!list.contains(FedoraNamespaces.FEDORA_MODELS_URI)) {
            registry.registerNamespace("fedora-models", FedoraNamespaces.FEDORA_MODELS_URI);
        }

        // kramerius resource
        NodeTypeManager mgr = currentSession.getWorkspace().getNodeTypeManager();
        NodeTypeTemplate krameriusResource = mgr.createNodeTypeTemplate();
        krameriusResource.setName("kramerius:resource");
        krameriusResource.setMixin(true);

        // kramerius resource must have fedora model property and property must
        // correspond with model node
        PropertyDefinitionTemplate propDefn = mgr.createPropertyDefinitionTemplate();
        propDefn.setName("fedora-models:model");
        propDefn.setRequiredType(PropertyType.STRING);

        // TODO: Change it
        propDefn.setMandatory(false);

        krameriusResource.getPropertyDefinitionTemplates().add(propDefn);

        // datastream
        NodeTypeTemplate dataStream = mgr.createNodeTypeTemplate();
        dataStream.setName("kramerius:datastream");
        dataStream.setMixin(true);

        // kramerius model; model
        NodeTypeTemplate modelResource = mgr.createNodeTypeTemplate();
        modelResource.setName("kramerius:model");
        modelResource.setMixin(true);

        NodeTypeDefinition[] nodeTypes = new NodeTypeDefinition[] { krameriusResource, dataStream, modelResource };
        mgr.registerNodeTypes(nodeTypes, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see cz.incad.fcrepo.RepoAbstraction#startTransaction()
     */
    @Override
    public void startTransaction() throws RepoAbstractionException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see cz.incad.fcrepo.RepoAbstraction#commitTransaction()
     */
    @Override
    public void commitTransaction() throws RepoAbstractionException {
        try {
            if (this.currentSession != null) {
                this.currentSession.save();
            }

            // wait until all items are proceseed processed
            synchronized (lock) {
                while (this.processed.get() < this.inserted.get()) {
                    lock.wait(TIMEOUT);
                }
            }
        } catch (AccessDeniedException e) {
            throw new RepoAbstractionException(e);
        } catch (ItemExistsException e) {
            throw new RepoAbstractionException(e);
        } catch (ReferentialIntegrityException e) {
            throw new RepoAbstractionException(e);
        } catch (ConstraintViolationException e) {
            throw new RepoAbstractionException(e);
        } catch (InvalidItemStateException e) {
            throw new RepoAbstractionException(e);
        } catch (VersionException e) {
            throw new RepoAbstractionException(e);
        } catch (LockException e) {
            throw new RepoAbstractionException(e);
        } catch (NoSuchNodeTypeException e) {
            throw new RepoAbstractionException(e);
        } catch (RepositoryException e) {
            throw new RepoAbstractionException(e);
        } catch (InterruptedException e) {
            throw new RepoAbstractionException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see cz.incad.fcrepo.RepoAbstraction#rollbackTransaction()
     */
    @Override
    public void rollbackTransaction() throws RepoAbstractionException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see cz.incad.fcrepo.RepoAbstraction#createObject(java.lang.String)
     */
    @Override
    public synchronized RepositoryObjectAbstraction createObject(String ident) throws RepoAbstractionException {
        try {
            Node rNode = this.currentSession.getRootNode();
            // specify type which is unique
            if (rNode.hasNode(ident)) {
                rNode.getNode(ident).remove();
            }
            Node kramerius = rNode.addNode(ident);
            kramerius.addMixin("mix:lockable");
            kramerius.addMixin("kramerius:resource");
            Calendar instance = Calendar.getInstance();
            instance.setTime(new Date());
            kramerius.setProperty(JcrConstants.JCR_LASTMODIFIED, instance);
            this.inserted.incrementAndGet();
            return new JackRabbitRepoObject(kramerius);
        } catch (RepositoryException e) {
            throw new RepoAbstractionException(e);
        }
    }

}
