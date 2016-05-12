package warrick.easyble.values;

import java.nio.ByteBuffer;

import warrick.easyble.GattValue;

public class ByteValue extends GattValue {
    public ByteValue(byte value) {
        this.value = value;
    }

    public ByteValue() {

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

    public byte getValue() {
        return value;
    }

    public void setValue(byte value) {
        this.value = value;
    }
}
