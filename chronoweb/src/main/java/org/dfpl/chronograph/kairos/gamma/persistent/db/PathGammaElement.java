package org.dfpl.chronograph.kairos.gamma.persistent.db;

import java.util.List;

import org.bson.Document;
import org.dfpl.chronograph.kairos.gamma.GammaElement;

public class PathGammaElement implements GammaElement<Document> {

	private Document element;

	public PathGammaElement(List<String> path, Long minimumVisitedTime) {
		element = new Document();
		element.put("path", path);
		element.put("time", minimumVisitedTime);
	}

	@Override
	public Document getElement() {
		return element;
	}

	@Override
	public byte[] getBytes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Document toElement(byte[] bytesToRead) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object toJsonValue(byte[] bytesToRead) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getElementByteSize() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<Document> getElementClass() {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte getDefaultByteValue() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Document getDefaultValue() {
		throw new UnsupportedOperationException();
	}

}
