package pw.ipex.plex.core.regex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlexCoreRegexEntry {
	public String entryName;
	public String regexString;
	public Pattern regexPattern;
	public Map<String, Integer> patternNames = new HashMap<>();
	public List<String> identifierTags = new ArrayList<>();
	public char FORMAT_SYMBOL_CHAR = (char) 167;
	public String FORMAT_SYMBOL = String.valueOf(FORMAT_SYMBOL_CHAR);
	
	public String formatInput(String input) {
		if (input.indexOf((char) 167) == -1) {
			input = input.replace('&', FORMAT_SYMBOL_CHAR);
		}
		return input;
	}
	
	public PlexCoreRegexEntry(String name, String pattern) {
		pattern = this.formatInput(pattern);
		this.entryName = name;
		this.regexString = pattern;
		this.regexPattern = Pattern.compile(pattern);
	}
	
	public PlexCoreRegexEntry(String name, String pattern, String idTag) {
		pattern = this.formatInput(pattern);
		this.entryName = name;
		this.regexString = pattern;
		this.regexPattern = Pattern.compile(pattern);
		this.tag(idTag);
	}
	
	public PlexCoreRegexEntry tag(String name) {
		this.identifierTags.add(name);
		return this;
	}
	
	public boolean hasTag(String name) {
		return this.identifierTags.contains(name);
	}
	
	public PlexCoreRegexEntry addField(int group, String name) {
		this.patternNames.put(name, group);
		return this;
	}
	
	public boolean matches(String string) {
		return string.matches(this.regexString);
	}
	
	public boolean hasField(String field) {
		return this.patternNames.keySet().contains(field);
	}
	
	public String getField(String input, String field) {
		try {
			return this.getAllFields(input).get(field);
		}
		catch (Throwable e) {
			return null;
		}
	}
	
	public Map<String, String> getAllFields(String input) {
		Map<String, String> output = new HashMap<>();
		Matcher matcher = this.regexPattern.matcher(input);
		matcher.find();
		for (String groupName : patternNames.keySet()) {
			try {
				output.put(groupName, matcher.group(this.patternNames.get(groupName)));
			}
			catch (Throwable e) {
				output.put(groupName, null);
			}			
		}
		return output;		
	}
	
	public String formatStringWithGroups(String messageInput, String formattingString) {
		messageInput = this.formatInput(messageInput);
		formattingString = this.formatInput(formattingString);
		Map<String, String> groups = this.getAllFields(messageInput);
		for (String groupName : groups.keySet()) {
			formattingString = formattingString.replace("{" + groupName + "}", groups.get(groupName) != null ? groups.get(groupName) : "");
			formattingString = formattingString.replace("{$" + groupName + "}", groups.get(groupName) != null ? groups.get(groupName).replaceAll(FORMAT_SYMBOL + "[0-9a-fA-Fklmor]", "") : "");
		}
		return formattingString;
	}
}