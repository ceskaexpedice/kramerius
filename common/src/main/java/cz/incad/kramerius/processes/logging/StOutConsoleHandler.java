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
package cz.incad.kramerius.processes.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

/**
 * Redirect default consoleHandler to <code>System.out.println</code>
 * @see ConsoleHandler
 */
public class StOutConsoleHandler extends StreamHandler  {

    private void configure() {
    setLevel(Level.ALL);
    setFilter(null);
    setFormatter(new SimpleFormatter());
    }

    
    public StOutConsoleHandler() {
        super();
        configure();
        setOutputStream(System.out);
    }

    @Override
    public void publish(LogRecord record) {
        super.publish(record);
        this.flush();
    }

    
}
