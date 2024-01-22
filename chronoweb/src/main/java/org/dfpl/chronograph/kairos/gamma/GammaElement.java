package org.dfpl.chronograph.kairos.gamma;

public interface GammaElement<E> {

	public byte[] getBytes();

	public E toElement(byte[] bytesToRead);

	public int getElementByteSize();

	public Class<E> getElementClass();

	public E getElement();
}
