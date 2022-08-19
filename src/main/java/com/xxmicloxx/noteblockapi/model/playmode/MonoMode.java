package com.xxmicloxx.noteblockapi.model.playmode;

import com.xxmicloxx.noteblockapi.model.*;
import com.xxmicloxx.noteblockapi.utils.InstrumentUtils;
import com.xxmicloxx.noteblockapi.utils.NoteUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;

/**
 * {@link Note} is played inside of {@link PlayerEntity}'s head.
 */
public class MonoMode extends ChannelMode {

	@Override
	public void play(PlayerEntity player, BlockPos pos, Song song, Layer layer, Note note, float volume, boolean doTranspose) {
		float pitch;
		if(doTranspose) {
			pitch = NoteUtils.getPitchTransposed(note);
		}
		else {
			pitch = NoteUtils.getPitchInOctave(note);
		}
		player.playSound(InstrumentUtils.getInstrument(note.getInstrument()), SoundCategory.RECORDS, volume, pitch);
	}
}
