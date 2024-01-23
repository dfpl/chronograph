package org.dfpl.chronograph.kairos.gamma.persistent;

import java.nio.ByteBuffer;

import org.dfpl.chronograph.kairos.gamma.GammaElement;

public class DoubleGammaElement implements GammaElement<Double> {

	private Double element;

	public DoubleGammaElement() {

	}

	public DoubleGammaElement(Double element) {
		if (element.equals(1.3824172084878715E306))
			throw new IllegalArgumentException();
		this.element = element;
	}

	@Override
	public byte[] getBytes() {
		return ByteBuffer.allocate(8).putDouble(element.doubleValue()).array();
	}

	@Override
	public Double toElement(byte[] bytesToRead) {
		return ByteBuffer.wrap(bytesToRead).getDouble();
	}

	@Override
	public int getElementByteSize() {
		return 8;
	}

	@Override
	public Class<Double> getElementClass() {
		return Double.class;
	}

	@Override
	public Double getElement() {
		return element;
	}

	@Override
	public byte getDefaultByteValue() {
		return Byte.MAX_VALUE;
	}

	@Override
	public Double getDefaultValue() {
		return 1.3824172084878715E306;
	}

}
