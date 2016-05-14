package warrick.easyble.values;

import java.nio.ByteBuffer;

import warrick.easyble.GattValue;

public class Int64GattValue extends GattValue {
    public Int64GattValue(long value) {
        this.value = value;
    }
    public Int64GattValue() {

    }

    @Override
    public void deserialize(byte[] value) {
        // Create the value from the supplied bytes
        ByteBuffer byteBuffer = ByteBuffer.wrap(value);
        this.value = byteBuffer.getLong();
    }

    @Override
    public byte[] serialize() {
        // Convert the longeger to 4 bytes
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.putLong(value);
        return byteBuffer.array();
    }

    private long value = 0;

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
