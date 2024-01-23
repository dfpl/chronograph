package org.dfpl.chronograph.kairos.gamma.persistent;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

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
	public List<E> toList() {
		List<E> list = new ArrayList<E>();
		try {
			for (int i = 0; i < gammaTable.cnt; i++) {
				E elem = gammaTable.getElement(i * gammaTable.elementByteSize, gamma);
				if (elem.equals(gammaTable.gammaElementConverter.getDefaultValue()))
					list.add(null);
				else
					list.add(elem);
			}
		} catch (Exception e) {
			return null;
		}
		return list;
	}
}
