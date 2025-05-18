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
package cz.incad.kramerius.security.licenses;

import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.TransformerException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Enum representing types of runtime licenses.
 * A runtime license dynamically determines access to documents based on their metadata.
 * Each enum constant defines its own matching logic through the {@link #accept(Document)} method.
 */
public enum RuntimeLicenseType {


    /**
     * License applicable to all documents in the repository
     */
    ALL_DOCUMENTS {
        @Override
        public boolean accept(Document doc) {
            return true;
        }
    },

    /**
     * License applicable only to specific page types of monographs.
     * <p>
     * This includes covers and front-matter pages such as:
     * FrontCover, TableOfContents, FrontJacket, TitlePage, and jacket.
     * <p>
     * It excludes periodical models by checking the {@code root.model} field.
     */
    COVER_AND_CONTENT_MONOGRAPH_PAGE {

        private static final String  PERIODICAL_MODEL = "periodical";

        private static final List<String> ALLOWED_PAGE_TYPES = Arrays.asList(
                "FrontCover", "TableOfContents", "FrontJacket", "TitlePage", "jacket"
        ).stream().map(String::toLowerCase).collect(Collectors.toList());

        @Override
        public boolean accept(Document doc) {

            // Check if the page type is one of the allowed types
            Element typeElm = XMLUtils.findElement(doc.getDocumentElement(), (elm) -> {
                String str = elm.getNodeName();
                boolean attr = elm.hasAttribute("name");

                if (str.equals("str") && attr && elm.getAttribute("name").trim().equals("page.type")) {
                    String pageType = elm.getTextContent().trim();
                    return ALLOWED_PAGE_TYPES.contains(pageType.toLowerCase());
                } else return false;
            });


            // Check if the root model is not a periodical
            Element rootElm = XMLUtils.findElement(doc.getDocumentElement(), (elm) -> {
                String str = elm.getNodeName();
                boolean attr = elm.hasAttribute("name");
                if (str.equals("str") && attr && elm.getAttribute("name").trim().equals("root.model")) {
                    String rootModel = elm.getTextContent();
                    return !rootModel.equals(PERIODICAL_MODEL);
                } else return false;
            });

            return typeElm != null && rootElm != null;
        }
    };


    /**
     * Checks whether the given document is accepted by this license type.
     *
     * @param doc the document to evaluate
     * @return {@code true} if the document is accepted by the runtime license type, {@code false} otherwise
     */
    public abstract boolean accept(Document doc);


    /**
     * Parses a {@code RuntimeLicenseType} from its string name.
     *
     * @param value the string representation of the runtime license type
     * @return an {@code Optional} containing the matching type if found, otherwise an empty Optional
     */
    public static Optional<RuntimeLicenseType> fromString(String value) {
        try {
            return Optional.of(RuntimeLicenseType.valueOf(value));
        } catch (IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }
}
