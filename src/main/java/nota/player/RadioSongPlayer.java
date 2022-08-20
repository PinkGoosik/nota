package nota.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import nota.Nota;
import nota.model.Layer;
import nota.model.Note;
import nota.model.Playlist;
import nota.model.Song;

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
		byte playerVolume = Nota.getPlayerVolume(player);

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
