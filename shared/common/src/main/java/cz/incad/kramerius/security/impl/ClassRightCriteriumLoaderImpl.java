/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.kramerius.security.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;

import cz.incad.kramerius.security.CriteriumType;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumLoader;
import cz.incad.kramerius.security.SecuredActions;

public class ClassRightCriteriumLoaderImpl implements RightCriteriumLoader {
 
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ClassRightCriteriumLoaderImpl.class.getName());

    private List<Class<RightCriterium>> clzs = new ArrayList<Class<RightCriterium>>();

    public ClassRightCriteriumLoaderImpl() {
        this.initFromManifests();
    }
    
    private void initFromManifests() {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources("cz/incad/kramerius/security/res/criteriums");
            while(resources.hasMoreElements()) {
                URL url = resources.nextElement();
                readClasses(url, classLoader);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void readClasses(URL url, ClassLoader classLoader) throws IOException, ClassNotFoundException {
        InputStream is = url.openStream();
        try {
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while((line = bufReader.readLine()) != null) {
                if (!line.startsWith("#")) {
                    Class<RightCriterium> clz = (Class<RightCriterium>) classLoader.loadClass(line.trim());
                    this.clzs.add(clz);
                }
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
        
    }

    
    @Override
    public List<RightCriterium> getCriteriums() {
        try {
            List<RightCriterium> crits = new ArrayList<RightCriterium>();
            for (int i = 0; i < clzs.size(); i++) {
                crits.add((RightCriterium) clzs.get(i).newInstance());
            }
            return crits;
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return new ArrayList<RightCriterium>();
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return new ArrayList<RightCriterium>();
        }
    }

    @Override
    public List<RightCriterium> getCriteriums(SecuredActions... applActions) {
        try {
            List<RightCriterium> crits = new ArrayList<RightCriterium>();
            for (int i = 0; i < clzs.size(); i++) {
                RightCriterium crit = (RightCriterium) clzs.get(i).newInstance();
                List<SecuredActions> actList = Arrays.asList(crit.getApplicableActions());
                for (SecuredActions act : applActions) {
                    if (actList.contains(act)) {
                        crits.add(crit);
                    }
                }
            }
            return crits;
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return new ArrayList<RightCriterium>();
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return new ArrayList<RightCriterium>();
        }
    }


    @Override
    public RightCriterium createCriterium(String criteriumQName) {
        try {
            RightCriterium crit = (RightCriterium) Class.forName(criteriumQName).newInstance();
            return crit;
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return null;
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return null;
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return null;
        }
    }

    @Override
    public boolean isDefined(String qname) {
        for (Class<RightCriterium> clz : this.clzs) {
            if (clz.getName().equals(qname)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public CriteriumType getCriteriumType() {
        CriteriumType returningType = CriteriumType.CLASS;
        return returningType;
    }
}


