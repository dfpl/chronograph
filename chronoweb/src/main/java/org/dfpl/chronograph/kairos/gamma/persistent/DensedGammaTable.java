package org.dfpl.chronograph.kairos.gamma.persistent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.function.BiPredicate;

import org.dfpl.chronograph.kairos.gamma.Gamma;
import org.dfpl.chronograph.kairos.gamma.GammaElement;
import org.dfpl.chronograph.kairos.gamma.GammaTable;

public class DensedGammaTable<K, E> implements GammaTable<K, E> {

	private RandomAccessFile gammaTable;
	private HashMap<K, Integer> idToIdx = new HashMap<K, Integer>();
	private ArrayList<K> idList = new ArrayList<K>();
	private int cnt = 0;
	private int elementByteSize;
	private GammaElement<E> gammaElementConverter;
	private int capacity;
	private WriteLock gammaWriteLock;
	private ReadLock gammaReadLock;
	private String fileName;
	private Class<? extends GammaElement<E>> gammaElementClass;
	private int expandFactor;

	public DensedGammaTable(String fileName, Class<? extends GammaElement<E>> gammaElementClass)
			throws FileNotFoundException {
		gammaTable = new RandomAccessFile(fileName, "rws");
		try {
			this.gammaElementConverter = gammaElementClass.getConstructor().newInstance();
			this.elementByteSize = gammaElementConverter.getElementByteSize();
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		this.capacity = 4;
		this.expandFactor = 2;
		this.fileName = fileName;
		this.gammaElementClass = gammaElementClass;
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		gammaWriteLock = lock.writeLock();
		gammaReadLock = lock.readLock();
	}

	public DensedGammaTable(String fileName, Class<? extends GammaElement<E>> gammaElementClass, int initialCapacity,
			int expandFactor) throws FileNotFoundException {
		gammaTable = new RandomAccessFile(fileName, "rws");
		try {
			this.gammaElementConverter = gammaElementClass.getConstructor().newInstance();
			this.elementByteSize = gammaElementConverter.getElementByteSize();
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		this.capacity = initialCapacity;
		this.expandFactor = expandFactor;
		this.fileName = fileName;
		this.gammaElementClass = gammaElementClass;
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		gammaWriteLock = lock.writeLock();
		gammaReadLock = lock.readLock();
	}

	private void expand() {
		try {
			RandomAccessFile newGammaTable = new RandomAccessFile(fileName + "-", "rws");
			for (int i = 0; i < cnt; i++) {
				for (int j = 0; j < cnt; j++) {
					E element = get(i, j);
					set(newGammaTable, i, j, element);
				}
			}
			gammaTable.close();
			new File(fileName).delete();
			fileName += "-";
			gammaTable = newGammaTable;
			capacity *= expandFactor;
		} catch (IOException e) {
		}
	}

	private int getID(K id) {
		Integer idx = idToIdx.get(id);
		if (idx != null)
			return idx;
		else {
			if (cnt + 1 > capacity) {
				expand();
			}
			idList.add(id);
			idToIdx.put(id, cnt++);
			return cnt - 1;

		}
	}

	private long getSeekPos(int from, int to) {
		return from * capacity * elementByteSize + to * elementByteSize;
	}

	private long getSeekPos(int from, int to, int initialCapacity) {
		return from * initialCapacity * elementByteSize + to * elementByteSize;
	}

	@Override
	public synchronized void set(K from, K to, GammaElement<E> element) {

		gammaWriteLock.lock();

		int fromIdx = getID(from);
		int toIdx = getID(to);
		long s = getSeekPos(fromIdx, toIdx);
		try {
			gammaTable.seek(s);
			gammaTable.write(element.getBytes());
		} catch (Exception e) {

		}

		gammaWriteLock.unlock();
	}

	private void set(RandomAccessFile raf, int from, int to, E element) {
		long s = getSeekPos(from, to, capacity * expandFactor);
		try {
			raf.seek(s);
			byte[] bytes = gammaElementClass.getConstructor(gammaElementConverter.getElementClass())
					.newInstance(element).getBytes();
			raf.write(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private E get(int from, int to) {
		long s = getSeekPos(from, to);
		try {
			gammaTable.seek(s);
			byte[] bytesToRead = new byte[elementByteSize];
			gammaTable.read(bytesToRead);
			return gammaElementConverter.toElement(bytesToRead);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public E get(K from, K to) {

		gammaReadLock.lock();

		Integer fromIdx = idToIdx.get(from);
		Integer toIdx = idToIdx.get(to);
		if (fromIdx == null || toIdx == null)
			return null;
		long s = getSeekPos(fromIdx, toIdx);
		try {
			gammaTable.seek(s);
			byte[] bytesToRead = new byte[elementByteSize];
			gammaTable.read(bytesToRead);
			E result = gammaElementConverter.toElement(bytesToRead);
			gammaReadLock.unlock();
			return result;
		} catch (Exception e) {
			gammaReadLock.unlock();
			return null;
		}

	}

	@Override
	public Gamma<K, E> getGamma(K from) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setGamma(K from, Gamma<K, GammaElement<E>> gamma) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		new File(fileName).delete();
	}

	@Override
	public void setIfExists(K ifExists, K newValue, GammaElement<E> element, BiPredicate<E, E> setNew) {
		// TODO Auto-generated method stub

	}

}
