package pw.ipex.plex.mods.messaging;

import java.util.ArrayList;
import java.util.List;

public class PlexMessagingMessageRenderData {
	public Integer relativeX = 0; // null = centered
	public int relativeY = 0;
	
	public int totalHeight = 0;
	public int maxWidth = 0;
	public String authorName = "";
	public boolean authorVisible = false;
	public List<PlexMessagingMessageTextData> textLines;
	
	public String playerHead = null;
	public int playerHeadX = 0;
	public int playerHeadY = 0;
	public int playerHeadSize = 0;
	
	public int textBackdropX = 0;
	public int textBackdropY = 0;
	public int textBackdropWidth = 0;
	public int textBackdropHeight = 0;
	public boolean displayBackdrop = false;
	
	public int textColour = 0xffffff;
	public int backdropColour = 0x85454545;
	
	public PlexMessagingMessageRenderData() {
		this.textLines = new ArrayList<PlexMessagingMessageTextData>();
	}
	
	public void addTextLine(String text, float scale, int x, int y, int width) {
		PlexMessagingMessageTextData textData = new PlexMessagingMessageTextData();
		textData.text = text;
		textData.scale = scale;
		textData.x = x;
		textData.y = y;
		textData.width = width;
		this.textLines.add(textData);
	}
	
	public int getXPosition(int startX, int endX) {
		if (this.relativeX == null) {
			return startX + ((endX - startX) / 2);
		}
		return startX + (this.relativeX % (endX - startX));
	}

	public int getYPosition(int yPos) {
		return yPos + this.relativeY;
	}
	
	public int getItemXPosition(int startX, int endX, int itemXPos) {
		return this.getXPosition(startX, endX) + itemXPos;
	}
	
	public int getItemYPosition(int yPos, int itemYpos) {
		return this.getYPosition(yPos) + itemYpos;
	}
}
