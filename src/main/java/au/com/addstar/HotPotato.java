package au.com.addstar;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HotPotato implements ModInitializer {
	public static final Item HOTPOTATO_ITEM = new HotPotatoItem(new FabricItemSettings().group(ItemGroup.MISC));
	public static final PotatoConfig config = AutoConfig.register(PotatoConfig.class, GsonConfigSerializer::new).getConfig();

	@Override
	public void onInitialize() {
		System.out.println("The HotPotato has arrived!");
		Registry.register(Registry.ITEM, new Identifier("hotpotato", "hot_potato"), HOTPOTATO_ITEM);
	}

	public static PotatoConfig getConfig() {
		return config;
	}

	public static StringTag makeText(String msg, Formatting... format) {
		Text lore = new LiteralText(msg).formatted(format);
		StringTag tag = StringTag.of(Text.Serializer.toJson(lore));
		return tag;
	}

	public static Text makeExchangeMsg(String owner, String last) {
		try {
			String msg = config.exchange_msg
					.replace("{to}", owner)
					.replace("{from}", last);
			Text msgtext = Text.Serializer.fromJson(msg);
			return msgtext;
		} catch (Exception e) {
			System.out.println("Failed to build exchange message.");
			e.printStackTrace();
		}
		return null;
	}

	public static void broadcastMsg(MinecraftServer server, Text msgtext, int delay) {
		//System.out.println("Scheduling broadcast...");
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		Runnable task = new Runnable() {
			final MinecraftServer s = server;
			final Text m = msgtext;
			final int d = delay;
			@Override
			public void run() {
				HotPotato.broadcastMsg(s, m);
			}
		};
		scheduler.schedule(task, delay, TimeUnit.SECONDS);
		scheduler.shutdown();
	}

	public static void broadcastMsg(MinecraftServer server, Text msgtext) {
		// broadcast a message in chat to all players
		// also log the message to console
		if (server != null && server.getPlayerManager() != null) {
			//System.out.println("[Broadcast] " + msgtext.asString());
			server.getPlayerManager().broadcastChatMessage(msgtext, MessageType.SYSTEM, null);
		}
	}
}
