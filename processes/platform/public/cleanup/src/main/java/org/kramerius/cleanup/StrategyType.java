package org.kramerius.cleanup;

public enum StrategyType {
    /**
     * Used primarily for Permanent Space.
     * Deletes the oldest files when the total storage size exceeds a predefined threshold.
     */
    SIZE_LIMIT,

    /**
     * Used primarily for User Context Space.
     * Deletes documents that have exceeded their maximum allowed age (expiration period).
     */
    EXPIRATION,

    /**
     * A destructive strategy that marks all documents for deletion.
     * Useful for cache clearing or environment resets.
     */
    FORCE_EMPTY
}