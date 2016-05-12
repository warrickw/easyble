package warrick.easyble;

/**
 * Defines a callback to notify about the result of a write operation
 */
public interface WriteResult {
    /**
     * The write operation was succesfull
     */
    void success();

    /**
     * The write operation didn't succeed
     */
    void failure();
}
