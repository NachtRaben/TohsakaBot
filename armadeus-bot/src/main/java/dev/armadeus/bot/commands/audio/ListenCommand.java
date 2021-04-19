package dev.armadeus.bot.commands.audio;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import dev.armadeus.core.command.DiscordUser;
import dev.armadeus.core.managers.GuildMusicManager;
import dev.armadeus.core.util.TimeUtil;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.RichPresence;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static dev.armadeus.core.util.Utils.EXEC;

public class ListenCommand extends AudioCommand {

    @Conditions("developeronly|guildonly")
    @CommandAlias("listen")
    public void listen(DiscordUser user, @Default(value = "true") boolean listen) {
        GuildMusicManager manager = user.getGuildMusicManager();

        Future<?> future = manager.getListeners().get(user.getUser().getIdLong());
        if (future != null && !manager.getListeners().containsKey(user.getUser().getIdLong())) {
            user.sendMessage("Sorry, but I'm already listening to my Senpai~");
            return;
        } else if (future != null && !listen) {
            future.cancel(true);
            user.sendMessage("Sorry Senpai~, guess I'm not good enough to listen to you :c");
            return;
        } else if (future != null) {
            user.sendMessage("Senpai~ I'm already listening to UwU");
            return;
        }

        if (canInteractMusic(user)) {
            manager.listeners.put(user.getUser().getIdLong(), EXEC.submit(new ListenRunnable(user)));
            user.sendMessage("Now listening to you Senpai~ c:");
        }
    }

    private static class SpotifyPresence {

        private RichPresence presence;

        public SpotifyPresence(DiscordUser user) {
            Activity act = user.getMember().getActivities().stream().filter(a -> a.getName().equalsIgnoreCase("spotify")).findFirst().orElse(null);
            presence = act != null ? act.asRichPresence() : null;
            if (act == null || presence == null) {
                return;
            }
            if (presence.getTimestamps() == null) {
                act = null;
                presence = null;
            }
        }

        public String getTitle() {
            return presence.getDetails();
        }

        public String getAuthor() {
            return presence.getState();
        }

        public long getPosition() {
            return Objects.requireNonNull(presence.getTimestamps()).getElapsedTime(ChronoUnit.MILLIS);
        }

        public boolean isListening() {
            return presence != null;
        }
    }

    private class ListenRunnable implements Runnable {

        AtomicBoolean error = new AtomicBoolean(false);
        DiscordUser user;

        public ListenRunnable(DiscordUser user) {
            this.user = user;
        }

        @Override
        public void run() {
            GuildMusicManager manager = user.getGuildMusicManager();
            while (!Thread.interrupted() && !error.get()) {
                String botChannel = manager.getLink().getChannel();
                if (user.getVoiceChannel() == null || (botChannel != null && !user.getVoiceChannel().getId().equals(botChannel))) {
                    break;
                }

                SpotifyPresence presence = new SpotifyPresence(user);
                if (!presence.isListening()) {
                    break;
                }

                AudioTrack track = manager.getPlayer().getPlayingTrack();
                long position = track != null ? manager.getPlayer().getTrackPosition() : 0;
                if (track == null || !isCorrectTrack(presence.getTitle(), presence.getAuthor(), track.getInfo().title)) {
                    ListenCommand.this.logger.info("Changing track expected {} but got {}", presence.getTitle(), track == null ? "NULL" : track.getInfo().title);
                    manager.getLink().getRestClient().getYoutubeSearchResult(presence.getTitle() + " - " + presence.getAuthor()).thenAccept(tracks -> {
                        if (tracks.isEmpty()) {
                            error.set(true);
                            return;
                        }
                        boolean found = false;
                        for (AudioTrack t : tracks) {
                            if (isCorrectTrack(presence.getTitle(), presence.getAuthor(), t.getInfo().title)) {
                                found = true;
                                AudioPlaylist playlist = new BasicAudioPlaylist("Search Results", tracks, tracks.get(0), true);
                                GuildMusicManager.playlistLoaded(user, playlist, 1);
                                manager.getScheduler().skipTo(t);
                                manager.getPlayer().seekTo(presence.getPosition());
                                break;
                            }
                        }
                        if (!found) {
                            error.set(true);
                        }
                    });
                } else if (Math.abs(presence.getPosition() - position) > 5000) {
                    ListenCommand.this.logger.info("We detected drift, expected {} but got {}", TimeUtil.format(presence.getPosition()), TimeUtil.format(position));
                }
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException ignored) {
                }
            }
            if (error.get()) {
                user.sendMessage("Sorry Senpai~, I lost you :c");
                manager.listeners.remove(user.getUser().getIdLong());
            }
        }

        private boolean isCorrectTrack(String title, String author, String trackTitle) {
            List<String> expected = Arrays.stream((title + " " + author).toLowerCase().split(" ")).map(s -> s.replaceAll("[^a-zA-Z\\d\\s]", "")).collect(Collectors.toList());
            List<String> tokens = Arrays.stream(trackTitle.toLowerCase().split(" ")).map(s -> s.replaceAll("[^a-zA-Z\\d\\s]", "")).collect(Collectors.toList());
            int matches = 0;
            for (String token : tokens) {
                for (String exp : expected) {
                    if (token.equals(exp))
                        matches++;
                }
            }
            return (float) matches >= (0.4f * expected.size());
        }
    }
}
