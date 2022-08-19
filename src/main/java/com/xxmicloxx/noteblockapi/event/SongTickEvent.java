package com.xxmicloxx.noteblockapi.event;

import com.xxmicloxx.noteblockapi.songplayer.SongPlayer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface SongTickEvent {

	/**
	 * Called at the start of the song tick.
	 */
	Event<SongTickEvent> EVENT = EventFactory.createArrayBacked(SongTickEvent.class, (callbacks) -> (songPlayer) -> {
		for (SongTickEvent callback : callbacks) {
			callback.onSongTick(songPlayer);
		}
	});

	void onSongTick(SongPlayer songPlayer);
}
