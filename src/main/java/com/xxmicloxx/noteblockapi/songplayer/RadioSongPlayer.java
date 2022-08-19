package com.xxmicloxx.noteblockapi.songplayer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import com.xxmicloxx.noteblockapi.NoteBlockAPI;
import com.xxmicloxx.noteblockapi.model.Layer;
import com.xxmicloxx.noteblockapi.model.Note;
import com.xxmicloxx.noteblockapi.model.Playlist;
import com.xxmicloxx.noteblockapi.model.Song;

/**
 * SongPlayer playing to everyone added to it no matter where they are
 */
public class RadioSongPlayer extends SongPlayer {

	public RadioSongPlayer(Song song) {
		super(song);
	}

	public RadioSongPlayer(Playlist playlist) {
		super(playlist);
	}

	@Override
	public void playTick(PlayerEntity player, int tick) {
		byte playerVolume = NoteBlockAPI.getPlayerVolume(player);

		for(Layer layer : song.getLayerHashMap().values()) {
			Note note = layer.getNote(tick);
			if(note == null) {
				continue;
			}

			float volume = (layer.getVolume() * (int) this.volume * (int) playerVolume * note.getVelocity()) / 100_00_00_00F;

			channelMode.play(player, new BlockPos(player.getEyePos()), song, layer, note, volume, !enable10Octave);
		}
	}
}
