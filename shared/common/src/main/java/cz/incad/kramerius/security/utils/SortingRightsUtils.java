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

    // check checkDnnt
    public static Right[] sortRights(Right[] findRights, ObjectPidsPath path) {

        ArrayList<Right> noCriterium = new ArrayList<>();
        ArrayList<Right> negativeFixedPriorty = new ArrayList<>();
        ArrayList<Right> positiveFixedPriority = new ArrayList<>();


        // dnnt labels must be sortec according labels
        ArrayList<Right> dnntExclusiveMin = new ArrayList<>();
        ArrayList<Right> dnntExclusiveMax = new ArrayList<>();


        ArrayList<Right> dynamicHintMin = new ArrayList<>();
        ArrayList<Right> dynamicHintNormal = new ArrayList<>();
        ArrayList<Right> dynamicHintMax = new ArrayList<>();

        ArrayList<Right> processing = new ArrayList<>(Arrays.asList(findRights));
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
                    case DNNT_EXCLUSIVE_MIN: {
                        dnntExclusiveMin.add(right);
                    }
                    break;

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

                    case DNNT_EXCLUSIVE_MAX: {
                        dnntExclusiveMax.add(right);
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


        // sort by label priority
        SortingRightsUtils.sortByLabelPriority(dnntExclusiveMin);
        SortingRightsUtils.sortByLabelPriority(dnntExclusiveMax);




        ArrayList<Right> result = new ArrayList<>();
        // how to exclusive
        // dnnt exclusive max
        result.addAll(dnntExclusiveMax);

        result.addAll(noCriterium);
        result.addAll(positiveFixedPriority);

        result.addAll(dynamicHintMax);
        result.addAll(dynamicHintNormal);
        result.addAll(dynamicHintMin);


        // not allowed in case of  DNNT is in place
        result.addAll(negativeFixedPriorty);

        // the last one  - dnnt
        result.addAll(dnntExclusiveMin);


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

    public static void sortByLabelPriority(final List<Right> list) {

        Collections.sort(list, new Comparator<Right>() {

            @Override
            public int compare(Right o1, Right o2) {
                int o1Priority = o1.getCriteriumWrapper() != null && o1.getCriteriumWrapper().getLicense() != null ? o1.getCriteriumWrapper().getLicense().getPriority() : -1;
                int o2Priority = o2.getCriteriumWrapper() != null && o2.getCriteriumWrapper().getLicense() != null ? o2.getCriteriumWrapper().getLicense().getPriority() : -1;

                return (o1Priority<o2Priority ? -1 : (o1Priority==o2Priority ? 0 : 1));
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
