package pw.ipex.plex.mods.messagingscreen.callback;

import pw.ipex.plex.mods.messagingscreen.render.PlexMessagingMessageHoverState;

public abstract class PlexMessagingMessageEventHandler {
	public void onClick(PlexMessagingMessageHoverState hoverState, int button) {}

	public void onHover(PlexMessagingMessageHoverState hoverState) {}
}
