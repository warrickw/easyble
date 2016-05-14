package warrick.easyble.values;

import java.nio.ByteBuffer;

import warrick.easyble.GattValue;

public class ByteGattValue extends GattValue {
    public ByteGattValue(byte value) {
        this.value = value;
    }

    public ByteGattValue() {

    }

    @Override
    public void deserialize(byte[] value) {
        this.value = value[0];
    }

    @Override
    public byte[] serialize() {
        return new byte[] {this.value};
    }

    private byte value = 0;

    /**
     * Gets the value in this gatt attribute
     * @return
     */
    public byte getValue() {
        return value;
    }

    /**
     * Returns the unsigned equaivelant of the byte value
     * @return
     */
    public int getUnsignedValue() {
        return value & 0xFF;
    }

    public void setValue(byte value) {
        this.value = value;
    }
}
