package cmdai.local;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;

import cmdai.Options;

public class CommandScreen extends ChatScreen {
	
	public static final CommandDispatcher<CommandSourceStack> dispatcher =
			new CommandDispatcher<>();

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
	 * The other option is to listen to KeyInputEvents, but these will be fired on both edges of
	 * every keyboard input. Normal chat and command screen opening inputs are checked at most once
	 * per client tick, so this method will hook this event to be more consistent with that.
	 */
	public static void open(ClientTickEvent event) {
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
	}
	
	/**
	 * Called by {@link ChatScreen#keyPressed}.
	 * Overridden to cut out the ClientChatEvent middleman.
	 * Adapted from {@link net.minecraft.commands.Commands#performCommand}.
	 */
	@Override
	@SuppressWarnings("resource")
	public void sendMessage(String command) {
		if (command.startsWith("$")) {
			var source = Minecraft.getInstance().player.createCommandSourceStack();
			Minecraft.getInstance().gui.getChat().addRecentChat(command);

			try {
				dispatcher.execute(command.substring(1), source);
			} catch (CommandRuntimeException cre) {
				source.sendFailure(cre.getComponent());
			} catch (CommandSyntaxException cse) {
				source.sendFailure(ComponentUtils.fromMessage(cse.getRawMessage()));
				if (cse.getInput() != null && cse.getCursor() >= 0) {
					MutableComponent failure = new TextComponent("")
							.withStyle(ChatFormatting.GRAY)
							.withStyle(style -> style.withClickEvent(
									new ClickEvent(Action.SUGGEST_COMMAND, command)));
					int j = Math.min(cse.getInput().length(), cse.getCursor());
					
					if (j > 10) failure.append("...");
					failure.append(cse.getInput().substring(Math.max(0, j - 10), j));
					if (j < cse.getInput().length()) failure.append(
							new TextComponent(cse.getInput().substring(j))
									.withStyle(new ChatFormatting[] {
											ChatFormatting.RED, ChatFormatting.UNDERLINE}));
					failure.append(new TranslatableComponent("command.context.here")
							.withStyle(new ChatFormatting[] {
									ChatFormatting.RED, ChatFormatting.ITALIC}));
					
					source.sendFailure(failure);
				}
			}
		} else sendMessage(command, true);
	}

}
