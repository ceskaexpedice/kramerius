package cz.incad.kramerius.views;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;

import antlr.RecognitionException;

import com.google.inject.Provider;

import cz.incad.Kramerius.views.ProcessesViewObject;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.service.TextsService;

public class ProcessesViewObjectTest {


    
    
    private static HttpServletRequest req(String size, String pageNumber, String ordering, String filter) {
        //http://krameriusdemo.mzk.cz/search/inc/admin/_processes_data.jsp?size=20&ordering=PLANNED&type=DESC&_=1396603167260 -> last page
        HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(req.getParameter("size")).andReturn(size).anyTimes();
        EasyMock.expect(req.getParameter("page")).andReturn(pageNumber).anyTimes();
        EasyMock.expect(req.getParameter("ordering")).andReturn(ordering).anyTimes();
        EasyMock.expect(req.getParameter("filter")).andReturn(filter).anyTimes();
        EasyMock.expect(req.getParameter("type")).andReturn(null).anyTimes();
        return req;
    }
    
    @Test
    public void testNumberOfPages_OnePage1() throws RecognitionException {
        LRProcessManager lrp = EasyMock.createMock(LRProcessManager.class);
        HttpServletRequest req = req("20",null, null,null);
        
        EasyMock.expect(lrp.getNumberOfLongRunningProcesses(null)).andReturn(19);
        
        
        EasyMock.replay(lrp, req);
        
        ProcessesViewObject pview = new ProcessesViewObject();
        pview.setRequestProvider(new _RequestProvider(req));
        pview.setProcessManager(lrp);
        pview.init();
        
        int numberOfPages = pview.getNumberOfPages();
        System.out.println(numberOfPages);
        System.out.println("firstpage :"+pview.getFirstPage());
        System.out.println("lastpage :"+pview.getLastPage());
        System.out.println("page:"+pview.getPage());
        System.out.println("page next ? :"+pview.getHasNext());
        System.out.println("page previous ? :"+pview.getHasPrevious());
        //Assert.assertTrue(numberOfPages == 1);
        
    }

   @Test
    public void testNumberOfPages_OnePage2() throws RecognitionException {
        LRProcessManager lrp = EasyMock.createMock(LRProcessManager.class);
        HttpServletRequest req = req("20",null, null,null);
        
        EasyMock.expect(lrp.getNumberOfLongRunningProcesses(null)).andReturn(27);
        
        
        EasyMock.replay(lrp, req);
        
        ProcessesViewObject pview = new ProcessesViewObject();
        pview.setRequestProvider(new _RequestProvider(req));
        pview.setProcessManager(lrp);
        pview.init();
        
        int numberOfPages = pview.getNumberOfPages();
        System.out.println(numberOfPages);
        System.out.println("firstpage :"+pview.getFirstPage());
        System.out.println("lastpage :"+pview.getLastPage());
        System.out.println("page:"+pview.getPage());
        System.out.println("page next ? :"+pview.getHasNext());
        System.out.println("page previous ? :"+pview.getHasPrevious());

        
        Assert.assertTrue(numberOfPages == 1);
        
    }

    private class _RequestProvider implements Provider<HttpServletRequest>{
        
        private HttpServletRequest req;
        
        
        public _RequestProvider(HttpServletRequest req) {
            super();
            this.req = req;
        }


        @Override
        public HttpServletRequest get() {
            return this.req;
        }
        
        
    }
}
