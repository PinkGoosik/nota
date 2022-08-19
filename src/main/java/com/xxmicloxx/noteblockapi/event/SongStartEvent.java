package com.xxmicloxx.noteblockapi.event;

import com.xxmicloxx.noteblockapi.songplayer.SongPlayer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface SongStartEvent {

	/**
	 * Called on start of a song.
	 *
	 */
	Event<SongStartEvent> EVENT = EventFactory.createArrayBacked(SongStartEvent.class, (callbacks) -> (songPlayer) -> {
		for (SongStartEvent callback : callbacks) {
			callback.onSongStart(songPlayer);
		}
	});

	void onSongStart(SongPlayer songPlayer);
}
