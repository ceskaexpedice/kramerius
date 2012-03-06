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
package cz.incad.kramerius;

import junit.framework.Assert;

import org.junit.Test;

import cz.incad.Kramerius.HandleServlet;


public class HandleServletTest {

    @Test
    public void testDisectHandle() {
        String url="http://localhost:8080/search/handle/uuid:045b1250-7e47-11e0-add1-000d606f5dc6";
        String handle = HandleServlet.disectHandle(url);
        Assert.assertEquals("uuid:045b1250-7e47-11e0-add1-000d606f5dc6",handle);
    }

    @Test
    public void testDisectHandleWithPage() {
        String url="http://localhost:8080/search/handle/uuid:045b1250-7e47-11e0-add1-000d606f5dc6/@2";
        String handle = HandleServlet.disectHandle(url);
        Assert.assertEquals("uuid:045b1250-7e47-11e0-add1-000d606f5dc6/@2",handle);
    }

}
