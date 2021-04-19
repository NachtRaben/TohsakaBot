package dev.armadeus.bot.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import dev.armadeus.core.command.DiscordCommand;
import dev.armadeus.core.command.DiscordUser;
import dev.armadeus.core.util.EmbedUtils;
import dev.armadeus.core.util.TimeUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class UptimeCommand extends DiscordCommand {

    private static final RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();

    @Default
    @CommandAlias("uptime|up")
    public void uptime(DiscordUser user) {
        user.sendMessage(EmbedUtils.newBuilder(user)
                .setDescription("__**Uptime**__")
                .setFooter(TimeUtil.format(rb.getUptime()))
                .build());
    }

}
