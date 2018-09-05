package pw.ipex.plex.mods.autogg;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import pw.ipex.plex.Plex;
import pw.ipex.plex.core.*;
import pw.ipex.plex.mod.PlexModBase;
import scala.collection.parallel.ParIterableLike;

public class PlexAutoGGMod extends PlexModBase {
	public PlexCoreValue modEnabled = new PlexCoreValue("autoGG_enabled", false);
	public PlexCoreValue ggDelay = new PlexCoreValue("autoGG_delay", 0.5D);
	public PlexCoreValue ggMessagePrimary = new PlexCoreValue("autoGG_primaryMessage", "gg");
	public PlexCoreValue ggMessageSecondary = new PlexCoreValue("autoGG_secondaryMessage", "gg.");
	public PlexCoreValue ggWaitUntilSilenceEnd = new PlexCoreValue("autoGG_waitUntilSilenceOver", true);
	
	public static double MIN_DELAY = 0.0D;
	public static double MAX_DELAY = 5.0D;
	
	public String subtitleText = "";
	public int useGGMessage = 0;
	public Long gameOverTime = null;
	public Long ggSendTime = null;
	public Long ggCooldown = null;
	public boolean sentGG = false;
	public boolean scheduleGGatChatUnsilence = true;

	@Override
	public void modInit() {
		this.modEnabled.set(this.modSetting("autogg_enabled", false).getBoolean(false));
		this.ggDelay.set(this.modSetting("autogg_delay", 0.5D).getDouble());
		this.ggMessagePrimary.set(this.modSetting("autogg_primary_message", "gg").getString());
		this.ggMessageSecondary.set(this.modSetting("autogg_secondary_message", "gg.").getString());
		this.ggWaitUntilSilenceEnd.set(this.modSetting("autogg_wait_until_silence_over", true).getBoolean(true));
		
		Plex.plexCommand.registerPlexCommand("autogg", new PlexAutoGGCommand());
		
		Plex.plexCommand.addPlexHelpCommand("autogg", "Displays AutoGG options");
		
		PlexCore.registerUiTab("AutoGG", PlexAutoGGUI.class);
	}
	
	@Override
	public String getModName() {
		return "AutoGG";
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent e) {
		if (!this.modEnabled.booleanValue) {
			return;
		}
		try {
			Field subtitleField = Plex.minecraft.ingameGUI.getClass().getDeclaredField("field_175200_y");
			subtitleField.setAccessible(true);
			this.subtitleText = (String) subtitleField.get(Plex.minecraft.ingameGUI);
		}
		catch (Throwable e1) {
			try {
				Field subtitleField = Plex.minecraft.ingameGUI.getClass().getDeclaredField("displayedSubTitle");
				subtitleField.setAccessible(true);
				this.subtitleText = (String) subtitleField.get(Plex.minecraft.ingameGUI);
			}
			catch (Throwable e2) {
				this.subtitleText = null;
			}
		}
		
		if (this.sentGG) {
			this.sentGG = false;
			this.ggSendTime = null;
			this.scheduleGGatChatUnsilence = false; 
		}

		if (this.subtitleText != null) {
			if (this.subtitleText.contains("won the game") && this.gameOverTime == null) {
				this.gameOverTime = Minecraft.getSystemTime();
			}
			else {
				this.gameOverTime = null;
			}
		}
		
		if (this.isGameOver()) {
			if (!this.ggWaitUntilSilenceEnd.booleanValue && Minecraft.getSystemTime() < this.gameOverTime + 1000L) {
				this.scheduleGG((long) (this.ggDelay.doubleValue * 1000.0D));
			}
			else if (this.ggWaitUntilSilenceEnd.booleanValue) {
				scheduleGGatChatUnsilence = true;
			}
		}

		if (this.gameOverTime != null) {
			if (Minecraft.getSystemTime() > this.gameOverTime + 12000L) {
				this.gameOverTime = null;
			}
		}
		
		if (this.ggSendTime != null) {
			if (Minecraft.getSystemTime() > this.ggSendTime) {
				this.sendGG();
			}
		}
	}
	
	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent e) {
		if (!this.modEnabled.booleanValue) {
			return;
		}
		String minified = PlexCoreUtils.minimalize(e.message.getFormattedText());
		if (this.gameOverTime == null) {
			if (minified.matches("^1st place - (.*)$")) {
				this.gameOverTime = Minecraft.getSystemTime();
			}
			else if (minified.matches("^([a-z]+) (.* )+won the game!$")) {
				this.gameOverTime = Minecraft.getSystemTime();
			}
		}
		if (minified.contains("chat> chat is no longer silenced")) {
			if (this.scheduleGGatChatUnsilence) {
				scheduleGGatChatUnsilence = false;
				this.scheduleGG((long) (this.ggDelay.doubleValue * 1000.0D));
			}
		}
	}

	/*
	@SubscribeEvent
	public void onOverlay(RenderGameOverlayEvent event) {
		if (this.subtitleText != null) {
			PlexCoreRenderUtils.drawScaledString("\"" + this.subtitleText + "\"", 25, 45, 0xffffffff, 0.5F, false);
		}
		else {
			PlexCoreRenderUtils.drawScaledString("\"\"", 25, 45, 0xffffffff, 0.5F, false);
		}

	}
	*/
	
	public boolean isGameOver() {
		if (this.gameOverTime == null) {
			return false;
		}
		return Minecraft.getSystemTime() < this.gameOverTime + 20000L;
	}
	
	public void scheduleGG(long delay) {
		if (this.ggCooldown != null) {
			if (Minecraft.getSystemTime() < this.ggCooldown) {
				return;
			}
		}
		if (this.ggSendTime == null) {
			this.ggSendTime = Minecraft.getSystemTime() + delay;
			this.ggCooldown = Minecraft.getSystemTime() + 10000L;
			this.gameOverTime = null;
		}
	}
	
	public void sendGG() {
		String ggMessage = this.ggMessagePrimary.stringValue;
		if (this.useGGMessage % 2 == 1) {
			ggMessage = this.ggMessageSecondary.stringValue;
		}
		Plex.minecraft.thePlayer.sendChatMessage(ggMessage);
		this.useGGMessage = (this.useGGMessage + 1) % 2;
		this.sentGG = true;
	}
	
	@Override
	public void saveModConfig() {
		this.modSetting("autogg_enabled", false).set(this.modEnabled.booleanValue);
		this.modSetting("autogg_delay", 0.5F).set(this.ggDelay.doubleValue);
		this.modSetting("autogg_primary_message", "").set(this.ggMessagePrimary.stringValue);
		this.modSetting("autogg_secondary_message", "").set(this.ggMessageSecondary.stringValue);
		this.modSetting("autogg_wait_until_silence_over", true).set(this.ggWaitUntilSilenceEnd.booleanValue);
	}

	@Override
	public void switchedLobby(PlexCoreLobbyType type) {
		this.gameOverTime = null;
		this.sentGG = false;
		this.scheduleGGatChatUnsilence = false;
		if (type.equals(PlexCoreLobbyType.SERVER_HUB)) {
			this.useGGMessage = 0;
		}
	}
}
