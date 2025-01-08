/**
 * Copyright ©2023 Accenture and/or its affiliates. All Rights Reserved.
 * <p>
 * Permission to any use, copy, modify, and distribute this software and
 * its documentation for any purpose is subject to a licensing agreement
 * duly entered into with the copyright owner or its affiliate.
 * <p>
 * All information contained herein is, and remains the property of Accenture
 * and/or its affiliates and its suppliers, if any.  The intellectual and
 * technical concepts contained herein are proprietary to Accenture and/or
 * its affiliates and its suppliers and may be covered by one or more patents
 * or pending patent applications in one or more jurisdictions worldwide,
 * and are protected by trade secret or copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless
 * prior written permission is obtained from Accenture and/or its affiliates.
 */
package cz.inovatika.kramerius.fedora.om;


import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.kramerius.fedora.om.processingindex.ProcessingIndexFeeder;
import cz.inovatika.kramerius.fedora.om.repository.Repository;
import cz.inovatika.kramerius.fedora.RepositoryException;
import cz.inovatika.kramerius.fedora.om.repository.impl.AkubraDOManager;
import cz.inovatika.kramerius.fedora.om.repository.impl.RepositoryImpl;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;

import java.io.IOException;

/**
 * AkubraRepositoryFactory
 * @author ppodsednik
 */
public final class RepositoryFactory {

  private RepositoryFactory() {
  }

  public static Repository createAkubraRepository() throws RepositoryException {
    try {
      ProcessingIndexFeeder processingIndexFeeder = new ProcessingIndexFeeder(createProcessingUpdateClient());
      AkubraDOManager akubraDOManager = new AkubraDOManager(createCacheManager());
      return new RepositoryImpl(processingIndexFeeder, akubraDOManager);
    } catch (IOException e) {
      throw new RepositoryException(e);
    }

  }

  private static CacheManager createCacheManager() {
    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
    cacheManager.init();
    return cacheManager;
  }

  /* TODO
  private SolrClient processingQueryClient() {
    String processingSolrHost = KConfiguration.getInstance().getSolrProcessingHost();
    return new HttpSolrClient.Builder(processingSolrHost).build();
  }*/

  private static SolrClient createProcessingUpdateClient() {
    String processingSolrHost = KConfiguration.getInstance().getSolrProcessingHost();
    return new ConcurrentUpdateSolrClient.Builder(processingSolrHost).withQueueSize(100).build();
  }


}
