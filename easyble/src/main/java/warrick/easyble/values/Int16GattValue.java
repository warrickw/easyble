package warrick.easyble.values;

import java.nio.ByteBuffer;

import warrick.easyble.GattValue;

public class Int16GattValue extends GattValue {
    public Int16GattValue(short value) {
        this.value = value;
    }
    public Int16GattValue() {

    }

    @Override
    public void deserialize(byte[] value) {
        // Create the value from the supplied bytes
        ByteBuffer byteBuffer = ByteBuffer.wrap(value);
        this.value = byteBuffer.getShort();
    }

    @Override
    public byte[] serialize() {
        // Convert the integer to 4 bytes
        ByteBuffer byteBuffer = ByteBuffer.allocate(2);
        byteBuffer.putShort(value);
        return byteBuffer.array();
    }

    private short value = 0;

    public int getValue() {
        return value;
    }

    public void setValue(short value) {
        this.value = value;
    }
}
