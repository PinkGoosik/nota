package com.xxmicloxx.noteblockapi.songplayer;

import com.xxmicloxx.noteblockapi.NoteBlockAPI;
import com.xxmicloxx.noteblockapi.event.SongStartEvent;
import com.xxmicloxx.noteblockapi.event.SongEndEvent;
import com.xxmicloxx.noteblockapi.event.SongTickEvent;
import com.xxmicloxx.noteblockapi.model.*;
import com.xxmicloxx.noteblockapi.model.playmode.ChannelMode;
import com.xxmicloxx.noteblockapi.model.playmode.MonoMode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plays a Song for a list of Players
 */
@SuppressWarnings("unused")
public abstract class SongPlayer {
	Identifier id = new Identifier("noteblock-api:unidentified");

	protected Song song;
	protected Playlist playlist;
	protected int currentSongIndex = 0;

	public boolean playing = false;
	protected boolean fading = false;
	protected short tick = -1;
	protected Map<UUID, Boolean> playerList = new ConcurrentHashMap<>();

	protected boolean autoDestroy = false;
	protected boolean destroyed = false;

	protected byte volume = 100;
	float songDelay = 50.0F; //milliseconds per tick

	protected RepeatMode repeat = RepeatMode.ALL;

	protected NoteBlockAPI api;
	protected ChannelMode channelMode = new MonoMode();
	protected boolean enable10Octave = false;

	Timer timer = new Timer();

	public SongPlayer(Song song) {
		this(new Playlist(song));
	}

	public SongPlayer(Playlist playlist) {
		this.playlist = playlist;
		this.api = NoteBlockAPI.getAPI();
		this.song = playlist.get(this.currentSongIndex);
		this.songDelay = song.getDelay() * 50.0f;
		schedule((long)this.songDelay);
	}

	/**
	 * Check if 6 octave range is enabled
	 *
	 * @return true if enabled, false otherwise
	 */
	public boolean isEnable10Octave() {
		return this.enable10Octave;
	}

	/**
	 * Enable or disable 6 octave range
	 * <p>
	 * If not enabled, notes will be transposed to 2 octave range
	 *
	 * @param enable10Octave true if enabled, false otherwise
	 */
	public void setEnable10Octave(boolean enable10Octave) {
		this.enable10Octave = enable10Octave;
	}

	private void play() {
		for(UUID uuid : playerList.keySet()) {
			PlayerEntity player = NoteBlockAPI.getAPI().getServer().getPlayerManager().getPlayer(uuid);
			if(player != null) {
				this.playTick(player, tick);
			}
		}
	}

	private void onTaskRun(TimerTask task) {
		if(this.destroyed || NoteBlockAPI.getAPI().isDisabling()) {
			task.cancel();
		}
		if(playing) {
			tick++;
			if(tick == 0) {
				SongStartEvent.EVENT.invoker().onSongStart(this);
			}
			if(tick > song.getLength()) {
				SongEndEvent.EVENT.invoker().onSongEnd(this);
				tick = -1;

				if(playlist.hasNext(currentSongIndex)) {
					currentSongIndex++;
					song = playlist.get(currentSongIndex);
					songDelay = song.getDelay() * 50.0f;
					this.playSong(currentSongIndex);
					this.schedule((long)this.songDelay);
					task.cancel();
					return;
				}
				else {
					currentSongIndex = 0;
					song = playlist.get(currentSongIndex);
					songDelay = song.getDelay() * 50.0f;
					if(repeat.equals(RepeatMode.ALL)) {
						this.schedule((long)this.songDelay);
						task.cancel();
						return;
					}
				}
				playing = false;
				if(this.autoDestroy) {
					task.cancel();
				}
				return;
			}
			SongTickEvent.EVENT.invoker().onSongTick(this);
			for(UUID uuid : playerList.keySet()) {
				PlayerEntity player = NoteBlockAPI.getAPI().getServer().getPlayerManager().getPlayer(uuid);
				if(player != null) {
					this.playTick(player, tick);
				}
			}
		}
	}

