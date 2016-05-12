package warrick.easyble;

/**
 * Defines a generic callack interface for reading a BLE attribute value
 * @param <T>
 */
public interface ReadResult<T extends GattValue> {
    /**
     * Supplies the read value to the caller
     * @param value
     */
    void success(T value);

    /**
     * There was an error reading the value
     */
    void failure();
}