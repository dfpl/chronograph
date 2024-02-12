package org.dfpl.chronograph.kairos.gamma.persistent;

import java.nio.ByteBuffer;

import org.dfpl.chronograph.kairos.gamma.GammaElement;

public class LongGammaElement implements GammaElement<Long> {

	private Long element;

	public LongGammaElement() {

	}

	public LongGammaElement(Long element) {
		if (element.equals(9187201950435737471l))
			throw new IllegalArgumentException();
		this.element = element;
	}

	@Override
	public byte[] getBytes() {
		return ByteBuffer.allocate(16).putLong(element.longValue()).array();
	}

	@Override
	public Long toElement(byte[] bytesToRead) {
		return ByteBuffer.wrap(bytesToRead).getLong();
	}

	@Override
	public Object toJsonValue(byte[] bytesToRead) {
		return ByteBuffer.wrap(bytesToRead).getLong();
	}

	@Override
	public int getElementByteSize() {
		return 16;
	}

	@Override
	public Class<Long> getElementClass() {
		return Long.class;
	}

	@Override
	public Long getElement() {
		return element;
	}

	@Override
	public byte getDefaultByteValue() {
		return Byte.MAX_VALUE;
	}

	@Override
	public Long getDefaultValue() {
		return 9187201950435737471l;
	}

}
