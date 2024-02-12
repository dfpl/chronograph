package org.dfpl.chronograph.kairos.gamma.persistent;

import java.nio.ByteBuffer;

import org.dfpl.chronograph.kairos.gamma.GammaElement;

public class BooleanGammaElement implements GammaElement<Boolean> {

	private Boolean element;

	public BooleanGammaElement() {

	}

	public BooleanGammaElement(Boolean element) {
		this.element = element;
	}

	@Override
	public byte[] getBytes() {
		if (element)
			return new byte[] { 1 };
		else
			return new byte[] { 0 };
	}

	@Override
	public Boolean toElement(byte[] bytesToRead) {
		byte b = ByteBuffer.wrap(bytesToRead).get();
		if (b == 1)
			return true;
		else
			return false;
	}
	
	@Override
	public Object toJsonValue(byte[] bytesToRead) {
		byte b = ByteBuffer.wrap(bytesToRead).get();
		if (b == 1)
			return true;
		else
			return false;
	}

	@Override
	public int getElementByteSize() {
		return 1;
	}

	@Override
	public Class<Boolean> getElementClass() {
		return Boolean.class;
	}

	@Override
	public Boolean getElement() {
		return element;
	}

	@Override
	public byte getDefaultByteValue() {
		return 0;
	}

	@Override
	public Boolean getDefaultValue() {
		return false;
	}

	

}
