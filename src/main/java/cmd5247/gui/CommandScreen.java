package cmd5247.gui;

import cmd5247.Options;
import cmd5247.commands.Commands;
import cmd5247.task.TaskManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;

import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;

public class CommandScreen extends ChatScreen {

	public CommandScreen(String p_95579_) {
		super(p_95579_);
	}

	@Override
	protected void init() {
		super.init();
		// change the CommandSuggestions to the local version
		this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.input, 
				this.font, false, false, 1, 10, true, -805306368);
		this.commandSuggestions.updateCommandInfo();
	}
	
	/**
	 * Called by {@link ChatScreen#keyPressed}.
	 * Overridden to cut out the ClientChatEvent middleman.
	 */
	@Override
	public boolean handleChatInput(String input, boolean p_242161_) {
		input = this.normalizeChatMessage(input);
		if (input.isEmpty()) {
		   return true;
		} else {
			if (p_242161_) {
				this.minecraft.gui.getChat().addRecentChat(input);
			}

			if (input.startsWith("$")) {
				Commands.getCommands().performCommand(input);
			} else if (input.startsWith("/")) {
				this.minecraft.player.connection.sendCommand(input.substring(1));
			} else {
				this.minecraft.player.connection.sendChat(input);
			}

			return minecraft.screen == this; // FORGE: Prevent closing the screen if another screen has been opened.
		}
	}

	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(Options.keyCommand);
		MinecraftForge.EVENT_BUS.addListener(CommandScreen::open);
	}

	/**
	 * The other option is to listen to KeyInputEvents, but these will be fired on both edges of
	 * every keyboard input. Normal chat and command screen opening inputs are checked at most once
	 * per client tick, so this method will hook this event to be more consistent with that.
	 */
	public static void open(ClientTickEvent event) {
		TaskManager.push("inputs");
		
		if (event.phase == Phase.END) {
			var game = Minecraft.getInstance();
			
			if (game.screen == null && game.getOverlay() == null && Options.keyCommand.consumeClick()) {
				var status = game.getChatStatus();

				if (!status.isChatAllowed(game.isLocalServer()))
					game.gui.setOverlayMessage(status.getMessage(), false);
				else
					game.setScreen(new CommandScreen("$"));
			}
		}
		
		TaskManager.pop();
	}

}
