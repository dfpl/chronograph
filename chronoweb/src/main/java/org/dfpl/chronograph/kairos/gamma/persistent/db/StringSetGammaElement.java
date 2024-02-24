package org.dfpl.chronograph.kairos.gamma.persistent.db;

import java.util.HashSet;
import java.util.Set;

import org.dfpl.chronograph.kairos.gamma.GammaElement;

public class StringSetGammaElement implements GammaElement<Set<String>> {

	private Set<String> element;

	public StringSetGammaElement(String source) {
		this.element = new HashSet<String>();
		this.element.add(source);
	}

	@Override
	public Set<String> getElement() {
		return element;
	}

	@Override
	public byte[] getBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> toElement(byte[] bytesToRead) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object toJsonValue(byte[] bytesToRead) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getElementByteSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Class<Set<String>> getElementClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte getDefaultByteValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Set<String> getDefaultValue() {
		// TODO Auto-generated method stub
		return null;
	}

}
