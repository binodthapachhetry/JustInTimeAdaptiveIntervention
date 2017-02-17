package edu.neu.android.wocketslib.utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlParser extends DefaultHandler {
	private String tagName;
	private WocketsRecord wr;
	private final String[] position = { "Right Wrist", "Right Ankle", "Left Wrist", "Left Ankle", "Right Pocket", "Left Pocket" };
	private String[] ids = new String[2];

	public void setWr(WocketsRecord wr) {
		this.wr = wr;
	}

	public WocketsRecord getWr() {
		return wr;
	}

	public XmlParser() {
		super();
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
		String temp = new String(ch, start, length);
		int counter = 0;
		if (tagName.equals("Swapped_Sensor")) {
			for (int i = 0; i < position.length; i++) {
				if (temp.equals(position[i]))
					wr.setLabel(i, ids[counter]);
			}
			counter++;
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		this.tagName = localName;
		for (int i = 0; i < attributes.getLength(); i++) {
			ids[i] = attributes.getValue(i);
			// Log.v("deBug", ids[i]);
		}
	}

}
