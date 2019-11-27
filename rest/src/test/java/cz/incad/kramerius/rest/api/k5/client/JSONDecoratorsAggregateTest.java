/*
 * Copyright (C) 2013 Pavel Stastny
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
package cz.incad.kramerius.rest.api.k5.client;

import static cz.incad.kramerius.fedora.impl.DataPrepare.dataStreams;
import static cz.incad.kramerius.fedora.impl.DataPrepare.drobnustkyRelsExt;
import static cz.incad.kramerius.fedora.impl.DataPrepare.drobnustkyWithIMGFULL;
import static cz.incad.kramerius.fedora.impl.DataPrepare.narodniListyRelsExt;
import static cz.incad.kramerius.solr.impl.SolrPrepare.solrDataDocument;
import static cz.incad.kramerius.solr.impl.SolrPrepare.solrMemoPrepare;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.easymock.EasyMock;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.impl.DataPrepare;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.rest.api.guice.ApiServletModule;
import cz.incad.kramerius.rest.api.k5.client.item.ItemResource;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;

public class JSONDecoratorsAggregateTest {

    @Test
    public void duplicateCheck() throws IOException,
            ParserConfigurationException, SAXException, LexerException,
            SecurityException, NoSuchMethodException {
        StatisticsAccessLog aclog = EasyMock
                .createMock(StatisticsAccessLog.class);

        FedoraAccess fa = createMockBuilder(FedoraAccessImpl.class)
                .withConstructor(KConfiguration.getInstance(), aclog)
                .createMock();

        SolrAccess sa = createMockBuilder(SolrAccessImpl.class).createMock();
        
        SolrMemoization memo = EasyMock.createMock(SolrMemoization.class);
        EasyMock.expect(memo.getRememberedIndexedDoc(DataPrepare.DROBNUSTKY_PIDS[0])).andReturn(null);
        EasyMock.expect(memo.askForIndexDocument(DataPrepare.DROBNUSTKY_PIDS[0])).andReturn(null);

        replay(fa, aclog, sa, memo);

        Injector injector = Guice.createInjector(new JSONDecTestModule(sa, memo, fa,
                null, null));
        JSONDecoratorsAggregate aggregate = injector
                .getInstance(JSONDecoratorsAggregate.class);
        List<JSONDecorator> list = aggregate.getDecorators();
        List<String> KEYS = new ArrayList<String>();
        for (JSONDecorator jsonDecorator : list) {
            String key = jsonDecorator.getKey();
            Assert.assertFalse(KEYS.contains(key));
            KEYS.add(key);
        }
    }

    @Test
    public void testApplyBasicPDF() throws IOException,
            ParserConfigurationException, SAXException, LexerException,
            SecurityException, NoSuchMethodException, JSONException {
        StatisticsAccessLog aclog = EasyMock
                .createMock(StatisticsAccessLog.class);

        FedoraAccess fa = createMockBuilder(FedoraAccessImpl.class)
                .withConstructor(KConfiguration.getInstance(), aclog)
                .addMockedMethod("getRelsExt").createMock();

        narodniListyRelsExt(fa);
        drobnustkyRelsExt(fa);

        SolrAccess sa = createMockBuilder(SolrAccessImpl.class)
                .addMockedMethod(
                        SolrAccess.class.getMethod("request", String.class,
                                String.class)).createMock();

        SolrMemoization memo = EasyMock.createMock(SolrMemoization.class);
        EasyMock.expect(memo.getRememberedIndexedDoc(DataPrepare.DROBNUSTKY_PIDS[0])).andReturn(null);
        EasyMock.expect(memo.askForIndexDocument(DataPrepare.DROBNUSTKY_PIDS[0])).andReturn(null);

        
        HttpServletRequest request = EasyMock
                .createMock(HttpServletRequest.class);
        HttpServletResponse response = EasyMock
                .createMock(HttpServletResponse.class);

        replay(fa, aclog, sa, memo, request, response);

        Injector injector = Guice.createInjector(new JSONDecTestModule(sa, memo, fa,
                request, response));
        JSONDecoratorsAggregate aggregate = injector
                .getInstance(JSONDecoratorsAggregate.class);

        List<JSONDecorator> list = aggregate.getDecorators();

        String pid = DataPrepare.DROBNUSTKY_PIDS[0] + "@2";
        // pdf pid
        String basicUrl = ItemResource.basicURL(pid);

        List<JSONDecorator> acceptedDecorators = new ArrayList<JSONDecorator>();
        for (JSONDecorator jsonDec : list) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("pid", pid);
            jsonObject.put("model", "page");
            if (jsonDec.apply(jsonObject, basicUrl)) {
                acceptedDecorators.add(jsonDec);
            }
        }
        Assert.assertTrue(acceptedDecorators.size() == 13);

    }

    @Test
    public void testApplyBasic() throws IOException,
            ParserConfigurationException, SAXException, LexerException,
            SecurityException, NoSuchMethodException, JSONException {
        StatisticsAccessLog aclog = EasyMock
                .createMock(StatisticsAccessLog.class);

        FedoraAccess fa = createMockBuilder(FedoraAccessImpl.class)
                .withConstructor(KConfiguration.getInstance(), aclog)
                .addMockedMethod("getRelsExt").createMock();

        narodniListyRelsExt(fa);
        drobnustkyRelsExt(fa);

        SolrAccess sa = createMockBuilder(SolrAccessImpl.class)
                .addMockedMethod(
                        SolrAccess.class.getMethod("request", String.class,
                                String.class)).createMock();

        SolrMemoization memo = EasyMock.createMock(SolrMemoization.class);
        EasyMock.expect(memo.getRememberedIndexedDoc(DataPrepare.DROBNUSTKY_PIDS[0])).andReturn(null);
        EasyMock.expect(memo.askForIndexDocument(DataPrepare.DROBNUSTKY_PIDS[0])).andReturn(null);

        
        HttpServletRequest request = EasyMock
                .createMock(HttpServletRequest.class);
        HttpServletResponse response = EasyMock
                .createMock(HttpServletResponse.class);

        replay(fa, aclog, sa, memo, request, response);

        Injector injector = Guice.createInjector(new JSONDecTestModule(sa, memo, fa,
                request, response));
        JSONDecoratorsAggregate aggregate = injector
                .getInstance(JSONDecoratorsAggregate.class);

        List<JSONDecorator> list = aggregate.getDecorators();

        String pid = DataPrepare.DROBNUSTKY_PIDS[0];
        String basicUrl = ItemResource.basicURL(pid);

        List<JSONDecorator> acceptedDecorators = new ArrayList<JSONDecorator>();
        for (JSONDecorator jsonDec : list) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("pid", pid);
            jsonObject.put("model", fa.getKrameriusModelName(pid));
            if (jsonDec.apply(jsonObject, basicUrl)) {
                acceptedDecorators.add(jsonDec);
            }
        }
        Assert.assertTrue(acceptedDecorators.size() == 12);
    }

    @Test
    public void testDecorateBasicPDF() throws IOException,
            ParserConfigurationException, SAXException, LexerException,
            SecurityException, NoSuchMethodException, JSONException {

        StatisticsAccessLog aclog = EasyMock
                .createMock(StatisticsAccessLog.class);

        FedoraAccess fa = createMockBuilder(FedoraAccessImpl.class)
                .withConstructor(KConfiguration.getInstance(), aclog)
                .createMock();

        SolrAccess sa = createMockBuilder(SolrAccessImpl.class)
                .addMockedMethod(
                        SolrAccess.class.getMethod("request", String.class,
                                String.class))
                .addMockedMethod("getSolrDataDocument").createMock();

        solrDataDocument(sa, DataPrepare.DROBNUSTKY_PIDS[0] + "/@2");

        SolrMemoization memo = EasyMock.createMock(SolrMemoization.class);
        solrMemoPrepare(memo, DataPrepare.DROBNUSTKY_PIDS[0] + "/@2");

        
        HttpServletRequest request = EasyMock
                .createMock(HttpServletRequest.class);
        EasyMock.expect(request.getRequestURL())
                .andReturn(
                        new StringBuffer(
                                "http://localhost:8080/search/api/v5.0/k5/item/"
                                        + DataPrepare.DROBNUSTKY_PIDS[0]))
                .anyTimes();
        EasyMock.expect(request.getHeader("x-forwarded-host")).andReturn(null)
                .anyTimes();

        HttpServletResponse response = EasyMock
                .createMock(HttpServletResponse.class);

        replay(fa, aclog, sa, memo, request, response);

        Injector injector = Guice.createInjector(new JSONDecTestModule(sa, memo, fa,
                request, response));
        JSONDecoratorsAggregate aggregate = injector
                .getInstance(JSONDecoratorsAggregate.class);

        List<JSONDecorator> list = aggregate.getDecorators();

        String pid = DataPrepare.DROBNUSTKY_PIDS[0] + "/@2";
        String basicUrl = ItemResource.basicURL(pid);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("pid", pid);
        jsonObject.put("model", "page");
        Map<String, Object> context = new HashMap<String, Object>();
        for (JSONDecorator jsonDec : list) {
            if (jsonDec.apply(jsonObject, basicUrl)) {
                jsonDec.decorate(jsonObject, context);
            }
        }

        Assert.assertTrue(jsonObject.has("title"));
        Assert.assertTrue(jsonObject.getString("title").equals("Drobnůstky"));

        Assert.assertTrue(jsonObject.has("root_title"));
        Assert.assertTrue(jsonObject.getString("root_title").equals(
                "Drobnůstky"));
        
         //TODO: context and datanode ?
//        Assert.assertTrue(jsonObject.has("context"));
//        Assert.assertTrue(jsonObject.has("datanode"));
    }

    @Test
    public void testDecorateBasic() throws IOException,
            ParserConfigurationException, SAXException, LexerException,
            SecurityException, NoSuchMethodException, JSONException {

        StatisticsAccessLog aclog = EasyMock
                .createMock(StatisticsAccessLog.class);

        FedoraAccess fa = createMockBuilder(FedoraAccessImpl.class)
                .withConstructor(KConfiguration.getInstance(), aclog)
                .addMockedMethod("getRelsExt")
                .addMockedMethod("isImageFULLAvailable")
                .addMockedMethod("getFedoraDataStreamsList").createMock();

        // narodniListyRelsExt(fa);
        drobnustkyRelsExt(fa);
        drobnustkyWithIMGFULL(fa);
        dataStreams(fa, DataPrepare.DROBNUSTKY_PIDS[0]);

        SolrAccess sa = createMockBuilder(SolrAccessImpl.class)
                .addMockedMethod(
                        SolrAccess.class.getMethod("request", String.class,
                                String.class))
                .addMockedMethod("getSolrDataDocument").createMock();

        solrDataDocument(sa, DataPrepare.DROBNUSTKY_PIDS[0]);

        SolrMemoization memo = EasyMock.createMock(SolrMemoization.class);
        solrMemoPrepare(memo, DataPrepare.DROBNUSTKY_PIDS[0]);
        

        
        HttpServletRequest request = EasyMock
                .createMock(HttpServletRequest.class);
        EasyMock.expect(request.getRequestURL())
                .andReturn(
                        new StringBuffer(
                                "http://localhost:8080/search/api/v5.0/k5/item/"
                                        + DataPrepare.DROBNUSTKY_PIDS[0]))
                .anyTimes();
        EasyMock.expect(request.getHeader("x-forwarded-host")).andReturn(null)
                .anyTimes();

        HttpServletResponse response = EasyMock
                .createMock(HttpServletResponse.class);

        
        
        replay(fa, aclog, sa, memo, request, response);

        Injector injector = Guice.createInjector(new JSONDecTestModule(sa, memo, fa,
                request, response));
        JSONDecoratorsAggregate aggregate = injector
                .getInstance(JSONDecoratorsAggregate.class);

        List<JSONDecorator> list = aggregate.getDecorators();

        String pid = DataPrepare.DROBNUSTKY_PIDS[0];
        String basicUrl = ItemResource.basicURL(pid);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("pid", pid);
        jsonObject.put("model", fa.getKrameriusModelName(pid));
        Map<String, Object> context = new HashMap<String, Object>();
        for (JSONDecorator jsonDec : list) {
            if (jsonDec.apply(jsonObject, basicUrl)) {
                jsonDec.decorate(jsonObject, context);
            }
        }

        Assert.assertTrue(jsonObject.has("title"));
        Assert.assertTrue(jsonObject.getString("title").equals("Drobnůstky"));

        Assert.assertTrue(jsonObject.has("root_title"));
        Assert.assertTrue(jsonObject.getString("root_title").equals(
                "Drobnůstky"));

        Assert.assertTrue(jsonObject.has("context"));
        Assert.assertTrue(jsonObject.has("datanode"));
    }

    public static class JSONDecTestModule extends AbstractModule {

        private SolrAccess solrAccess;
        private SolrMemoization solrMemo;
        private FedoraAccess fedoraAccess;
        private HttpServletRequest request;
        private HttpServletResponse response;


        public JSONDecTestModule(SolrAccess solrAccess, SolrMemoization memo,
                FedoraAccess fedoraAccess, HttpServletRequest request,
                HttpServletResponse response) {
            super();
            this.solrAccess = solrAccess;
            this.solrMemo = memo;
            this.fedoraAccess = fedoraAccess;
            this.request = request;
            this.response = response;
        }

        @Override
        protected void configure() {
            bind(JSONDecoratorsAggregate.class);
            bind(SolrAccess.class).toInstance(this.solrAccess);
            bind(FedoraAccess.class).annotatedWith(
                    Names.named("securedFedoraAccess")).toInstance(
                    this.fedoraAccess);
            bind(SolrMemoization.class).toInstance(this.solrMemo);

            decorators();
        }

        private void decorators() {
            Multibinder<JSONDecorator> decs = Multibinder.newSetBinder(
                    binder(), JSONDecorator.class);

            ApiServletModule.decoratorsBindings(decs);

            // decs.addBinding().to(HandleDecorate.class);
            // decs.addBinding().to(ItemSolrTitleDecorate.class);
            // decs.addBinding().to(SolrContextDecorate.class);
            //
            // decs.addBinding().to(SolrDateDecorate.class);
            // decs.addBinding().to(SolrISSNDecorate.class);
            // decs.addBinding().to(SolrLanguageDecorate.class);
            //
            // decs.addBinding().to(SolrDataNode.class);
            //
            // decs.addBinding().to(ZoomDecorate.class);
            // decs.addBinding().to(PDFDecorate.class);
        }

        @Provides
        public HttpServletRequest getRequest() {
            return request;
        }

        @Provides
        public HttpServletResponse getResponse() {
            return this.response;
        }

    }

}
