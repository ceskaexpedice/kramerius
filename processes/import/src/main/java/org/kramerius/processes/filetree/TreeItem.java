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
package org.kramerius.processes.filetree;

import java.util.ArrayList;
import java.util.List;


/**
 * HTML tree model for string template 
 * @author pavels
 */
public class TreeItem {
    
    private List<TreeItem> children = new ArrayList<TreeItem>();
    private String itemName;
    private String id;
    

    public TreeItem(String id, String itemName) {
        super();
        this.itemName = itemName;
        this.id = id;
    }

    public void addItem(TreeItem item) {
        children.add(item);
    }
    
    public void removeItem(TreeItem item) {
        children.remove(item);
    }
    
    public List<TreeItem> getChildren() {
        return children;
    }

    public String getItemName() {
        return itemName;
    }

    public String getId() {
        return id;
    }
    
    public boolean isLeaf() {
        return this.children.isEmpty(); 
    }

}
