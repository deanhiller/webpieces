package org.webpieces.data.impl;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

import org.webpieces.data.api.BufferPool;

public class SplitProxyWrapper extends SliceableDataWrapper  {

	private SliceableDataWrapper wrapper;
	private int offset;
	private int length;

	public SplitProxyWrapper(SliceableDataWrapper wrapper, int offset, int length) {
		this.wrapper = wrapper;
		this.offset = offset;
		this.length = length;
	}
	
	@Override
	public int getReadableSize() {
		return length;
	}

	@Override
	public byte readByteAt(int i) {
		return wrapper.readByteAt(offset + i);
	}

	@Override
	public String createStringFrom(int offset, int length, Charset charSet) {
		int endMarkOfProxyView = this.offset +  this.length;
		int endMarkOfRequest = this.offset + offset + length;
		if(offset > endMarkOfProxyView) {
			throw new IndexOutOfBoundsException("offset="+offset+" is outside the bounds of this view, endOfView="
						+endMarkOfProxyView+" view.offset="+this.offset+" view.length="+this.length);
		} else if(endMarkOfRequest > endMarkOfProxyView) {
			throw new IndexOutOfBoundsException("this.offset="+offset+" this.length="+length
					+" request goes outside of view.  view end="+endMarkOfProxyView+
					".  request.offset="+offset+" request.length="+length);
		}
		int newOffset = offset+this.offset;
		return wrapper.createStringFrom(newOffset, length, charSet);
	}

	@Override
	public byte[] createByteArray() {
		byte[] copy = new byte[length];
		for(int i = 0; i < copy.length; i++) {
			copy[i] = readByteAt(i);
		}
		return copy;
	}

	@Override
	public int getNumLayers() {
		return wrapper.getNumLayers()+1;
	}

	@Override
	public void addUnderlyingBuffersToList(List<ByteBuffer> buffers) {
		ByteBuffer buffer = wrapper.getSlicedBuffer(offset, length);
		buffers.add(buffer);
	}

	@Override
	public ByteBuffer getSlicedBuffer(int offset, int length) {
		//slice the slice so we have a view on top of a view...
		ByteBuffer buffer = wrapper.getSlicedBuffer(this.offset, this.length);
		
		int position = buffer.position();
		int limit = buffer.limit();
		buffer.position(offset);
		buffer.limit(offset+length);
		ByteBuffer theView = buffer.slice();
		buffer.position(position);
		buffer.limit(limit);
		return theView;
	}

	@Override
	protected void releaseImpl(BufferPool pool) {
		wrapper.releaseUnderlyingBuffers(pool);
	}
	
}
