package warrick.easyble;

/**
 * Represents a Gatt characteristic value that can be read and written to a service characteristic
 */
public abstract class GattValue {
    /**
     * This should be overriden by implementations of this class to deserialize their internal value
     * from a byte array
     * @param value
     */
    public void deserialize(byte[] value) {
        // STUB implementation
    }

    /**
     * This should be overidden in implementations to provide the serialized value
     * to be written to a gatt characteristic
     * @return
     */
    public byte[] serialize() {
        return new byte[] {};
    }
}