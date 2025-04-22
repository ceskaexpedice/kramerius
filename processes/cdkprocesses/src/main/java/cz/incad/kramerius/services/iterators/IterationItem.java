package cz.incad.kramerius.services.iterators;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a single item processed during an iteration.
 * <p>
 * Each iteration item contains a persistent identifier and information
 * about the source from which it originates. This class is used in conjunction
 * with {@link ProcessIterationCallback} to represent individual items retrieved
 * during the iteration process.
 * </p>
 */
public class IterationItem {

    /** Iteration idetifier; pid or compositeId. */
    private String id;

    /** Source from which the identifier originates. */
    private String source;

    /** Additional properties got during iteration */
    private Map<String, Object> doc;


    public IterationItem(String id, String source) {
        this.id = id;
        this.source = source;
        this.doc = new HashMap<>(); // no model; no conflict check
    }

    public IterationItem(String id, String source, Map<String, Object> doc) {
        this.id = id;
        this.source = source;
        this.doc = doc;
    }

    /**
     * Gets the persistent identifier (PID) of the object.
     *
     * @return The PID of the iteration item.
     */
    public String getId() {
        return id;
    }


    public boolean compositeIdUsed() {
        return this.id.contains("!");
    }

    public String getPid() {
        return this.compositeIdUsed() ?
                this.id.replaceFirst(".*!", "") :
                this.id;
    }


    /**
     * Gets the source from which the PID originates.
     *
     * @return The source of the iteration item.
     */
    public String getSource() {
        return source;
    }


    public Map<String, Object> getDoc() {
        return doc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IterationItem)) return false;
        IterationItem that = (IterationItem) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getSource(), that.getSource());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getSource());
    }

    @Override
    public String toString() {
        return "IterationItem [pid=" + id + ", source=" + source + "]";
    }
}
