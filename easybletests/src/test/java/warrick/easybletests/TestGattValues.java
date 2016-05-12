package warrick.easybletests;

import org.junit.Test;

import warrick.easyble.values.ByteValue;
import warrick.easyble.values.Int16Value;
import warrick.easyble.values.Int32Value;

import static org.junit.Assert.*;

public class TestGattValues {
    @Test
    public void Test_Int32() {
        // Make sure the integer serializes and deserializes correctly
        Int32Value value = new Int32Value(-23442345);
        value.deserialize(new Int32Value(123456).serialize());
        assertEquals("Deserialized value didn't match serialized value", 123456, value.getValue());
    }

    @Test
    public void Test_Int16() {
        // Make sure the integer serializes and deserializes correctly
        Int16Value value = new Int16Value((short)-135);
        value.deserialize(new Int16Value((short)1281).serialize());
        assertEquals("Deserialized value didn't match serialized value", 1281, value.getValue());
    }

    @Test
    public void Test_Byte() {
        // Make sure the integer serializes and deserializes correctly
        ByteValue value = new ByteValue((byte)-127);
        value.deserialize(new ByteValue((byte)123).serialize());
        assertEquals("Deserialized value didn't match serialized value", 123, value.getValue());
    }
}