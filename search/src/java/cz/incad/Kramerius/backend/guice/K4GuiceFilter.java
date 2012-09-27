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
/**
 * 
 */
package cz.incad.Kramerius.backend.guice;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.google.inject.Injector;

/**
 * Guice filter TODO: Do it by guice way
 * @author pavels
 */
public abstract class K4GuiceFilter implements Filter{

    @Override
    public abstract void destroy();

    @Override
    public abstract void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException;

    @Override
    public void init(FilterConfig filter) throws ServletException {
        Injector injector = getInjector(filter);
        injector.injectMembers(this);
    }

    
    protected Injector getInjector(FilterConfig config) {
        return (Injector) config.getServletContext().getAttribute(Injector.class.getName());
    }

    
}
