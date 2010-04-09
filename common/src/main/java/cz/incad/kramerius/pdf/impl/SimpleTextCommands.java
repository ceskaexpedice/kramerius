package cz.incad.kramerius.pdf.impl;

import java.util.HashMap;
import java.util.Map;

public enum SimpleTextCommands {

	LINE("{line}"),
	PARA("{para}"),
	FONT("{font}");
	
	private String textValue;
	
	private SimpleTextCommands(String textValue) {
		this.textValue = textValue;
	}

	
	public String getTextValue() {
		return textValue;
	}


	public int indexStart(String text) {
		return text.indexOf(this.textValue);
	}

	public int oneParameter(String text, Map<String, String> mp, int indexOf) {
		if (checkIndex(text, indexOf)) {
			int index = indexOf + 1;
			boolean reading = true;
			boolean readingLeft = true;
			StringBuffer left = new StringBuffer();
			StringBuffer right = new StringBuffer();
			while(reading && checkIndex(text, index)) {
				char ch = text.charAt(index);
				if (ch == '=') {
					readingLeft = false;
					index += 1;
					continue;
				} if (ch == ',') {
					mp.put(left.toString(), right.toString());
					return index;
				} if (ch == ')') {
					mp.put(left.toString(), right.toString());
					return index;
				} if (readingLeft) {
					left.append(ch);
				} else {
					right.append(ch);
				}
				index +=1;
			}
			return index;
		} else return -1;		
	}
	
	public int streamParameters(String text, Map<String, String> mp, int indexOf) {
		if (checkIndex(text, indexOf)) {
			int oneParameter = oneParameter(text, mp, indexOf);
			while(oneParameter != -1) {
				char ch = text.charAt(oneParameter);
				if (ch == ')')  return oneParameter;
				if (ch != ',')  return -1;
				oneParameter = oneParameter(text, mp, oneParameter); 
			}
			return -1;
		} else return -1;
	}
	
	public Map<String, String> parameters(String text) {
		int indexStart = indexStart(text)+this.textValue.length();
		if (indexStart < text.length()) {
			char ch = text.charAt(indexStart);
			if (ch == '(') {
				Map<String, String> map = new HashMap<String, String>();
				int lastIndex = streamParameters(text, map, indexStart);
				if (checkIndex(text, lastIndex)) {
					ch = text.charAt(lastIndex);
					if (ch ==')') return map;
					else return new HashMap<String, String>();
				} else return new HashMap<String, String>();
			} else return new HashMap<String, String>();
		} else return new HashMap<String, String>();
	}


	private boolean checkIndex(String text, int lastIndex) {
		return (lastIndex > -1) && (lastIndex < text.length());
	}
	
	public static SimpleTextCommands findCommand(String text) {
		SimpleTextCommands[] values = values();
		for (SimpleTextCommands command : values) {
			if (text.contains(command.getTextValue())) {
				return command;
			}
		}
		return null;
	}
	
	
	public static void main(String[] args) {
		String text  = "ahoj nevim jak to pujde protoze nevim{font}(size=24,style=bold)";
		SimpleTextCommands findCommand = findCommand(text);
		System.out.println(findCommand);
		Map<String, String> parameters = findCommand.parameters(text);
		System.out.println(parameters);
	}
}
