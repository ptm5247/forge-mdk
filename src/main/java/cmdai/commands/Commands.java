package cmdai.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;

public class Commands {
	
	private static Commands commands;
	
	private final CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
	
	public Commands() {
		StopCommand.register(dispatcher);
		FishCommand.register(dispatcher);
		DiscordCommand.register(dispatcher);
	}
	
	/** To be called during FMLClientSetupEvent. */
	public static void clientSetup() {
		MinecraftForge.EVENT_BUS.addListener(Commands::reloadCommands);
	}
	
	public static void reloadCommands(RegisterCommandsEvent event) {
		commands = new Commands();
	}
	
	public static Commands getCommands() {
		return commands;
	}
	
	public CommandDispatcher<CommandSourceStack> getDispatcher() {
		return dispatcher;
	}
	
	/** Adapted from {@link net.minecraft.commands.Commands#performCommand}. */
	@SuppressWarnings("resource")
	public void performCommand(String command) {
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
	}

}
