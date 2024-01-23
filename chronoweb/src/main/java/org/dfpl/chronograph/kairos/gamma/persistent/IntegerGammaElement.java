package org.dfpl.chronograph.kairos.gamma.persistent;

import java.nio.ByteBuffer;

import org.dfpl.chronograph.kairos.gamma.GammaElement;

public class IntegerGammaElement implements GammaElement<Integer> {

	private Integer element;

	public IntegerGammaElement() {

	}

	public IntegerGammaElement(Integer element) {
		if (element.equals(2139062143))
			throw new IllegalArgumentException();
		this.element = element;
	}

	@Override
	public byte[] getBytes() {
		return ByteBuffer.allocate(4).putInt(element.intValue()).array();
	}

	@Override
	public Integer toElement(byte[] bytesToRead) {
		return ByteBuffer.wrap(bytesToRead).getInt();
	}

	@Override
	public int getElementByteSize() {
		return 4;
	}

	@Override
	public Class<Integer> getElementClass() {
		return Integer.class;
	}

	@Override
	public Integer getElement() {
		return element;
	}

	@Override
	public byte getDefaultByteValue() {
		return Byte.MAX_VALUE;
	}

	@Override
	public Integer getDefaultValue() {
		return 2139062143;
	}

}
