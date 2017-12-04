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
/**
 * 
 */
package org.kramerius.processes.utils;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author pavels
 *
 */
public class OtherSettingsTemplateTest {

    
    @Test
    public void testTemplateChoose() {
        OtherSettingsTemplate template1 = OtherSettingsTemplate.disectTemplate(true, true);
        Assert.assertEquals(template1, OtherSettingsTemplate.importFedoraStartIndexer);

        OtherSettingsTemplate template2 = OtherSettingsTemplate.disectTemplate(false, true);
        Assert.assertEquals(template2, OtherSettingsTemplate.noFedoraNoIndexer);

        OtherSettingsTemplate template3 = OtherSettingsTemplate.disectTemplate(false, false);
        Assert.assertEquals(template3, OtherSettingsTemplate.noFedoraNoIndexer);

        OtherSettingsTemplate template4 = OtherSettingsTemplate.disectTemplate(true, false);
        Assert.assertEquals(template4, OtherSettingsTemplate.importFedoraNoIndexer);
    }

}