	/**
	 * Starts this SongPlayer
	 */
	private void schedule(long delay) {
		this.timer.schedule(new TimerTask() {
			@Override
			public void run() {
				onTaskRun(this);
			}
		}, 0L, delay);
	}

//	private void start() {
//		api.doAsync(() -> {
//			while (!destroyed) {
//				long startTime = System.currentTimeMillis();
//				lock.lock();
//				try {
//					if (destroyed || NoteBlockAPI.getAPI().isDisabling()){
//						break;
//					}
//
//					if (playing || fading) {
//						if (fadeTemp != null){
//							if (fadeTemp.isDone()) {
//								fadeTemp = null;
//								fading = false;
//								if (!playing) {
//									//TODO: port SongStoppedEvent to fabric events
////									SongStoppedEvent event = new SongStoppedEvent(this);
////									plugin.doSync(() -> Bukkit.getPluginManager().callEvent(event));
//									volume = fadeIn.getFadeTarget();
//									continue;
//								}
//							}else {
//								int fade = fadeTemp.calculateFade();
//								if (fade != -1){
//									volume = (byte) fade;
//								}
//							}
//						} else if (tick < fadeIn.getFadeDuration()){
//							int fade = fadeIn.calculateFade();
//							if (fade != -1){
//								volume = (byte) fade;
//							}
//							CallUpdate("fadeDone", fadeIn.getFadeDone());
//						} else if (tick >= song.getLength() - fadeOut.getFadeDuration()){
//							int fade = fadeOut.calculateFade();
//							if (fade != -1){
//								volume = (byte) fade;
//							}
//						}
//
//						tick++;
//						if (tick > song.getLength()) {
//							tick = -1;
//							fadeIn.setFadeDone(0);
//							CallUpdate("fadeDone", fadeIn.getFadeDone());
//							fadeOut.setFadeDone(0);
//							volume = fadeIn.getFadeTarget();
//							if (repeat == RepeatMode.ONE){
//								//TODO: port SongLoopEvent to fabric events
////								SongLoopEvent event = new SongLoopEvent(this);
////								plugin.doSync(() -> Bukkit.getPluginManager().callEvent(event));
//
////								if (!event.isCancelled()) {
////									continue;
////								}
//							} else {
//								if (random) {
//									songQueue.put(song, true);
//									checkPlaylistQueue();
//									ArrayList<Song> left = new ArrayList<>();
//									for (Song s : songQueue.keySet()) {
//										if (!songQueue.get(s)) {
//											left.add(s);
//										}
//									}
//
//									if (left.size() == 0) {
//										left.addAll(songQueue.keySet());
//										for (Song s : songQueue.keySet()) {
//											songQueue.put(s, false);
//										}
//										song = left.get(rng.nextInt(left.size()));
//										actualSong = playlist.getIndex(song);
//										CallUpdate("song", song);
//										if (repeat == RepeatMode.ALL) {
//											// TODO: port SongLoopEvent to fabric events
////											SongLoopEvent event = new SongLoopEvent(this);
////											plugin.doSync(() -> Bukkit.getPluginManager().callEvent(event));
////
////											if (!event.isCancelled()) {
////												continue;
////											}
//										}
//									} else {
//										song = left.get(rng.nextInt(left.size()));
//										actualSong = playlist.getIndex(song);
//
//										CallUpdate("song", song);
//										// TODO: port SongNextEvent to fabric events
////										SongNextEvent event = new SongNextEvent(this);
////										plugin.doSync(() -> Bukkit.getPluginManager().callEvent(event));
//										continue;
//									}
//								} else {
//									if (playlist.hasNext(actualSong)) {
//										actualSong++;
//										song = playlist.get(actualSong);
//										CallUpdate("song", song);
//										// TODO: port SongNextEvent to fabric events
////										SongNextEvent event = new SongNextEvent(this);
////										plugin.doSync(() -> Bukkit.getPluginManager().callEvent(event));
//										continue;
//									} else {
//										actualSong = 0;
//										song = playlist.get(actualSong);
//										CallUpdate("song", song);
//										if (repeat == RepeatMode.ALL) {
//											// TODO: port SongNextEvent to fabric events
////											SongLoopEvent event = new SongLoopEvent(this);
////											plugin.doSync(() -> Bukkit.getPluginManager().callEvent(event));
//
////											if (!event.isCancelled()) {
////												continue;
////											}
//										}
//									}
//								}
//							}
//							playing = false;
//							// TODO: port SongEndEvent to fabric events
////							SongEndEvent event = new SongEndEvent(this);
////							plugin.doSync(() -> Bukkit.getPluginManager().callEvent(event));
//							if (autoDestroy) {
//								destroy();
//							}
//							continue;
//						}
//						CallUpdate("tick", tick);
//
//						api.doSync(() -> {
//							try {
//								for (UUID uuid : playerList.keySet()) {
//									PlayerEntity player = NoteBlockAPI.getAPI().getServer().getPlayerManager().getPlayer(uuid);
//									if (player == null) {
//										// offline...
//										continue;
//									}
//									playTick(player, tick);
//								}
//							} catch (Exception e){
//								//TODO: one logger please
////								Bukkit.getLogger().severe("An error occurred during the playback of song "
////										+ (song != null ?
////										song.getPath() + " (" + song.getAuthor() + " - " + song.getTitle() + ")"
////										: "null"));
//								e.printStackTrace();
//							}
//						});
//					}
//				} catch (Exception e) {
//					//TODO: and here
////					Bukkit.getLogger().severe("An error occurred during the playback of song "
////							+ (song != null ?
////									song.getPath() + " (" + song.getAuthor() + " - " + song.getTitle() + ")"
////									: "null"));
//					e.printStackTrace();
//				} finally {
//					lock.unlock();
//				}
//
//				if (destroyed) {
//					break;
//				}
//
//				long duration = System.currentTimeMillis() - startTime;
//				float delayMillis = song.getDelay() * 50;
//				if (duration < delayMillis) {
//					try {
//						Thread.sleep((long) (delayMillis - duration));
//					} catch (InterruptedException e) {
//						// do nothing
//					}
//				}
//			}
//		});
//	}

