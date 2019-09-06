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
package cz.incad.kramerius.pdf.commands;

import org.w3c.dom.Element;

/**
 * Represents one IText command 
 */
public interface ITextCommand {

    /**
     * Load command 
     * @param elm XML element 
     * @param cmnds Commands container 
     * @throws InstantiationException Could not initialize command 
     * @throws IllegalAccessException Could not initalize commnad
     */
    public void load(Element elm, ITextCommands cmnds) throws InstantiationException, IllegalAccessException;

    public ITextCommands getRoot();

    /**
     * Returns parent command
     * @return parent command
     */
    public ITextCommand getParent();

    /**
     * Sets parent command
     * @param parent parent command
     */
    public void setParent(ITextCommand parent);
    
    /**
     * Process command
     * @param procsListener Listener for receiving processing informations
     */
    public void process(ITextCommandProcessListener procsListener);
}
