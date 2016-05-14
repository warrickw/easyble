package warrick.easyble.values;

import java.nio.ByteBuffer;

import warrick.easyble.GattValue;

public class Int32GattValue extends GattValue {
    public Int32GattValue(int value) {
        this.value = value;
    }
    public Int32GattValue() {

    }

    @Override
    public void deserialize(byte[] value) {
        // Create the value from the supplied bytes
        ByteBuffer byteBuffer = ByteBuffer.wrap(value);
        this.value = byteBuffer.getInt();
    }

    @Override
    public byte[] serialize() {
        // Convert the integer to 4 bytes
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(value);
        return byteBuffer.array();
    }

    private int value = 0;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
