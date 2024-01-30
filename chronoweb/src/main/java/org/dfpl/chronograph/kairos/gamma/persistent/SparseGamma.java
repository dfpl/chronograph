package org.dfpl.chronograph.kairos.gamma.persistent;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import org.dfpl.chronograph.kairos.gamma.Gamma;
import org.dfpl.chronograph.kairos.gamma.GammaElement;

public class SparseGamma<K, E> implements Gamma<K, E> {

	private SparseGammaTable<K, E> gammaTable;
	private RandomAccessFile gamma;

	public SparseGamma(SparseGammaTable<K, E> gammaTable, RandomAccessFile gamma) {
		this.gammaTable = gammaTable;
		this.gamma = gamma;
	}

	@Override
	public E getElement(K to) {
		Integer toIdx = gammaTable.idToIdx.get(to);
		if (toIdx == null)
			return null;
		try {
			return gammaTable.getElement(gammaTable.getSeekPos(toIdx), gamma);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void setElement(K to, GammaElement<E> element) {
		Integer toIdx = gammaTable.idToIdx.get(to);
		if (toIdx == null)
			throw new NullPointerException();
		try {
			gammaTable.setElement(gammaTable.getSeekPos(toIdx), gamma, element);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	@Override
	public Map<K, E> toMap(boolean setDefaultToNull) {
		Map<K, E> result = new HashMap<K, E>();
		try {
			for (int i = 0; i < gammaTable.cnt; i++) {
				E elem = gammaTable.getElement(i * gammaTable.elementByteSize, gamma);
				K key = gammaTable.idList.get(i);
				result.put(key, elem);
			}
		} catch (Exception e) {
			return null;
		}
		return result;
	}
}
