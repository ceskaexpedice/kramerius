package cz.incad.kramerius.auth.mochshib;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;

public class MockHTTPServletInvocationHandler implements InvocationHandler{

    public static class EmbeddedEnumeration  implements  Enumeration<String>{
        private HttpServletRequest request;
        private Hashtable<String,String> table;

        private List<String> list = new ArrayList<>();
        public EmbeddedEnumeration(HttpServletRequest request, Hashtable<String, String> table) {
            this.request = request;
            this.table = table;
            Enumeration headerNames = request.getHeaderNames();
            while(headerNames.hasMoreElements()) list.add((String) headerNames.nextElement());
            Enumeration tableKeys = table.keys();
            while(tableKeys.hasMoreElements()) list.add((String) tableKeys.nextElement());
        }

        @Override
        public boolean hasMoreElements() {
            return !this.list.isEmpty();
        }

        @Override
        public String nextElement() {
            return this.list.remove(0);
        }
    }

    private Hashtable<String, String> attributes = new Hashtable<>();
    private HttpServletRequest request;

    public MockHTTPServletInvocationHandler(Hashtable<String, String> attributes, HttpServletRequest request) {
        this.attributes = attributes;
        this.request = request;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        String methodName = method.getName();
        if (methodName.equals("getHeaderNames")) {
            return new EmbeddedEnumeration(this.request, this.attributes);
        }
        if (methodName.equals("getHeader")) {
            String param = (String) args[0];
            String header = this.request.getHeader(param);
            if (header != null && !header.trim().equals("")) return  header;
            else return this.attributes.get(param);
        }
        return method.invoke(this.request, args);
    }
}
