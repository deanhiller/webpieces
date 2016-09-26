package dto;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.data.impl.ByteBufferDataWrapper;
import org.webpieces.data.impl.ChainedDataWrapper;

import java.nio.ByteBuffer;
import java.util.List;

public abstract class Http2Frame {
	static DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

	//24 bits unsigned length
	public abstract Http2FrameType getFrameType(); //8bits
	//1bit reserved
	private int streamId; //31 bits unsigned

    public void setStreamId(int streamId) {
        // Clear the MSB because streamId can only be 31 bits
        this.streamId = streamId & 0x7FFFFFFF;
    }

	private DataWrapper getDataWrapper() {
		ByteBuffer header = ByteBuffer.allocate(9);
		DataWrapper payload = getPayloadDataWrapper();

		int length = payload.getReadableSize();
        header.put((byte) (length >> 16));
        header.putShort((short) length);

        header.put(getFrameTypeByte());
		header.put(getFlagsByte());
        // 1 bit reserved, streamId MSB is always 0, see setStreamId()
        header.putInt(streamId);
        header.flip();

        // The payload might be a chained datawrapper, and we can't stick a chained datawrapper on the end
        // of a non-chained datawrapper, so we wrap the new bytebufferdatawrapper into a chained datawrapper
        // here.
		return dataGen.chainDataWrappers(new ChainedDataWrapper(new ByteBufferDataWrapper(header)), payload);
	}

	// includes header length
	static public int peekLengthOfFrame(DataWrapper data) {
        ByteBuffer lengthBytes = ByteBuffer.wrap(data.readBytesAt(0, 3));
        int length = lengthBytes.getShort() << 8;
        length |= lengthBytes.get();
        return length + 9; // add 9 bytes for the header
    }

    // Ignores what's left over at the end of the datawrapper
	static public Http2Frame getDataWrapper(DataWrapper data) {
        ByteBuffer headerByteBuffer = ByteBuffer.wrap(data.readBytesAt(0, 9));
        int length = headerByteBuffer.getShort() << 8;
        length |= headerByteBuffer.get();

        byte frameTypeId = headerByteBuffer.get();

        Class<? extends Http2Frame> frameClass = Http2FrameType.fromId(frameTypeId).getFrameClass();
        try {
            Http2Frame ret = frameClass.newInstance();

            byte flags = headerByteBuffer.get();
            ret.setFlags(flags);

            // Ignore the reserved bit
            int streamId = headerByteBuffer.getInt();
            ret.setStreamId(streamId);

            if(length > 0) {
                List<? extends DataWrapper> splitWrappers = dataGen.split(data, 9);
                DataWrapper payloadPlusMore = splitWrappers.get(1);
                List<? extends DataWrapper> split = dataGen.split(payloadPlusMore, length);
                ret.setPayload(split.get(0));
            }
            return ret;

        } catch (InstantiationException | IllegalAccessException e) {
            // TODO: deal with exception
            return null; // should reraise in some fashion
        }
    }

    // The payload doesn't have any extra data past the end of the frame by now
    protected abstract void setPayload(DataWrapper payload);

	public byte[] getBytes() {
		return getDataWrapper().createByteArray();
	}

	private byte getFrameTypeByte() {
		return getFrameType().getId();
	}

	protected abstract byte getFlagsByte();
	protected abstract void setFlags(byte flag);

	abstract protected DataWrapper getPayloadDataWrapper();

	DataWrapper pad(byte[] padding, DataWrapper data) {
		byte[] length = { (byte) padding.length };
		DataWrapper lengthDW = dataGen.wrapByteArray(length);
		DataWrapper paddingDW = dataGen.wrapByteArray(padding);
		return dataGen.chainDataWrappers(dataGen.chainDataWrappers(lengthDW, data), paddingDW);
	}
}
