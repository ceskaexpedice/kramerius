package cz.inovatika.kramerius.services.iterators;

/**
 * Callback interface for handling the completion of an iteration process.
 * <p>
 * Implementations of this interface define actions to be executed once the iteration
 * process has fully completed. This can include cleanup operations, logging,
 * or triggering further processing steps.
 * </p>
 */
public interface ProcessIterationEndCallback {

    /**
     * Called when the iteration process has completed.
     * <p>
     * This method is invoked after all items have been processed, signaling the end of the iteration.
     * Implementations can perform any necessary finalization or cleanup tasks here.
     * </p>
     */
    public void end();
}
