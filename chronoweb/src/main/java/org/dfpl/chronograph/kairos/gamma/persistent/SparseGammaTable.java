package org.dfpl.chronograph.kairos.gamma.persistent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.function.BiPredicate;

import org.dfpl.chronograph.kairos.gamma.Gamma;
import org.dfpl.chronograph.kairos.gamma.GammaElement;
import org.dfpl.chronograph.kairos.gamma.GammaTable;

public class SparseGammaTable<K, E> implements GammaTable<K, E> {

	private HashMap<Integer, RandomAccessFile> gammaMap;
	HashMap<K, Integer> idToIdx = new HashMap<K, Integer>();
	private ArrayList<K> idList = new ArrayList<K>();
	int cnt = 0;
	int elementByteSize;
	GammaElement<E> gammaElementConverter;
	private File directory;
	private WriteLock gammaWriteLock;
	private ReadLock gammaReadLock;
	private int capacity;
	private int expandFactor;

	public SparseGammaTable(String directoryName, Class<? extends GammaElement<E>> gammaElementClass)
			throws FileNotFoundException, NotDirectoryException {
		this(directoryName, gammaElementClass, 4, 2);
	}

	public SparseGammaTable(String directoryName, Class<? extends GammaElement<E>> gammaElementClass,
			int initialCapacity, int expandFactor) throws FileNotFoundException, NotDirectoryException {
		gammaMap = new HashMap<Integer, RandomAccessFile>();
		try {
			this.gammaElementConverter = gammaElementClass.getConstructor().newInstance();
			this.elementByteSize = gammaElementConverter.getElementByteSize();
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		this.directory = new File(directoryName);
		if (!this.directory.isDirectory())
			throw new NotDirectoryException(directoryName);
		ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		gammaWriteLock = lock.writeLock();
		gammaReadLock = lock.readLock();
		this.capacity = initialCapacity;
		this.expandFactor = expandFactor;
	}

	private void expand() {
		gammaMap.values().forEach(gamma -> {
			try {
				gamma.seek(getSeekPos(capacity));
				byte[] fill = new byte[capacity * elementByteSize * (expandFactor - 1)];
				Arrays.fill(fill, gammaElementConverter.getDefaultByteValue());
				gamma.write(fill);
			} catch (IOException e) {
				e.printStackTrace();
			}

		});
		capacity *= expandFactor;
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

	@Override
	public synchronized void set(K from, K to, GammaElement<E> element) {

		gammaWriteLock.lock();
		int fromIdx = getID(from);
		int toIdx = getID(to);

		RandomAccessFile gamma = gammaMap.get(fromIdx);
		try {
			if (gamma == null) {
				gamma = new RandomAccessFile(directory.getAbsolutePath() + "\\" + fromIdx, "rws");
				byte[] fill = new byte[capacity * elementByteSize];
				Arrays.fill(fill, gammaElementConverter.getDefaultByteValue());
				gamma.write(fill);
				gammaMap.put(fromIdx, gamma);
			}
			long s = getSeekPos(toIdx);
			setElement(s, gamma, element);
		} catch (Exception e) {

		}
		gammaWriteLock.unlock();
	}

	long getSeekPos(int to) {
		return to * elementByteSize;
	}

	void setElement(long pos, RandomAccessFile gamma, GammaElement<E> element) throws IOException {
		gamma.seek(pos);
		gamma.write(element.getBytes());
	}

	public E getElement(long pos, RandomAccessFile gamma) throws IOException {
		gamma.seek(pos);
		byte[] bytesToRead = new byte[elementByteSize];
		gamma.read(bytesToRead);
		E e = gammaElementConverter.toElement(bytesToRead);
		return e;
	}

	@Override
	public E get(K from, K to) {

		gammaReadLock.lock();

		Integer fromIdx = idToIdx.get(from);
		Integer toIdx = idToIdx.get(to);
		if (fromIdx == null || toIdx == null)
			return null;

		RandomAccessFile gamma = gammaMap.get(fromIdx);
		if (gamma == null)
			return null;
		long s = getSeekPos(toIdx);
		try {
			E result = getElement(s, gamma);
			gammaReadLock.unlock();
			return result;
		} catch (Exception e) {
			gammaReadLock.unlock();
			return null;
		}

	}

	@Override
	public Gamma<K, E> getGamma(K from) {
		Integer idx = idToIdx.get(from);
		if (idx == null)
			return null;
		return new SparseGamma<K, E>(this, gammaMap.get(idx));
	}

	@Override
	public void clear() {
		String directoryName = directory.getAbsolutePath();
		gammaMap.values().forEach(raf -> {
			try {
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		for (File f : new File(directoryName).listFiles()) {
			f.delete();
		}
	}

	@Override
	public void setIfExists(K ifExists, K set, GammaElement<E> newElement, BiPredicate<E, E> setNew) {
		Integer ifExistsIdx = idToIdx.get(ifExists);
		Integer setIdx = idToIdx.get(set);
		long ifExistsPos = getSeekPos(ifExistsIdx);
		long setPos = getSeekPos(setIdx);
		if (ifExistsIdx == null || setIdx == null) {
			throw new NullPointerException();
		}
		gammaMap.values().forEach(gamma -> {
			try {
				E exists = getElement(ifExistsPos, gamma);
				if (!exists.equals(gammaElementConverter.getDefaultValue())) {
					if (setNew.test(getElement(setPos, gamma), newElement.getElement())) {
						setElement(setPos, gamma, newElement);
					}
				}
			} catch (IOException e) {

				e.printStackTrace();
			}
		});
	}

}
