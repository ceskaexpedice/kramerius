package ca.thoughtwire.concurrent;

/**
 * Interface to act on members joining and leaving the grid.
 *
 * @author vanessa.williams
 */
public interface GridMembershipListener {

    public void memberAdded(String uuid);

    public void memberRemoved(final String uuid);

}
