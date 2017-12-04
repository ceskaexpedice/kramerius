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
package cz.incad.kramerius.security.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.security.Right;

public class SortingRightsUtils {

    public static Right[] sortRights(Right[] findRights, ObjectPidsPath path) {

        ArrayList<Right> noCriterium = new ArrayList<Right>();
        ArrayList<Right> negativeFixedPriorty = new ArrayList<Right>();
        ArrayList<Right> positiveFixedPriority = new ArrayList<Right>();
        
        ArrayList<Right> dynamicHintMin = new ArrayList<Right>();
        ArrayList<Right> dynamicHintNormal = new ArrayList<Right>();
        ArrayList<Right> dynamicHintMax = new ArrayList<Right>();
        
        ArrayList<Right> processing = new ArrayList<Right>(Arrays.asList(findRights));
        // vyzobani pravidel bez kriterii 
        for (Iterator iterator = processing.iterator(); iterator.hasNext();) {
            Right right = (Right) iterator.next();
            if (right.getCriteriumWrapper() == null) {
                noCriterium.add(right);
                iterator.remove();
            }
        }
        // vyzobani pravidel s pevne nadefinovanou prioritou
        for (Iterator iterator = processing.iterator(); iterator.hasNext();) {
            Right right = (Right) iterator.next();
            if (right.getFixedPriority() < 0) {
                negativeFixedPriorty.add(right);
                iterator.remove();
            } else if (right.getFixedPriority() > 0) {
                positiveFixedPriority.add(right);
                iterator.remove();
            } 
        }
        
        for (Right right : processing) {
            if (right.getCriteriumWrapper().getRightCriterium() != null) {
                switch (right.getCriteriumWrapper().getRightCriterium().getPriorityHint()) {
                    case MIN: {
                        dynamicHintMin.add(right);
                    }
                    break;

                    case NORMAL: {
                        dynamicHintNormal.add(right);
                    }
                    break;

                    case MAX: {
                        dynamicHintMax.add(right);
                    }
                    break;
                }
            }
        }
        
        SortingRightsUtils.sortByPID(noCriterium, path);
        SortingRightsUtils.sortByFixedPriority(negativeFixedPriorty);
        SortingRightsUtils.sortByFixedPriority(positiveFixedPriority);
        SortingRightsUtils.sortByPID(dynamicHintMax, path);
        SortingRightsUtils.sortByPID(dynamicHintNormal, path);
        SortingRightsUtils.sortByPID(dynamicHintMin, path);
        
        ArrayList<Right> result = new ArrayList<Right>();
        result.addAll(noCriterium);
        result.addAll(positiveFixedPriority);
        result.addAll(dynamicHintMax);
        result.addAll(dynamicHintNormal);
        result.addAll(dynamicHintMin);
        result.addAll(negativeFixedPriorty);
        
        return (Right[]) result.toArray(new Right[result.size()]);
    }

    public static void sortByPID(final List<Right> list, final List<String>pids) {
        Collections.sort(list, new Comparator<Right>() {
    
            @Override
            public int compare(Right o1, Right o2) {
                int thisVal = pids.indexOf(o1.getPid());
                int anotherVal = pids.indexOf(o2.getPid());
                return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
            }
            
            
        });
    }

    public static void sortByPID(final List<Right> list, final ObjectPidsPath path) {
        Collections.sort(list, new Comparator<Right>() {
            
            List<String> pathStrings = Arrays.asList(path.getPathFromLeafToRoot());
            
            @Override
            public int compare(Right o1, Right o2) {
                int thisVal = pathStrings.indexOf(o1.getPid());
                int anotherVal = pathStrings.indexOf(o2.getPid());
                return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
            }
            
        });
    }

    public static void sortByFixedPriority(List<Right> list) {
        Collections.sort(list,  new Comparator<Right>() {
            @Override
            public int compare(Right o1, Right o2) {
                int thisVal = o1.getFixedPriority();
                int anotherVal = o2.getFixedPriority();
                return (thisVal>anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
            }
        });
    }

    
    
    
}
