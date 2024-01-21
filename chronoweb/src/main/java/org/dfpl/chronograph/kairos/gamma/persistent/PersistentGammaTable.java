package org.dfpl.chronograph.kairos.gamma.persistent;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

import org.dfpl.chronograph.kairos.gamma.Gamma;
import org.dfpl.chronograph.kairos.gamma.GammaElement;
import org.dfpl.chronograph.kairos.gamma.GammaTable;

public class PersistentGammaTable<K, E> implements GammaTable<K, E> {

	private RandomAccessFile gammaTable;
	private HashMap<K, Integer> idToIdx = new HashMap<K, Integer>();
	private ArrayList<K> idList = new ArrayList<K>();
	@SuppressWarnings("unused")
	private HashMap<Integer, K> idxToId = new HashMap<Integer, K>();
	private int cnt = 0;
	private int elementByteSize;
	private GammaElement<E> gammaElementConverter;
	private int initialCapacity;
	private Object gammaLock = new Object();
	private String fileName;
	private Class<? extends GammaElement<E>> gammaElementClass;

	public PersistentGammaTable(String fileName, Class<? extends GammaElement<E>> gammaElementClass)
			throws FileNotFoundException {
		gammaTable = new RandomAccessFile(fileName, "rws");
		try {
			this.gammaElementConverter = gammaElementClass.getConstructor().newInstance();
			this.elementByteSize = gammaElementConverter.getElementByteSize();
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		this.initialCapacity = 4;
		this.fileName = fileName;
		this.gammaElementClass = gammaElementClass;
	}

	private void expand() {
		synchronized (gammaLock) {
			fileName += "-";
			try {
				RandomAccessFile newGammaTable = new RandomAccessFile(fileName, "rws");
				for (int i = 0; i < cnt; i++) {
					for (int j = 0; j < cnt; j++) {
						E element = get(i, j);
						set(newGammaTable, i, j, element);
					}
				}
				gammaTable = newGammaTable;
				initialCapacity *= 2;
			} catch (FileNotFoundException e) {
			}
		}
	}

	private int getID(K id) {
		Integer idx = idToIdx.get(id);
		if (idx != null)
			return idx;
		else {
			if (cnt + 1 > initialCapacity) {
				expand();
			}
			idList.add(id);
			idToIdx.put(id, cnt++);
			return cnt - 1;

		}
	}

	private long getSeekPos(int from, int to) {
		return from * initialCapacity * elementByteSize + to * elementByteSize;
	}

	private long getSeekPos(int from, int to, int initialCapacity) {
		return from * initialCapacity * elementByteSize + to * elementByteSize;
	}

	@Override
	public synchronized void set(K from, K to, GammaElement<E> element) {
		int fromIdx = getID(from);
		int toIdx = getID(to);
		long s = getSeekPos(fromIdx, toIdx);
		try {
			gammaTable.seek(s);
			gammaTable.write(element.getBytes());
		} catch (Exception e) {

		}
	}

	private void set(RandomAccessFile raf, int from, int to, E element) {
		long s = getSeekPos(from, to, initialCapacity * 2);
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
		Integer fromIdx = idToIdx.get(from);
		Integer toIdx = idToIdx.get(to);
		if (fromIdx == null || toIdx == null)
			return null;
		long s = getSeekPos(fromIdx, toIdx);
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
	public Gamma<K, GammaElement<E>> getGamma(K from) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setGamma(K from, Gamma<K, GammaElement<E>> gamma) {
		// TODO Auto-generated method stub

	}

}
