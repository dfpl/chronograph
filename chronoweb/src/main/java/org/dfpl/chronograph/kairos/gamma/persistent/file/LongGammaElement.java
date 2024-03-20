package org.dfpl.chronograph.kairos.gamma.persistent.file;

import java.nio.ByteBuffer;

import org.dfpl.chronograph.kairos.gamma.GammaElement;

public class LongGammaElement implements GammaElement<Long> {

	public static final Long DEFAULT_VALUE = 9187201950435737471L;
	private Long element;

	public LongGammaElement() {

	}

	public LongGammaElement(Long element) {
		if (element.equals(DEFAULT_VALUE))
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
		return DEFAULT_VALUE;
	}

}
