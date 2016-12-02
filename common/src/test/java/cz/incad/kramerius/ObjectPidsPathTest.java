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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

import cz.incad.kramerius.AbstractObjectPath.Between;

public class ObjectPidsPathTest {

    @Test
    public void testInjectObjectsPaths() {
        List<String> relsExtPath = new ArrayList<String>() {
            {
                add("uuid:root");
                add("uuid:monograph");
                add("uuid:internalpart");
                add("uuid:page");
            }
        };
        List<String> expecting = new ArrayList<String>() {
            {
                add("uuid:root");
                add("uuid:monograph");
                add("uuid:internalpart");
                add("uuid:article");
                add("uuid:page");
            }
        };

        ObjectPidsPath path = new ObjectPidsPath(relsExtPath.toArray(new String[relsExtPath.size()]));
        TestCase.assertEquals(relsExtPath, Arrays.asList(path.getPathFromRootToLeaf()));

        ObjectPidsPath npath = path.injectObjectBetween("uuid:article",
                new AbstractObjectPath.Between("uuid:internalpart", "uuid:page"));
        String[] pathFromRootToLeaf = npath.getPathFromRootToLeaf();
        Assert.assertEquals(expecting, Arrays.asList(pathFromRootToLeaf));

        npath = path.injectObjectBetween("uuid:article",
                new AbstractObjectPath.Between("uuid:page", "uuid:internalpart"));
        pathFromRootToLeaf = npath.getPathFromRootToLeaf();
        Assert.assertEquals(expecting, Arrays.asList(pathFromRootToLeaf));

    }

    @Test
    public void testInjectObjectsPathsAtTheBeginning() {

        List<String> relsExtPath = new ArrayList<String>() {
            {
                add("uuid:root");
                add("uuid:monograph");
                add("uuid:internalpart");
                add("uuid:page");
            }
        };

        List<String> expecting = new ArrayList<String>() {
            {
                add("uuid:superroot");
                add("uuid:root");
                add("uuid:monograph");
                add("uuid:internalpart");
                add("uuid:page");
            }
        };

        ObjectPidsPath path = new ObjectPidsPath(relsExtPath.toArray(new String[relsExtPath.size()]));
        TestCase.assertEquals(relsExtPath, Arrays.asList(path.getPathFromRootToLeaf()));

        ObjectPidsPath npath = path.injectObjectBetween("uuid:superroot",
                new AbstractObjectPath.Between(null, "uuid:root"));
        String[] pathFromRootToLeaf = npath.getPathFromRootToLeaf();
        Assert.assertEquals(expecting, Arrays.asList(pathFromRootToLeaf));

        npath = path.injectObjectBetween("uuid:superroot", new AbstractObjectPath.Between("uuid:root", null));
        pathFromRootToLeaf = npath.getPathFromRootToLeaf();
        Assert.assertEquals(expecting, Arrays.asList(pathFromRootToLeaf));

    }

    @Test
    public void testInjectObjectsPathsInTheEnd() {

        List<String> relsExtPath = new ArrayList<String>() {
            {
                add("uuid:root");
                add("uuid:monograph");
                add("uuid:internalpart");
                add("uuid:page");
            }
        };

        List<String> expecting = new ArrayList<String>() {
            {
                add("uuid:root");
                add("uuid:monograph");
                add("uuid:internalpart");
                add("uuid:page");
                add("uuid:subpage");
            }
        };

        ObjectPidsPath path = new ObjectPidsPath(relsExtPath.toArray(new String[relsExtPath.size()]));
        TestCase.assertEquals(relsExtPath, Arrays.asList(path.getPathFromRootToLeaf()));

        ObjectPidsPath npath = path.injectObjectBetween("uuid:subpage",
                new AbstractObjectPath.Between(null, "uuid:page"));
        String[] pathFromRootToLeaf = npath.getPathFromRootToLeaf();
        Assert.assertEquals(expecting, Arrays.asList(pathFromRootToLeaf));

        npath = path.injectObjectBetween("uuid:subpage", new AbstractObjectPath.Between("uuid:page", null));
        pathFromRootToLeaf = npath.getPathFromRootToLeaf();
        Assert.assertEquals(expecting, Arrays.asList(pathFromRootToLeaf));

    }

    @Test
    public void testPidPaths() {
        List<String> relsExtPath = new ArrayList<String>() {
            {
                add("uuid:root");
                add("uuid:monograph");
                add("uuid:internalpart");
                add("uuid:page");
            }
        };
        ObjectPidsPath path = new ObjectPidsPath(relsExtPath.toArray(new String[relsExtPath.size()]));
        TestCase.assertEquals(relsExtPath, Arrays.asList(path.getPathFromRootToLeaf()));

        Collections.reverse(relsExtPath);
        TestCase.assertEquals(relsExtPath, Arrays.asList(path.getPathFromLeafToRoot()));
    }

    @Test
    public void testCutHeads() {
        List<String> relsExtPath = new ArrayList<String>() {
            {
                add("uuid:root");
                add("uuid:monograph");
                add("uuid:internalpart");
                add("uuid:page");
            }
        };

        List<String> cutHeadPath0 = new ArrayList<String>() {
            {
                add("uuid:monograph");
                add("uuid:internalpart");
                add("uuid:page");
            }
        };

        List<String> cutHeadPath1 = new ArrayList<String>() {
            {
                add("uuid:internalpart");
                add("uuid:page");
            }
        };

        List<String> cutHeadPath2 = new ArrayList<String>() {
            {
                add("uuid:page");
            }
        };

        ObjectPidsPath path = new ObjectPidsPath(relsExtPath.toArray(new String[relsExtPath.size()]));

        ObjectPidsPath cutHead0 = path.cutHead(0);
        TestCase.assertEquals(Arrays.asList(cutHead0.getPathFromRootToLeaf()), cutHeadPath0);

        ObjectPidsPath cutHead1 = path.cutHead(1);
        TestCase.assertEquals(Arrays.asList(cutHead1.getPathFromRootToLeaf()), cutHeadPath1);

        ObjectPidsPath cutHead2 = path.cutHead(2);
        TestCase.assertEquals(Arrays.asList(cutHead2.getPathFromRootToLeaf()), cutHeadPath2);
    }

    @Test
    public void testCutTails() {
        List<String> relsExtPath = new ArrayList<String>() {
            {
                add("uuid:root");
                add("uuid:monograph");
                add("uuid:internalpart");
                add("uuid:page");
            }
        };

        List<String> cutTailPath0 = new ArrayList<String>() {
            {
                add("uuid:root");
                add("uuid:monograph");
                add("uuid:internalpart");
            }
        };

        List<String> cutTailPath1 = new ArrayList<String>() {
            {
                add("uuid:root");
                add("uuid:monograph");
            }
        };

        List<String> cutTailPath2 = new ArrayList<String>() {
            {
                add("uuid:root");
            }
        };

        ObjectPidsPath path = new ObjectPidsPath(relsExtPath.toArray(new String[relsExtPath.size()]));

        ObjectPidsPath cutTail0 = path.cutTail(0);
        TestCase.assertEquals(Arrays.asList(cutTail0.getPathFromRootToLeaf()), cutTailPath0);

        ObjectPidsPath cutTail1 = path.cutTail(1);
        TestCase.assertEquals(Arrays.asList(cutTail1.getPathFromRootToLeaf()), cutTailPath1);

        ObjectPidsPath cutTail2 = path.cutTail(2);
        TestCase.assertEquals(Arrays.asList(cutTail2.getPathFromRootToLeaf()), cutTailPath2);
    }

}
