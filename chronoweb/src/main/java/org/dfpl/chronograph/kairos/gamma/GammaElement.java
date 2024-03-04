package org.dfpl.chronograph.kairos.gamma;

/**
 * A {@code GammaElement} is the temporal information diffusion result
 * 
 * @param <E> the data type of this element
 */
public interface GammaElement<E> {

	/**
	 * Return this element as bytes
	 * 
	 * @return the byte representation of this element
	 */
	public byte[] getBytes();

	/**
	 * Convert the bytes to its Gamma Element representation
	 * 
	 * @param bytesToRead the bytes to convert
	 * @return the converted element
	 */
	public E toElement(byte[] bytesToRead);

	/**
	 * Convert the bytes to its JSON representation
	 * 
	 * @param bytesToRead the bytes to convert
	 * @return the converted JSON value
	 */
	public Object toJsonValue(byte[] bytesToRead);

	/**
	 * Return the byte size of the element
	 * 
	 * @return the byte size
	 */
	public int getElementByteSize();

	/**
	 * Return the {@code Class} of this element
	 * 
	 * @return the class type of this element
	 */
	public Class<E> getElementClass();

	/**
	 * Return the element
	 * 
	 * @return the element
	 */
	public E getElement();

	/**
	 * Return the default byte value
	 * 
	 * @return the default byte value
	 */
	public byte getDefaultByteValue();

	/**
	 * Return the default element value
	 * 
	 * @return the default value
	 */
	public E getDefaultValue();

}
