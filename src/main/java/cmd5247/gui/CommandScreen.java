package cmd5247.gui;

import cmd5247.Options;
import cmd5247.commands.Commands;
import cmd5247.task.TaskManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;

import net.minecraftforge.client.ClientRegistry;
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
	public void sendMessage(String command) {
		if (command.startsWith("$")) Commands.getCommands().performCommand(command);
		else sendMessage(command, true);
	}
	
	/** To be called during FMLClientSetupEvent. */
	public static void clientSetup() {
		ClientRegistry.registerKeyBinding(Options.keyCommand);
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
			
			if (game.screen == null && game.getOverlay() == null
					&& Options.keyCommand.consumeClick()) {
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
