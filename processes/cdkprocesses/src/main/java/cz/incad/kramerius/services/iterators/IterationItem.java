package cz.incad.kramerius.services.iterators;

import java.util.Objects;

/**
 * Represents one interation item
 */
public class IterationItem {

    private String pid;
    private String source;

    public IterationItem(String pid, String source) {
        this.pid = pid;
        this.source = source;
    }

    /**
     * Gets pid of the object
     * @return
     */
    public String getPid() {
        return pid;
    }

    /**
     * Gets source from where pid comes from
     * @return
     */
    public String getSource() {
        return source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IterationItem)) return false;
        IterationItem that = (IterationItem) o;
        return Objects.equals(getPid(), that.getPid()) &&
                Objects.equals(getSource(), that.getSource());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPid(), getSource());
    }
}
