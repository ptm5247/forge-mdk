package cmdai.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import cmdai.Util;
import cmdai.discord.DiscordBot;

public class DiscordCommand {
	
	/** Registers the $discord command. */
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("discord")
			.then(Commands.literal("start").executes(DiscordCommand::start))
			.then(Commands.literal("stop").executes(DiscordCommand::stop)));
	}
	
	/** Starts up the discord bot. */
	public static int start(CommandContext<CommandSourceStack> context)
			throws CommandSyntaxException {
		Util.msg(DiscordBot.start()
				? "Starting the Discord bot..."
				: "The Discord bot is already running!");
		
		return Command.SINGLE_SUCCESS;
	}
	
	/** Shuts down the discord bot. */
	public static int stop(CommandContext<CommandSourceStack> context) {
		Util.msg(DiscordBot.stop()
				? "Shutting down the Discord bot..."
				: "The Discord bot is not running!");
		
		return Command.SINGLE_SUCCESS;
	}

}