	/**
	 * Gets unique id of this SongPlayer
	 *
	 * @return song player's unique id
	 */
	public Identifier getId() {
		return this.id;
	}

	/**
	 * Sets unique id for this SongPlayer
	 *
	 */
	public void setId(Identifier id) {
		this.id = id;
	}

	/**
	 * Gets list of current Player UUIDs listening to this SongPlayer
	 *
	 * @return list of Player UUIDs
	 */
	public Set<UUID> getPlayerUUIDs() {
		Set<UUID> uuids = new HashSet<>(this.playerList.keySet());
		return Collections.unmodifiableSet(uuids);
	}

	/**
	 * Adds a Player to the list of Players listening to this SongPlayer
	 *
	 * @param player player entity
	 */
	public void addPlayer(PlayerEntity player) {
		addPlayer(player.getUuid());
	}

	/**
	 * Adds a Player to the list of Players listening to this SongPlayer
	 *
	 * @param playerUuid player's uuid
	 */
	public void addPlayer(UUID playerUuid) {
		if(!this.playerList.containsKey(playerUuid)) {
			this.playerList.put(playerUuid, false);
			ArrayList<SongPlayer> songs = NoteBlockAPI.getSongPlayersByPlayer(playerUuid);
			if(songs == null) {
				songs = new ArrayList<>();
			}
			songs.add(this);
			NoteBlockAPI.setSongPlayersByPlayer(playerUuid, songs);
		}
	}

	/**
	 * Returns whether the SongPlayer is set to destroy itself when no one is listening
	 * or when the Song ends
	 *
	 * @return if autoDestroy is enabled
	 */
	public boolean getAutoDestroy() {
		return this.autoDestroy;
	}

	/**
	 * Sets whether the SongPlayer is going to destroy itself when no one is listening
	 * or when the Song ends
	 *
	 * @param autoDestroy if auto destroy should be enabled
	 */
	public void setAutoDestroy(boolean autoDestroy) {
		this.autoDestroy = autoDestroy;
	}

	/**
	 * Plays the Song for the specific player
	 *
	 * @param player to play this SongPlayer for
	 * @param tick   to play at
	 */
	public abstract void playTick(PlayerEntity player, int tick);

