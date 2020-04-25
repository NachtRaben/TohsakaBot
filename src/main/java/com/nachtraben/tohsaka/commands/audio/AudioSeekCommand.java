package com.nachtraben.tohsaka.commands.audio;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.managers.GuildMusicManager;
import com.nachtraben.core.util.TimeUtil;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;
import com.nachtraben.tohsaka.Tohsaka;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.Arrays;
import java.util.Map;

public class AudioSeekCommand extends Command {

    public AudioSeekCommand() {
        super("seek", "<time>", "Buffers/Scans to a certain point in the current track.");
        setAliases(Arrays.asList("scan", "buffer"));
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            GuildMusicManager manager = Tohsaka.getInstance().getGuildManager().getConfigurationFor(sendee.getGuild().getIdLong()).getMusicManager();

            String time = args.get("time");

            AudioTrack currentTrack = manager.getPlayer().getPlayingTrack();
            if (currentTrack == null) {
                sender.sendMessage("You have to queue up music in order to seek in a song.");
                return;
            }

            if (!currentTrack.isSeekable()) {
                sender.sendMessage("I cannot seek in a stream.");
            }

            boolean seek = time.startsWith("+") || time.startsWith("-");
            long position = Math.abs(TimeUtil.stringToMillis(time.replaceAll("[+-]", "")));

            if (seek) {
                position = time.startsWith("+") ? currentTrack.getPosition() + position : currentTrack.getPosition() - position;
            }
            position = Math.max(0, Math.min(position, currentTrack.getDuration()));

            sender.sendMessage("Seeking to `" + TimeUtil.fromLong(position, TimeUtil.FormatType.TIME) + "/" + TimeUtil.fromLong(currentTrack.getDuration(), TimeUtil.FormatType.TIME) + "`");
            manager.getPlayer().seekTo(position);
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }

}
