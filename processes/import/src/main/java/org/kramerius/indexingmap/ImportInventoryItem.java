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

import java.util.Collections;
import java.util.List;

import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Represents a single object discovered during the digital library data import process.
 * <p>
 * This class is a foundational element for building an in-memory graph or tree structure
 * of the imported data. Each instance corresponds to a single logical object (e.g., a book,
 * an issue, a page) and holds information about its identifier, its model, and its
 * position within the hierarchical structure of the imported data. It also tracks whether
 * the object is already present in the target index.
 */
public class ImportInventoryItem {

    public static final Logger LOGGER = Logger.getLogger(ImportInventoryItem.class.getName());

    /**
     * Defines the scheduling strategy for an item's indexation.
     * <p>
     * {@code TREE} indicates that the entire sub-tree rooted at this item should be indexed.
     * {@code OBJECT} indicates that only this specific item should be indexed.
     */
    public static enum TypeOfSchedule {
        TREE, OBJECT;
    }
    /** The unique persistent identifier (PID) of the object. */
    private String pid;
    /** The content model of the object (e.g., "monograph", "periodical", "page"). */
    private String model;
    /** The title of the object. Can be null for objects without a title. */
    private String title;
    /**
     * Indicates whether this object is already imported.
     */
    private boolean presentInProcessingIndex =false;

    private List<String> childrenPids = new ArrayList<>();
    /** The parent item in the in-memory tree structure. Null for root items. */
    private ImportInventoryItem parent;
    /** The list of children items in the in-memory tree structure. */
    private List<ImportInventoryItem> children;
    /** The indexation strategy for this item, as determined by the scheduler. */
    private TypeOfSchedule indexationPlanType;

    /**
     * Constructs a new ImportInventoryItem.
     *
     * @param pid                    The unique persistent identifier (PID) of the item.
     * @param model                  The content model of the item.
     * @param childrenPids           A list of PIDs of the item's direct children.
     * @param presentInProcessingIndex True if the item already exists in the processing index.
     */
    public ImportInventoryItem(String pid, String model, List<String> childrenPids, boolean presentInProcessingIndex) {
        this.pid = pid;
        this.model = model;
        this.presentInProcessingIndex = presentInProcessingIndex;
        this.children = new ArrayList<>();
        this.childrenPids = childrenPids;
    }

    /**
     * Constructs a new ImportInventoryItem with a title.
     *
     * @param pid                    The unique persistent identifier (PID) of the item.
     * @param model                  The content model of the item.
     * @param title                  The title of the item.
     * @param childrenPids           A list of PIDs of the item's direct children.
     * @param presentInProcessingIndex True if the item already exists in the processing index.
     */
    public ImportInventoryItem(String pid, String model, String title, List<String> childrenPids, boolean presentInProcessingIndex) {
        this.pid = pid;
        this.model = model;
        this.title = title;
        this.presentInProcessingIndex = presentInProcessingIndex;
        this.children = new ArrayList<>();
        this.childrenPids = childrenPids;
    }

    /**
     * Private constructor for internal use, typically for creating copies.
     *
     * @param pid   The unique persistent identifier (PID) of the item.
     * @param model The content model of the item.
     */
    private ImportInventoryItem(String pid, String model) {
        this.pid = pid;
        this.model = model;
    }

    /**
     * Private constructor for internal use, typically for creating copies with a title.
     *
     * @param pid   The unique persistent identifier (PID) of the item.
     * @param model The content model of the item.
     * @param title The title of the item.
     */
    private ImportInventoryItem(String pid, String model, String title) {
        this.pid = pid;
        this.model = model;
        this.title = title;
    }

    /**
     * Returns the unique persistent identifier (PID) of this item.
     *
     * @return The PID.
     */
    public String getPid() { return pid; }

    /**
     * Returns the content model of this item.
     *
     * @return The model.
     */
    public String getModel() { return model; }

    /**
     * Returns the title of this item.
     *
     * @return The title.
     */
    public String getTitle() { return title; }

    /**
     * Sets the title of this item.
     *
     * @param title The new title.
     */
    public void setTitle(String title) {
        this.title = title;
    }


    List<String> getChildrenPids() {
        return childrenPids;
    }

    /**
     * Checks if this item is present in the processing index.
     *
     * @return True if present, false otherwise.
     */
    public boolean isPresentInProcessingIndex() { return presentInProcessingIndex; }

    /**
     * Sets the flag indicating whether this item is present in the processing index.
     *
     * @param presentInProcessingIndex The new state.
     */
    public void setPresentInProcessingIndex(boolean presentInProcessingIndex) {
        this.presentInProcessingIndex = presentInProcessingIndex;
    }


    /**
     * Returns the list of child items linked in the in-memory tree.
     *
     * @return A list of child {@link ImportInventoryItem} objects.
     */
    public List<ImportInventoryItem> getChildren() { return children; }

    /**
     * Sets the parent of this item.
     *
     * @param parent The parent {@link ImportInventoryItem}.
     */
    public void setParent(ImportInventoryItem parent) {
        this.parent = parent;
    }

