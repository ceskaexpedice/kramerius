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
package cz.incad.kramerius.rest.oai.representativepage;

/**
 * Factory class for creating instances of {@link RepresentativePageFinder}.
 * <p>
 * This factory provides a way to obtain a default implementation of the
 * {@code RepresentativePageFinder} interface.
 */
public class RepresentativePageFinderFactory {

    /**
     * Creates and returns a new instance of a {@link RepresentativePageFinder}.
     *
     * @return A {@code RepresentativePageFinder} instance
     */
    public static RepresentativePageFinder create() {
        return new FirstPageFinder();
    }

}
