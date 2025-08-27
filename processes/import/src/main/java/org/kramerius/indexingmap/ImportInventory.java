/*
 * Copyright (C) 2025  Inovatika
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
package org.kramerius.indexingmap;

import java.util.*;

/**
 * A container for all items discovered during the data import process for a digital library.
 * <p>
 * This class serves as an in-memory representation of objects found in the import directory,
 * such as books, periodicals, or individual pages. Each item is a part of a hierarchical
 * tree structure. The plan is created by traversing the import directory and linking these
 * items together into a graph or tree.
 * <p>
 * This class collects all discovered objects in a single place. The actual indexation
 * process is then scheduled based on a specific strategy (e.g., {@link ScheduleStrategy}).
 */
public class ImportInventory {

    /**
     * List of items
     */
    private List<ImportInventoryItem> items = new ArrayList<>();

    /**
     * Adds a new item to the collection of discovered items.
     *
     * @param item The {@link ImportInventoryItem} to be added.
     */
    public void addIndexationPlanItem(ImportInventoryItem item) {
        this.items.add(item);
    }

    /**
     * Removes an item from the collection of discovered items.
     *
     * @param item The {@link ImportInventoryItem} to be removed.
     */
    public void removeIndexationPlanItem(ImportInventoryItem item) {
        this.items.remove(item);
    }

    /**
     * Returns a copy of the list of all discovered items in the inventory.
     * <p>
     * This method returns a new {@link ArrayList} containing all items, ensuring that
     * external modifications to the returned list do not affect the internal state
     * of this {@link ImportInventory} object.
     *
     * @return a new list containing all {@link ImportInventoryItem} objects.
     */
    public List<ImportInventoryItem> getIndexationPlanItems() {
        return new ArrayList<>(items);
    }

    /**
     * Checks if an item with a specific PID (Persistent Identifier) has already been
     * added to the inventory.
     * <p>
     * This method iterates through the list of items to determine if any item
     * has a matching PID, which helps in preventing duplicates during the import process.
     *
     * @param pid The PID (as a {@link String}) to be checked.
     * @return {@code true} if an item with the given PID is found, {@code false} otherwise.
     */
    public boolean isPidAlreadyPlanned(String pid) {
        for (ImportInventoryItem item : items) {
            if (item.getPid().equals(pid)) {
                return true;
            }
        }
        return false;
    }


    public void printInventory() {
        List<ImportInventoryItem> roots = new ArrayList<>();
        for (ImportInventoryItem item : this.getIndexationPlanItems()) {
            ImportInventoryItem root = item.findRoot();
            if (root != null) {
                if (!roots.contains(root)) {roots.add(root);}
            }
        }
        for (ImportInventoryItem root : roots) {
            root.printTree();
        }
    }
}