    /**
     * Returns the parent of this item in the tree.
     *
     * @return The parent {@link ImportInventoryItem}, or null if this is a root.
     */
    public ImportInventoryItem getParent() {
        return parent;
    }

    /**
     * Adds a child to the list of children.
     *
     * @param child The child {@link ImportInventoryItem} to add.
     */
    public void addChild(ImportInventoryItem child) {
        this.children.add(child);
    }

    /**
     * Removes a child from the list of children.
     *
     * @param child The child {@link ImportInventoryItem} to remove.
     */
    public void removeChild(ImportInventoryItem child) {
        this.children.remove(child);
    }

    /**
     * Returns the indexation scheduling type for this item.
     *
     * @return The {@link TypeOfSchedule}.
     */
    public TypeOfSchedule getIndexationPlanType() {
        return indexationPlanType;
    }

    /**
     * Sets the indexation scheduling type for this item.
     *
     * @param indexationPlanType The {@link TypeOfSchedule} to set.
     */
    public void setIndexationPlanType(TypeOfSchedule indexationPlanType) {
        this.indexationPlanType = indexationPlanType;
    }

    /**
     * Creates a new instance of ImportInventoryItem as a copy, setting the indexation plan type.
     * <p>
     * This method is useful for scheduling purposes, as it allows for an item to be scheduled
     * for indexation without modifying the original item in the main inventory.
     *
     * @param indexationPlan The {@link TypeOfSchedule} to apply to the new item.
     * @return A new {@link ImportInventoryItem} instance.
     */
    public ImportInventoryItem withIndexationPlan(TypeOfSchedule indexationPlan) {
        ImportInventoryItem newItem = new ImportInventoryItem(this.pid, this.model, this.title);
        newItem.setIndexationPlanType(indexationPlan);
        newItem.setPresentInProcessingIndex(this.presentInProcessingIndex);
        return newItem;
    }

    /**
     * Traverses up the tree to find and return the root ancestor of this item.
     *
     * @return The root {@link ImportInventoryItem} of the tree.
     */
    public ImportInventoryItem findRoot() {
        ImportInventoryItem current = this;
        while (current.getParent() != null) {
            current = current.getParent();
        }
        return current;
    }

    /**
     * Finds the highest ancestor that is not present in the processing index.
     * <p>
     * This method is used to identify the top-level item in a branch that needs
     * to be indexed due to new content.
     *
     * @return The highest {@link ImportInventoryItem} not in the index, or null if all ancestors are present.
     */
    public ImportInventoryItem findHighestNotImportedAncestor() {
        ImportInventoryItem highestNotIndexedAncestor = null;
        ImportInventoryItem current = this;
        while (current != null) {
            if (!current.isPresentInProcessingIndex()) {
                highestNotIndexedAncestor = current;
            }
            current = current.getParent();
        }
        return highestNotIndexedAncestor;
    }

    /**
     * Finds and returns a list of all ancestors of this item, ordered from the highest
     * ancestor (the root) down to the direct parent.
     *
     * @return A list of ancestor {@link ImportInventoryItem} objects.
     */
    public List<ImportInventoryItem> findAllAncestors() {
        List<ImportInventoryItem> ancestors = new ArrayList<>();
        ImportInventoryItem current = this.getParent();
        while (current != null) {
            ancestors.add(current);
            current = current.getParent();
        }
        Collections.reverse(ancestors);
        return ancestors;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ImportInventoryItem that = (ImportInventoryItem) o;
        return presentInProcessingIndex == that.presentInProcessingIndex && Objects.equals(pid, that.pid) && Objects.equals(model, that.model) && Objects.equals(childrenPids, that.childrenPids) && Objects.equals(parent, that.parent) && Objects.equals(children, that.children) && indexationPlanType == that.indexationPlanType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid, model, presentInProcessingIndex, childrenPids, parent, children, indexationPlanType);
    }



    /**
     * Prints a hierarchical representation of the tree structure from this item to the console.
     * This method is useful for visual debugging and verifying the tree structure.
     */
    public void printTree() {
        printTree(0);
    }

    /**
     * Helper recursive method for printing the tree with indentation.
     * @param depth The depth for indentation.
     */
    private void printTree(int depth) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("  ");
        }

        String prefix = this.isPresentInProcessingIndex() ? "+ " : "- ";
        String line = String.format("%s%s%s (%s) - present in index: %s",
                indent.toString(),
                prefix,
                this.getPid(),
                this.getModel(),
                this.isPresentInProcessingIndex());

        LOGGER.info(line);
        if (this.children != null) {
            for (ImportInventoryItem child : this.getChildren()) {
                if (child != null) {
                    child.printTree(depth + 1);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "IndexationPlanItem{" +
                "pid='" + pid + '\'' +
                ", model='" + model + '\'' +
                ", title='" + title + '\'' +
                ", presentInProcessingIndex=" + presentInProcessingIndex +
                ", childrenPids=" + childrenPids +
                ", parent=" + parent +
                ", children=" + children +
                ", indexationPlanType=" + indexationPlanType +
                '}';
    }
}