package com.xxmicloxx.noteblockapi.model.playmode;

import com.xxmicloxx.noteblockapi.model.Layer;
import com.xxmicloxx.noteblockapi.model.Note;
import com.xxmicloxx.noteblockapi.model.Song;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Decides how is {@link Note} played to {@link PlayerEntity}
 */
public abstract class ChannelMode {

	public abstract void play(PlayerEntity player, BlockPos pos, Song song, Layer layer, Note note, float volume, boolean doTranspose);
}
