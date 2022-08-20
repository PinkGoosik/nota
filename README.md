### Nota

<img align="right" width="100" src="src/main/resources/assets/nota/icon.png">

Port of the NoteBlock API for fabric, play .nbs files as noteblock sounds.
This port is not complete replica, some features are missing or unfinished.
You can find original spigot plugin [here](https://www.spigotmc.org/resources/noteblockapi.19287/).

### Development

#### Adding to your project
To use Nota in your project add the JitPack repository to your build file and a mod dependency. Replace the `VERSION_TAG` with the latest version, for example `0.1.0+1.19`

```gradle
repositories {
  maven { url = "https://jitpack.io" }
}

dependencies {
  modImplementation include("com.github.PinkGoosik:nota:VERSION_TAG")
}
```

#### How to get .nbs

- Download .nbs files, you can simply download a few songs [here](https://dev.bukkit.org/projects/icjukebox/pages/tracks).
- Convert .midi to .nbs with [Note Block Studio](https://github.com/HielkeMinecraft/OpenNoteBlockStudio/releases).
- Create a song in [Note Block Studio](https://github.com/HielkeMinecraft/OpenNoteBlockStudio/releases) and export it as .nbs file.

#### Playing songs

Song in .nbs format have to be loaded before you can use it.

```java
Song song = NBSDecoder.parse(new File("path/to/song.nbs"));
Song song2 = NBSDecoder.parse(new File("path/to/another/song.nbs"));
Playlist playlist = new Playlist(song, song2,...);
```

#### SongPlayer types
There are 3 types of SongPlayer:
- RadioSongPlayer
- PositionSongPlayer
- EntitySongPlayer

#### RadioSongPlayer
Plays song for all added players no matter where they are.

```java
Song song; // Preloaded song
RadioSongPlayer rsp = new RadioSongPlayer(song); // Create RadioSongPlayer.
rsp.setId(new Identifier("example:radio")); // Set unique identifier, not necessary
rsp.addPlayer(player); // Add player to SongPlayer so they will hear the song.
rsp.setPlaying(true); // Start RadioSongPlayer playback
```

#### PositionSongPlayer
Plays song for all added players in specified range from specified point.

```java
Song song; // Preloaded song
PositionSongPlayer psp = new PositionSongPlayer(song); // Create PositionSongPlayer.
psp.setId(new Identifier("example:position")); // Set unique identifier, not necessary
psp.setBlockPos(pos); // Set location where the song will be playing
psp.setDistance(16); // Set distance from target location in which players will hear the SongPlayer, default: 16
psp.addPlayer(player); // Add player to SongPlayer so they will hear the song.
psp.setPlaying(true); // Start PositionSongPlayer playback
```

#### EntitySongPlayer
Plays song for all added players in specified range from specified entity.

```java
Song song; // Preloaded song
EntitySongPlayer esp = new EntitySongPlayer(song); // Create EntitySongPlayer.
esp.setId(new Identifier("example:entity")); // Set unique identifier, not necessary
esp.setEntity(entity); // Set entity which position will be used
esp.setDistance(16); // Set distance from target location in which players will hear the SongPlayer, default: 16
esp.addPlayer(player); // Add player to SongPlayer so they will hear the song.
esp.setPlaying(true); // Start EntitySongPlayer playback
```
