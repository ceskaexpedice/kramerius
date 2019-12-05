/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also 
 * available online at http://fedora-commons.org/license/).
 */
package org.fcrepo.common.rdf;

//import org.jrdf.graph.AbstractURIReference;

import java.net.URI;

/**
 * A URIReference with convenient constructors.
 *
 * @author Chris Wilper
 */
public class SimpleURIReference
        {
    
    private static final long serialVersionUID = 1L;
    
    public SimpleURIReference(URI uri) {
        //super(uri);
    }
    
    public SimpleURIReference(URI uri, boolean validate) {
        //super(uri, validate);
    }
    
}
