package cmdai.discord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.security.auth.login.LoginException;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;

import cmdai.Util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;

public class DiscordBot {
	
	private static Optional<DiscordBot> bot = Optional.empty();
	
	private JDA jda;
	private Guild guild;
	private Category online, offline;
	private Map<UUID, TextChannel> UUID2Channel = new HashMap<>();
	private TextChannel general;
	
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
			MinecraftForge.EVENT_BUS.unregister(bot.get());
			bot.get().jda.shutdown();
			bot = Optional.empty();
			return true;
		} else return false;
	}
	
	@net.dv8tion.jda.api.hooks.SubscribeEvent
	public void on(ReadyEvent event) throws IOException {
		var bytes = getClass().getResourceAsStream("/discord_server_id").readAllBytes();
		this.guild = jda.getGuildById(new String(bytes, StandardCharsets.UTF_8));
		
		// ensure the online and offline categories exist
		var onlineCategories = guild.getCategoriesByName("Online Players", false);
		this.online = onlineCategories.isEmpty()
				? guild.createCategory("Online Players").complete()
				: onlineCategories.get(0);
		var offlineCategories = guild.getCategoriesByName("Offline Players", false);
		this.offline = offlineCategories.isEmpty()
				? guild.createCategory("Offline Players").complete()
				: offlineCategories.get(0);
		
		// ensure general exists
		var generalChannels = guild.getTextChannelsByName("general", false);
		this.general = generalChannels.isEmpty()
				? guild.createTextChannel("general").complete()
				: generalChannels.get(0);
		
		// ensure player channels all exist and are in the right category
		var moveOffline = new ArrayList<>(online.getTextChannels());
		for (var player : Minecraft.getInstance().getConnection().getOnlinePlayers()) {
			var uuid = player.getProfile().getId();
			var channel = UUID2Channel.computeIfAbsent(uuid, this::getChannelFromUUID);
			
			moveOffline.remove(channel);
		}
		moveOffline.forEach(c -> c.getManager().setParent(offline).complete());
		
		MinecraftForge.EVENT_BUS.register(this);
		Util.msg("Discord bot ready!");
	}
	
	/** Gets an existing text channel or creates a new one under online. */
	private TextChannel getChannelFromUUID(UUID uuid) {
		var cpl = Minecraft.getInstance().getConnection();
		var name = cpl.getPlayerInfo(uuid).getProfile().getName().toLowerCase();
		var textChannels = guild.getTextChannelsByName(name, false);
		
		return textChannels.isEmpty()
				? guild.createTextChannel(name, online).complete()
				: textChannels.get(0);
	}
	
	/** Moves a player channel into online upon login. */
	@net.minecraftforge.eventbus.api.SubscribeEvent
	public void on(PlayerLoggedInEvent event) {
		var uuid = event.getPlayer().getUUID();
		var channel = UUID2Channel.computeIfAbsent(uuid, this::getChannelFromUUID);
		
		channel.getManager().setParent(online).complete();
	}
	
	/** Moves a player channel into offline upon logout. */
	@net.minecraftforge.eventbus.api.SubscribeEvent
	public void on(PlayerLoggedOutEvent event) {
		UUID2Channel.get(event.getPlayer().getUUID()).getManager().setParent(offline).complete();
	}
	
	/** Forwards an incoming chat message to the appropriate channel. */
	@net.minecraftforge.eventbus.api.SubscribeEvent
	public void on(ClientChatReceivedEvent event) {
		var channel = Minecraft.getInstance().getConnection().getOnlinePlayerIds().size() <= 2
				? UUID2Channel.get(event.getSenderUUID())
				: general;
		
		String msg = event.getMessage().getString();
		
		switch (event.getType()) {
		case CHAT:
			if (channel != general) msg = msg.substring(msg.indexOf(' ') + 1);
			break;
		case SYSTEM:
			int skip = msg.indexOf("whispers to you:");
			if (skip != -1) {
				msg = msg.substring(skip + 17);
				break;
			}
		default: return;
		}
		
		channel.sendMessage(msg).queue();
	}

}
