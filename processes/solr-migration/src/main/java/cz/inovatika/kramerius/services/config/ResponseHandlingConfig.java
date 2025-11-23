package cz.inovatika.kramerius.services.config;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Immutable configuration for handling external service responses, including 
 * rate limiting (delay) and error recovery (retries based on status codes).
 * Uses the Builder pattern for creation.
 */
public class ResponseHandlingConfig {

    /** Minimal delay in milliseconds between consecutive requests. Defaults to 0. */
    private final int delayMs;
    /** Maximum number of times a request should be retried on a recoverable error. Defaults to 0 (no retries). */
    private final int maxRetries;
    /** HTTP status codes (e.g., 503, 429) that trigger a retry attempt. */
    private final int[] retryStatusCodes;

    private ResponseHandlingConfig(Builder builder) {
        this.delayMs = builder.delayMs;
        this.maxRetries = builder.maxRetries;
        this.retryStatusCodes = builder.retryStatusCodes;
    }

    // --- Getters ---

    /**
     * Returns the configured delay between requests in milliseconds.
     * @return The delay in milliseconds.
     */
    public int getDelayMs() { return delayMs; }

    /**
     * Returns the maximum number of retry attempts.
     * @return The maximum number of retries.
     */
    public int getMaxRetries() { return maxRetries; }

    /**
     * Returns the list of HTTP status codes that trigger a retry.
     * @return An array of retry status codes.
     */
    public int[] getRetryStatusCodes() { return retryStatusCodes; }


    // --- BUILDER PATTERN ---
    
    public static class Builder {
        private int delayMs = 0;
        private int maxRetries = 0;
        private int[] retryStatusCodes = new int[0];

        // Private helper to parse comma-separated status codes
        private int[] parseStatusCodes(String codes) {
            if (codes == null || codes.trim().isEmpty()) return new int[0];
            return Arrays.stream(codes.split(","))
                         .map(String::trim)
                         .filter(s -> !s.isEmpty())
                         .mapToInt(Integer::parseInt)
                         .toArray();
        }


        public Builder delayMs(int delayMs) {
            this.delayMs = delayMs;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Sets retry status codes from a comma-separated string (e.g., "500, 503, 429").
         */
        public Builder retryStatusCodes(String codes) {
             this.retryStatusCodes = parseStatusCodes(codes);
             return this;
        }


        public ResponseHandlingConfig build() {
            // Optional: Add validation logic here if needed
            return new ResponseHandlingConfig(this);
        }
    }

    @Override
    public String toString() {
        return "ResponseHandlingConfig{" +
                "delayMs=" + delayMs +
                ", maxRetries=" + maxRetries +
                ", retryStatusCodes=" + Arrays.stream(retryStatusCodes).mapToObj(String::valueOf).collect(Collectors.joining(",")) +
                '}';
    }
}