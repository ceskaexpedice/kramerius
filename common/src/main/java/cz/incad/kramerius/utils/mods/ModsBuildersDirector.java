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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

public class ModsBuildersDirector {

    private BuilderFilter builderFilter;
    
    List<AbstractBuilder> BUILDERS = new ArrayList<AbstractBuilder>(); {
        BUILDERS.add(new AuthorBuilder());
        BUILDERS.add(new ArticleTitleBuilder());
        BUILDERS.add(new IdentifiersBuilder());
        BUILDERS.add(new PageNumbersBuilder());
        BUILDERS.add(new PeriodicalIssueNumberBuilder());
        BUILDERS.add(new PeriodicalVolumeNumberBuilder());
        BUILDERS.add(new PublisherBuilder());
        BUILDERS.add(new TitleBuilder());
    }

    public BuilderFilter getBuilderFilter() {
        return builderFilter;
    }
    
    public void setBuilderFilter(BuilderFilter builderFilter) {
        this.builderFilter = builderFilter;
    }
    
    public void build(Document document, Map<String, List<String>> map, String modelName) throws XPathExpressionException {
        for (AbstractBuilder builder : BUILDERS) {
            boolean accept = getBuilderFilter() != null ? getBuilderFilter().canBuild(builder) : true;
            if (accept) {
                builder.build(document, map, modelName);
            }
        }
    }
}
