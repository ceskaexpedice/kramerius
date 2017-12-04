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
package cz.incad.kramerius.utils.mods;

import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

public class InternalPartBuilder extends AbstractBuilder {

    private static final String APPLICABLE_MODEL = "internalpart";

    public static final String MODS_TITLE="mods:title";
    public static final String MODS_SUBTITLE="mods:subTitle";
    public static final String MODS_PARTNAME="mods:partName";

    public static final String MODS_LIST="mods:list";
    public static final String MODS_PAGENUMBER="mods:pageNumber";

//    <mods:modsCollection xmlns:mods="http://www.loc.gov/mods/v3">
//    <mods:mods version="3.3">
//        <mods:titleInfo>
//            <mods:title>Národní listy</mods:title>
//            <mods:subTitle></mods:subTitle>
//            <mods:partName></mods:partName>
//        </mods:titleInfo>

//    <mods:part type="Article">
//    <mods:extent unit="pages">
//        <mods:list>[104]-120 pp.</mods:list>
//
//    </mods:extent>
//    <mods:detail type="pageNumber">
//        <mods:number>[104]</mods:number>
//    </mods:detail>
//</mods:part>

    @Override
    public void build(Document document, Map<String, List<String>> map, String model) throws XPathExpressionException {
        // TODO Auto-generated method stub
    }
}
