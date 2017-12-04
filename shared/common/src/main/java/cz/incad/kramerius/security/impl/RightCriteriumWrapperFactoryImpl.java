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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;

import cz.incad.kramerius.security.CriteriumType;
import cz.incad.kramerius.security.RightCriterium;
import cz.incad.kramerius.security.RightCriteriumLoader;
import cz.incad.kramerius.security.RightCriteriumParams;
import cz.incad.kramerius.security.RightCriteriumWrapper;
import cz.incad.kramerius.security.RightCriteriumWrapperFactory;
import cz.incad.kramerius.security.SecuredActions;

public class RightCriteriumWrapperFactoryImpl implements RightCriteriumWrapperFactory {
    
    Set<RightCriteriumLoader> loaders;
    
    @Inject
    public RightCriteriumWrapperFactoryImpl(Set<RightCriteriumLoader> loaders) {
        super();
        this.loaders = loaders;
    }


    @Override
    public RightCriteriumWrapper createCriteriumWrapper(String qname) {
        for (RightCriteriumLoader loader : this.loaders) {
            if (loader.isDefined(qname)) {
                return new RightCriteriumWrapperImpl(loader.createCriterium(qname),-1, loader.getCriteriumType());
            }
        }
        return null;
    }


    @Override
    public RightCriteriumWrapper loadExistingWrapper(CriteriumType criteriumType, String qname, int identifier, RightCriteriumParams params) {
        for (RightCriteriumLoader loader : this.loaders) {
            CriteriumType loaderCritType = loader.getCriteriumType();
            if (loaderCritType.equals(criteriumType)) {
                RightCriteriumWrapperImpl wrapper = new RightCriteriumWrapperImpl(loader.createCriterium(qname), identifier, criteriumType);
                wrapper.setCriteriumParams(params);
                return wrapper;
            }
        }
        return null;
    }


    @Override
    public List<RightCriteriumWrapper> createAllCriteriumWrappers() {
        List<RightCriteriumWrapper> wrappers = new ArrayList<RightCriteriumWrapper>();
        for (RightCriteriumLoader loader : this.loaders) {
            List<RightCriterium> criteriums = loader.getCriteriums();
            for (RightCriterium rCrit : criteriums) {
                wrappers.add(new RightCriteriumWrapperImpl(rCrit,-1, loader.getCriteriumType()));
            }
        }
        return wrappers;
    }


    @Override
    public List<RightCriteriumWrapper> createAllCriteriumWrappers(SecuredActions... actions) {
        List<RightCriteriumWrapper> wrappers = new ArrayList<RightCriteriumWrapper>();
        for (RightCriteriumLoader loader : this.loaders) {
            List<RightCriterium> criteriums = loader.getCriteriums();
            for (RightCriterium rCrit : criteriums) {
                wrappers.add(new RightCriteriumWrapperImpl(rCrit,-1, loader.getCriteriumType()));
            }
        }
        return wrappers;
    }

    
    
    
//    public RightCriterium createCriterium(int critId, int critParamId, String qname, String shortDesc, String longDesc,Object[] objs) {
//        try {
//            Class<? extends RightCriterium> clz = (Class<? extends RightCriterium>) Class.forName(qname);
//            RightCriteriumWrapperImpl crc = new RightCriteriumWrapperImpl(clz, critId);
//
//            RightCriteriumParams params = new RightCriteriumParamsImpl(critParamId);
//            params.setObjects(objs);
//            params.setLongDescription(longDesc);
//            params.setShortDescription(shortDesc);
//            //crc.setObjects(objs);
//            crc.setCriteriumParams(params);
//            
//            return crc;
//        } catch (ClassNotFoundException e) {
//            throw new IllegalStateException(e);
//        }
//    }
}
