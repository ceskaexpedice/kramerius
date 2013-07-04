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
package cz.incad.Kramerius.views;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.service.TextsService;

public class WellcomeViewObject {

    private static final String DEFAULT_INTRO_CONSTANT = "default_intro";

    private static final String INTRO_CONSTANT = "intro";

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(WellcomeViewObject.class.getName());
    
    @Inject
    TextsService textService;
    
    @Inject
    Provider<Locale> provider;

    @Inject
    IsActionAllowed actionAllowed;
    
    public String getIntro() throws IOException {
        boolean operationPermited = actionAllowed.isActionAllowed(SecuredActions.EDIT_INFO_TEXT.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null, ObjectPidsPath.REPOSITORY_PATH);
        return operationPermited ? getTextIntro() : getTextIntro();
    }
    
    public String getEditIntro() throws IOException {
    	StringTemplate template = stGroup().getInstanceOf("editor");
        template.setAttribute("text", getTextIntro());
        template.setAttribute("lang", provider.get().getLanguage());
        return template.toString();
    }
    
    
    public String getTextIntro() throws IOException {
        if (textService.isAvailable(INTRO_CONSTANT, provider.get())) {
            return textService.getText(INTRO_CONSTANT, provider.get());
        } else {
            return textService.getText(DEFAULT_INTRO_CONSTANT,provider.get());
        }
    }
    
    
    private static StringTemplateGroup stGroup() {
        InputStream is = WellcomeViewObject.class.getResourceAsStream("wellcomehtml.stg");
        StringTemplateGroup grp = new StringTemplateGroup(new InputStreamReader(is), DefaultTemplateLexer.class);
        return grp;
    }

}
