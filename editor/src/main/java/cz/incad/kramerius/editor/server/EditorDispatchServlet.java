/*
 * Copyright (C) 2010 Jan Pokorsky
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

package cz.incad.kramerius.editor.server;

import com.google.inject.Injector;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import net.customware.gwt.dispatch.server.BatchActionHandler;
import net.customware.gwt.dispatch.server.DefaultActionHandlerRegistry;
import net.customware.gwt.dispatch.server.Dispatch;
import net.customware.gwt.dispatch.server.SimpleDispatch;
import net.customware.gwt.dispatch.server.standard.AbstractStandardDispatchServlet;

/**
 * XXX use GuiceStandardDispatchServlet instead
 *
 * @author Jan Pokorsky
 */
public final class EditorDispatchServlet extends AbstractStandardDispatchServlet {

    private final DefaultActionHandlerRegistry handlerRegistry = new DefaultActionHandlerRegistry();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        Injector injector = (Injector) getServletContext().getAttribute(Injector.class.getName());
//        log("#EditorDispatchServlet.init.injector: " + injector);
//        System.out.println("#EditorDispatchServlet.init.injector: " + injector);

        handlerRegistry.addHandler(new BatchActionHandler());

        GetKrameriusObjectQueryHandler getContainerHandler =
                injector.getInstance(GetKrameriusObjectQueryHandler.class);
        handlerRegistry.addHandler(getContainerHandler);

        SaveRelationsQueryHandler saveRelationsQueryHandler =
                injector.getInstance(SaveRelationsQueryHandler.class);
        handlerRegistry.addHandler(saveRelationsQueryHandler);

        GetSuggestionQueryHandler getSuggestionQueryHandler =
                injector.getInstance(GetSuggestionQueryHandler.class);
        handlerRegistry.addHandler(getSuggestionQueryHandler);
    }

    @Override
    protected Dispatch getDispatch() {
        return new SimpleDispatch(handlerRegistry);
    }

}