/*
 * Copyright (C) Sep 14, 2023 Pavel Stastny
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

import java.util.Arrays;
import java.util.List;

import cz.incad.kramerius.security.impl.criteria.Licenses;
import cz.incad.kramerius.security.impl.criteria.LicensesGEOIPFiltered;
import cz.incad.kramerius.security.impl.criteria.LicensesIPFiltered;
import cz.incad.kramerius.security.impl.criteria.ReadDNNTLabels;
import cz.incad.kramerius.security.impl.criteria.ReadDNNTLabelsIPFiltered;

public class LicensesCriteriaList {


    public static List<String> NAMES = Arrays.asList(
            ReadDNNTLabels.class.getName(),
            ReadDNNTLabelsIPFiltered.class.getName(),
            Licenses.class.getName(),
            LicensesIPFiltered.class.getName(),
            LicensesGEOIPFiltered.class.getName()
    );
}
