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
package cz.incad.kramerius.processes.template.impl;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletContext;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.template.OutputTemplateFactory;
import cz.incad.kramerius.processes.template.ProcessInputTemplate;
import cz.incad.kramerius.processes.template.ProcessOutputTemplate;

public class OutputTemplateFactoryImpl implements OutputTemplateFactory {

    @Inject
    Provider<ServletContext> contextProvider;

    @Override
    public ProcessOutputTemplate create(String clz) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class<?> clazs = Class.forName(clz);
        Object objInstance = clazs.newInstance();
        Injector inj = (Injector) this.contextProvider.get().getAttribute(Injector.class.getName());
        inj.injectMembers(objInstance);
        return (ProcessOutputTemplate) objInstance;
    }

}