	/**
	 * SongPlayer will destroy itself
	 */
	public void destroy() {
		this.destroyed = true;
		this.playing = false;
		this.setTick((short) -1);
		//TODO: SongDestroyingEvent event probably
//			SongDestroyingEvent event = new SongDestroyingEvent(this);
//			plugin.doSync(() -> Bukkit.getPluginManager().callEvent(event));
		//Bukkit.getScheduler().cancelTask(threadId);
//			if (event.isCancelled()) {
//				return;
//			}
	}

	/**
	 * Returns whether the SongPlayer is actively playing
	 *
	 * @return if this player is playing
	 */
	public boolean isPlaying() {
		return this.playing;
	}

	/**
	 * Sets whether the SongPlayer is playing
	 *
	 * @param playing if this player should play
	 */
	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	/**
	 * Gets the current tick of this SongPlayer
	 *
	 * @return current tick
	 */
	public short getTick() {
		return this.tick;
	}

	/**
	 * Sets the current tick of this SongPlayer
	 *
	 * @param tick tick
	 */
	public void setTick(short tick) {
		this.tick = tick;
	}

	/**
	 * Removes a player from this SongPlayer
	 *
	 * @param player to remove
	 */
	public void removePlayer(PlayerEntity player) {
		removePlayer(player.getUuid());
	}

	/**
	 * Removes a player from this SongPlayer
	 *
	 * @param playerUuid of player to remove
	 */
	public void removePlayer(UUID playerUuid) {
		playerList.remove(playerUuid);
		if(NoteBlockAPI.getSongPlayersByPlayer(playerUuid) == null) {
			return;
		}
		ArrayList<SongPlayer> songs = new ArrayList<>(
				NoteBlockAPI.getSongPlayersByPlayer(playerUuid));
		songs.remove(this);
		NoteBlockAPI.setSongPlayersByPlayer(playerUuid, songs);
		if(this.playerList.isEmpty() && this.autoDestroy) {
//				SongEndEvent event = new SongEndEvent(this);
//				plugin.doSync(() -> Bukkit.getPluginManager().callEvent(event));
			destroy();
		}
	}

	/**
	 * Gets the current volume of this SongPlayer
	 *
	 * @return volume (0-100)
	 */
	public byte getVolume() {
		return this.volume;
	}

	/**
	 * Sets the current volume of this SongPlayer
	 *
	 * @param volume (0-100)
	 */
	public void setVolume(byte volume) {
		if(volume > 100) {
			volume = 100;
		}
		else if(volume < 0) {
			volume = 0;
		}
		this.volume = volume;
	}

	/**
	 * Gets the Song being played by this SongPlayer
	 *
	 * @return song
	 */
	public Song getSong() {
		return this.song;
	}

	/**
	 * Gets the Playlist being played by this SongPlayer
	 *
	 * @return playlist
	 */
	public Playlist getPlaylist() {
		return this.playlist;
	}

	/**
	 * Sets the Playlist being played by this SongPlayer. Will affect next Song
	 */
	public void setPlaylist(Playlist playlist) {
		this.playlist = playlist;
	}

	/**
	 * Get index of actually played {@link Song} in {@link Playlist}
	 *
	 * @return song index
	 */
	public int getPlayedSongIndex() {
		return this.currentSongIndex;
	}

	/**
	 * Start playing {@link Song} at specified index in {@link Playlist}
	 * If there is no {@link Song} at this index, {@link SongPlayer} will continue playing current song
	 *
	 * @param index song index
	 */
	public void playSong(int index) {
		if(this.playlist.exist(index)) {
			this.song = this.playlist.get(index);
			this.currentSongIndex = index;
			this.tick = -1;
		}
	}

	/**
	 * Start playing {@link Song} that is next in {@link Playlist} or random {@link Song} from {@link Playlist}
	 */
	public void playNextSong() {
		this.tick = this.song.getLength();
	}

	/**
	 * Sets SongPlayer's {@link RepeatMode}
	 *
	 * @param repeatMode repeat mode
	 */
	public void setRepeatMode(RepeatMode repeatMode) {
		this.repeat = repeatMode;
	}

	/**
	 * Gets SongPlayer's {@link RepeatMode}
	 *
	 * @return repeat mode
	 */
	public RepeatMode getRepeatMode() {
		return this.repeat;
	}

	public ChannelMode getChannelMode() {
		return this.channelMode;
	}
}
