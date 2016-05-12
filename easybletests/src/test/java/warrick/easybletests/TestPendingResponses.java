package warrick.easybletests;

import org.junit.Test;

import warrick.easyble.GattValue;
import warrick.easyble.PendingReadResult;
import warrick.easyble.PendingWriteResult;
import warrick.easyble.ReadResult;
import warrick.easyble.WriteResult;
import warrick.easyble.values.Int32Value;

import static org.junit.Assert.assertEquals;

public class TestPendingResponses {
    @Test
    public void TestReadResponseSuccess() {
        final byte[] didCall = new byte[] {0};
        PendingReadResult pendingReadResult = new PendingReadResult(null, new ReadResult<Int32Value>() {
            @Override
            public void success(Int32Value value) {
                didCall[0] = 1;
                assertEquals("Did not create value class correctly", 123456, value.getValue());
            }

            @Override
            public void failure() {

            }
        }, Int32Value.class);

        pendingReadResult.success(new Int32Value(123456).serialize());

        assertEquals("PendingReadResult did not call callback at all", didCall[0], 1);
    }

    @Test
    public void TestReadResponseFailure() {
        final byte[] didCall = new byte[] {0};
        PendingReadResult pendingReadResult = new PendingReadResult(null, new ReadResult<Int32Value>() {
            @Override
            public void success(Int32Value value) {
            }

            @Override
            public void failure() {
                didCall[0] = 1;

            }
        }, Int32Value.class);

        pendingReadResult.failure();

        assertEquals("PendingReadResult did not call callback at all", didCall[0], 1);
    }

    @Test
    public void TestWriteResponse() {
        final byte[] didCall = new byte[] {0};
        PendingWriteResult pendingWriteResult = new PendingWriteResult(null, new WriteResult() {
            @Override
            public void success() {
                didCall[0] = 1;
            }

            @Override
            public void failure() {

            }
        }, new byte[] {0});

        pendingWriteResult.success();

        assertEquals("PendingReadResult did not call callback at all", didCall[0], 1);
    }

    @Test
    public void TestWriteFailResponse() {
        final byte[] didCall = new byte[] {0};
        PendingWriteResult pendingWriteResult = new PendingWriteResult(null, new WriteResult() {
            @Override
            public void success() {

            }

            @Override
            public void failure() {
                didCall[0] = 1;

            }
        }, new byte[] {0});

        pendingWriteResult.failure();

        assertEquals("PendingReadResult did not call callback at all", didCall[0], 1);
    }
}
