package org.dfpl.chronograph.kairos.gamma.persistent.file;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import org.dfpl.chronograph.kairos.gamma.Gamma;
import org.dfpl.chronograph.kairos.gamma.GammaElement;

import io.vertx.core.json.JsonObject;

public class FixedSizedGamma<K, E> implements Gamma<K, E> {

	private FixedSizedGammaTable<K, E> gammaTable;
	private RandomAccessFile gamma;

	public FixedSizedGamma(FixedSizedGammaTable<K, E> gammaTable, RandomAccessFile gamma) {
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
				if(elem.equals(gammaTable.gammaElementConverter.getDefaultValue())){
					elem = null;
				}
				K key = gammaTable.idList.get(i);
				result.put(key, elem);
			}
		} catch (Exception e) {
			return null;
		}
		return result;
	}
	
	@Override
	public JsonObject toJson(boolean setDefaultToNull) {
		JsonObject result = new JsonObject();
		try {
			for (int i = 0; i < gammaTable.cnt; i++) {
				Object elem = gammaTable.getJsonValue(i * gammaTable.elementByteSize, gamma);
				if(elem.equals(gammaTable.gammaElementConverter.getDefaultValue())){
					elem = null;
				}
				K key = gammaTable.idList.get(i);
				result.put(key.toString(), elem);
			}
		} catch (Exception e) {
			return null;
		}
		return result;
	}
}
