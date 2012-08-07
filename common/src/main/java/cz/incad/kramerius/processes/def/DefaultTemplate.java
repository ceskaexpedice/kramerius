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
package cz.incad.kramerius.processes.def;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.annotations.DefaultParameterValue;
import cz.incad.kramerius.processes.annotations.ParameterName;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.processes.template.ProcessInputTemplate;
import cz.incad.kramerius.service.ResourceBundleService;

public class DefaultTemplate implements ProcessInputTemplate {
    
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DefaultTemplate.class.getName());
    
    
    @Inject
    ResourceBundleService resourceBundleService;
    
    @Inject
    Provider<Locale> localeProvider;
    
    
    @Override
    public void renderInput(LRProcessDefinition definition, Writer writer, Properties paramsMapping) throws IOException {
        try {
            
            List<FieldDesc> fdescs = new ArrayList<DefaultTemplate.FieldDesc>();
            
            String mainClass = definition.getMainClass();
            Class<?> clz = Class.forName(mainClass);
            
            Method method = ProcessStarter.annotatedMethod(clz);
            if (method == null) throw new IllegalStateException("cannot find annotated process method !");
            Annotation[][] annots = method.getParameterAnnotations();
            
            Class<?>[] types = method.getParameterTypes();
            for (int i = 0; i < types.length; i++) {
                Annotation[] ann = annots[i];
                Annotation paramAnnot = ProcessStarter.findNameAnnot(ann);
                if (paramAnnot!=null) {
                    String name = ((ParameterName)paramAnnot).value();
                    FieldDesc fdesc = new FieldDesc(name, paramsMapping != null && paramsMapping.containsKey(name)  ?  paramsMapping.get(name) : findDefaultValue(clz, types[i], name));
                    fdescs.add(fdesc);
                }
            }
            

            InputStream iStream = this.getClass().getResourceAsStream("default.st");
            StringTemplateGroup templateGroup = new StringTemplateGroup(new InputStreamReader(iStream,"UTF-8"), DefaultTemplateLexer.class);
            StringTemplate template = templateGroup.getInstanceOf("form");
            ResourceBundle resbundle = resourceBundleService.getResourceBundle("labels", localeProvider.get());
            template.setAttribute("bundle", resourceBundleMap(resbundle));
            template.setAttribute("fields", fdescs);
            template.setAttribute("process", definition.getId());

            writer.write(template.toString());
            
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            throw new IOException(e);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            throw new IOException(e);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            throw new IOException(e);
        }
    }

    
    private Object findDefaultValue(Class processClass, Class expectingType, String name) throws IllegalArgumentException, IllegalAccessException {
        Field[] fields = processClass.getFields();
        for (Field f : fields) {
            if ((f.getType().equals(expectingType)) && (Modifier.isStatic(f.getModifiers()))) {
                DefaultParameterValue annotation = f.getAnnotation(DefaultParameterValue.class);
                if(name.equals(annotation.value())) {
                    return f.get(null);
                }
            }
        }
        return null;
    }


    public static Map<String, String> resourceBundleMap(ResourceBundle bundle) {
        Map<String, String> map = new HashMap<String, String>();
        Set<String> keySet = bundle.keySet();
        for (String key : keySet) {
            map.put(key, bundle.getString(key));
        }
        return map;
    }
    
    
    public static class FieldDesc {
        
        private String name;
        private Object value;
        
        public FieldDesc(String name, Object value) {
            super();
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }
        
        public Object getValue() {
            return value;
        }
        
        public void setValue(Object value) {
            this.value = value;
        }
        
    }
}
