package org.dfpl.chronograph.kairos.gamma.persistent.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.NotDirectoryException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.dfpl.chronograph.kairos.gamma.Gamma;
import org.dfpl.chronograph.kairos.gamma.GammaElement;
import org.dfpl.chronograph.kairos.gamma.GammaTable;

public class FixedSizedGammaTable<K, E> implements GammaTable<K, E> {

	private HashMap<Integer, RandomAccessFile> gammaMap;
	HashMap<K, Integer> idToIdx = new HashMap<>();
	ArrayList<K> idList = new ArrayList<K>();
	int cnt = 0;
	int elementByteSize;
	GammaElement<E> gammaElementConverter;
	private File directory;
	private WriteLock gammaWriteLock;
	private ReadLock gammaReadLock;
	private int capacity;
	private int expandFactor;

	public FixedSizedGammaTable(String directoryName, Class<? extends GammaElement<E>> gammaElementClass)
			throws FileNotFoundException, NotDirectoryException {
		this(directoryName, gammaElementClass, 4, 2);
	}

	public FixedSizedGammaTable(String directoryName, Class<? extends GammaElement<E>> gammaElementClass,
			int initialCapacity, int expandFactor) throws NotDirectoryException {
		this.gammaMap = new HashMap<>();
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

	public int getID(K id) {
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
	public void addSource(K source, GammaElement<E> element) {
		gammaWriteLock.lock();
		int fromIdx = getID(source);
		RandomAccessFile gamma = gammaMap.get(fromIdx);
		if (gamma != null) {
			gammaWriteLock.unlock();
			return;
		}

		try {
			gamma = new RandomAccessFile(directory.getAbsolutePath() + "\\" + fromIdx, "rws");
			byte[] fill = new byte[capacity * elementByteSize];
			Arrays.fill(fill, gammaElementConverter.getDefaultByteValue());
			gamma.write(fill);
			gamma.seek(fromIdx * elementByteSize);
			gamma.write(element.getBytes());
			gammaMap.put(fromIdx, gamma);
		} catch (Exception e) {

		}
		gammaWriteLock.unlock();
	}

	@Override
	public void set(K from, K to, GammaElement<E> element) {

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

	public Object getJsonValue(long pos, RandomAccessFile gamma) throws IOException {
		gamma.seek(pos);
		byte[] bytesToRead = new byte[elementByteSize];
		gamma.read(bytesToRead);
		Object e = gammaElementConverter.toJsonValue(bytesToRead);
		return e;
	}

	public File getDirectory() {
		return directory;
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
		return new FixedSizedGamma<>(this, gammaMap.get(idx));
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
	public void update(K check, Predicate<E> testCheck, K update, GammaElement<E> newValue,
			BiPredicate<E, E> testUpdate) {

		gammaWriteLock.lock();
		int checkIdx = getID(check);
		int updateIdx = getID(update);
		long checkPos = getSeekPos(checkIdx);
		long updatePos = getSeekPos(updateIdx);
		gammaMap.values().forEach(gamma -> {
			try {
				E checkValue = getElement(checkPos, gamma);

				if (!testCheck.test(checkValue))
					return;

				if (testUpdate.test(getElement(updatePos, gamma), newValue.getElement())) {
					setElement(updatePos, gamma, newValue);
				}
			} catch (IOException e) {

				e.printStackTrace();
			}
		});
		gammaWriteLock.unlock();
	}

	public void update(K check, Predicate<E> testCheck, E newCheckValue, Map<K, GammaElement<E>> updates,
			BiPredicate<E, E> testUpdate) {

		gammaWriteLock.lock();
		int checkIdx = getID(check);
		long checkPos = getSeekPos(checkIdx);
		gammaMap.values().forEach(gamma -> {
			try {
				E checkValue = getElement(checkPos, gamma);

				if (!testCheck.test(checkValue))
					return;

				if (checkValue == null || testUpdate.test(checkValue, newCheckValue))
					return;

				for (Map.Entry<K, GammaElement<E>> update : updates.entrySet()) {
					int updateIdx = getID(update.getKey());
					long updatePos = getSeekPos(updateIdx);
					if (testUpdate.test(getElement(updatePos, gamma), update.getValue().getElement())) {
						setElement(updatePos, gamma, update.getValue());
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		gammaWriteLock.unlock();
	}

	@Override
	public void update(Set<K> sources, K check, Predicate<E> testCheck, K update, GammaElement<E> newValue,
			BiPredicate<E, E> testUpdate) {

		gammaWriteLock.lock();
		int checkIdx = getID(check);
		int updateIdx = getID(update);
		long checkPos = getSeekPos(checkIdx);
		long updatePos = getSeekPos(updateIdx);

		Set<Integer> sourceIdxes = sources.parallelStream().map(s -> {
			return idToIdx.get(s);
		}).filter(s -> s != null).collect(Collectors.toSet());

		gammaMap.entrySet().parallelStream().map(entry -> {
			Integer key = entry.getKey();
			if (sourceIdxes.contains(key)) {
				return entry.getValue();
			} else
				return null;
		}).filter(g -> g != null).forEach(gamma -> {
			try {
				E checkValue = getElement(checkPos, gamma);
				if (!testCheck.test(checkValue))
					return;

				if (testUpdate.test(getElement(updatePos, gamma), newValue.getElement())) {
					setElement(updatePos, gamma, newValue);
				}
			} catch (IOException e) {

				e.printStackTrace();
			}
		});

		gammaMap.values().forEach(gamma -> {
			try {
				E checkValue = getElement(checkPos, gamma);
				if (!testCheck.test(checkValue))
					return;

				if (testUpdate.test(getElement(updatePos, gamma), newValue.getElement())) {
					setElement(updatePos, gamma, newValue);
				}
			} catch (IOException e) {

				e.printStackTrace();
			}
		});
		gammaWriteLock.unlock();
	}

	@Override
	public Set<K> getSources() {
		return gammaMap.keySet().parallelStream().map(idx -> {
			return idList.get(idx);
		}).collect(Collectors.toSet());
	}

	@Override
	public void print() {
		for (K s : getSources()) {
			System.out.println(s + " -> " + getGamma(s).toMap(true));
		}
	}

	@Override
	public void invalidate(K from, K to) {
		gammaWriteLock.lock();
		int fromIdx = getID(from);
		int toIdx = getID(to);
		long toPos = getSeekPos(toIdx);
		RandomAccessFile gamma = gammaMap.get(fromIdx);

		byte[] fill = new byte[elementByteSize];
		Arrays.fill(fill, gammaElementConverter.getDefaultByteValue());
		try {
			gamma.seek(toPos);
			gamma.write(fill);
		} catch (IOException e) {
			e.printStackTrace();
		}
		gammaWriteLock.unlock();
	}

	@Override
	public void append(K check, Predicate<E> testCheck, K update, GammaElement<E> newValue,
			BiPredicate<E, E> testUpdate) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void append(Set<K> sources, K check, Predicate<E> testCheck, K update, GammaElement<E> newValue,
			BiPredicate<E, E> testUpdate) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		for (K s : getSources()) {
			stringBuilder.append(String.format("%s -> %s", s, getGamma(s).toMap(true)));
		}
		return stringBuilder.toString();
	}
}
