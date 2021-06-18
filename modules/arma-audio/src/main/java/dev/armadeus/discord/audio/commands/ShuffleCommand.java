package dev.armadeus.discord.audio.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;

public class ShuffleCommand extends AudioCommand {

    @Conditions("guildonly")
    @CommandAlias("shuffle")
    @CommandPermission("armadeus.shuffle")
    public void shuffle(DiscordCommandIssuer user) {
        if (cannotQueueMusic(user))
            return;

        if (isNotPlaying(user))
            return;

        getAudioManager(user).getPlayer().getScheduler().shuffle();
        user.sendMessage("The queue has been shuffled! ~owo~");
    }
}
