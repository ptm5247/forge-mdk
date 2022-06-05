package cmdai.discord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.security.auth.login.LoginException;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;

import net.minecraftforge.common.MinecraftForge;

import cmdai.Util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;

public class DiscordBot {
	
	private static Optional<DiscordBot> bot = Optional.empty();
	
	private JDA jda;
	
	private DiscordBot() throws LoginException, IOException {
		var tokenBytes = this.getClass().getResourceAsStream("/discord_token").readAllBytes();
		var token = new String(tokenBytes, StandardCharsets.UTF_8);
		
		this.jda = JDABuilder.createDefault(token)
			.setEventManager(new AnnotatedEventManager())
			.addEventListeners(this)
			.setActivity(Activity.playing("Minecraft"))
			.build();
	}
	
	public static boolean start() {
		if (bot.isEmpty()) {
			new Thread(() -> {
				try {
					bot = Optional.of(new DiscordBot());
				} catch (LoginException | IOException e) {
					Util.msg(new TextComponent(e.getMessage()).withStyle(ChatFormatting.RED));
				}
			}, "JDA Builder Thread").start();
			return true;
		} else return false;
	}
	
	public static boolean stop() {
		if (bot.isPresent()) {
			bot.get().jda.shutdown();
			bot = Optional.empty();
			return true;
		} else return false;
	}
	
	@net.dv8tion.jda.api.hooks.SubscribeEvent
	public void on(ReadyEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		Util.msg("Discord bot ready!");
	}

}
