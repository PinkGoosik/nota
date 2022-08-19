package test;

import com.xxmicloxx.noteblockapi.NoteBlockAPI;
import com.xxmicloxx.noteblockapi.event.SongEndEvent;
import com.xxmicloxx.noteblockapi.event.SongStartEvent;
import com.xxmicloxx.noteblockapi.model.Playlist;
import com.xxmicloxx.noteblockapi.model.RepeatMode;
import com.xxmicloxx.noteblockapi.model.Song;
import com.xxmicloxx.noteblockapi.songplayer.PositionSongPlayer;
import com.xxmicloxx.noteblockapi.songplayer.SongPlayer;
import com.xxmicloxx.noteblockapi.utils.NBSDecoder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.UUID;

public class Main implements ModInitializer {
	public static final String MOD_ID = "noteblock-api-test-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Song vitality = null;
	public static Song home = null;
	public static SongPlayer songPlayer = null;

	@Override
	public void onInitialize() {
		var api = NoteBlockAPI.getAPI();

		try {
			vitality = NBSDecoder.parse(new File("songs/vitality.nbs"));
			home = NBSDecoder.parse(new File("songs/home.nbs"));
			LOGGER.info("Songs successfully decoded!");
		}
		catch(Exception e) {
			LOGGER.warn("Failed to decode this song due to an exception: " + e);
		}

//		ServerTickEvents.END_SERVER_TICK.register(server -> {
//			if(songPlayer != null) {
//				songPlayer.onTick();
//			}
//		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("start-song").executes(context -> {
				var player = context.getSource().getPlayer();
				if(player != null) {
					var world = player.world;
					if(!world.isClient) {
						songPlayer = new PositionSongPlayer(new Playlist(vitality, home), world);
						((PositionSongPlayer) songPlayer).setTargetLocation(player.getBlockPos());
						songPlayer.addPlayer(player);
						songPlayer.setRepeatMode(RepeatMode.NONE);
						songPlayer.setPlaying(true);
					}
				}
				return 1;
			}));
			dispatcher.register(CommandManager.literal("toggle-song").executes(context -> {
				if(songPlayer != null) {
					songPlayer.setPlaying(!songPlayer.isPlaying());
				}
				return 1;
			}));
		});

		SongStartEvent.EVENT.register(sp -> {
			for(UUID uuid : sp.getPlayerUUIDs()) {
				PlayerEntity player = NoteBlockAPI.getAPI().getServer().getPlayerManager().getPlayer(uuid);
				if(player != null) {
					player.sendMessage(Text.of("Song Started Playing: " + sp.getSong().getPath()));
				}
			}
		});

		SongEndEvent.EVENT.register(sp -> {
			for(UUID uuid : sp.getPlayerUUIDs()) {
				PlayerEntity player = NoteBlockAPI.getAPI().getServer().getPlayerManager().getPlayer(uuid);
				if(player != null) {
					player.sendMessage(Text.of("Song Ended Playing: " + sp.getSong().getPath()));
				}
			}
		});

		//Example code, you can remove it
		FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(mod -> {
			ModMetadata meta = mod.getMetadata();
			LOGGER.info(meta.getName() + " " + meta.getVersion().getFriendlyString() + " successfully initialized!");
		});
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
