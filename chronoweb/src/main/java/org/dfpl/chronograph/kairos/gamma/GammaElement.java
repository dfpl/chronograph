package org.dfpl.chronograph.kairos.gamma;

public interface GammaElement<E> {

	public byte[] getBytes();

	public E toElement(byte[] bytesToRead);

	public Object toJsonValue(byte[] bytesToRead);

	public int getElementByteSize();

	public Class<E> getElementClass();

	public E getElement();

	public byte getDefaultByteValue();

	public E getDefaultValue();

}
