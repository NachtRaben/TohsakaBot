package com.nachtraben.core.command;

import com.nachtraben.core.DiscordBot;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;

public class PrivateCommandSender extends DiscordCommandSender {

    private PrivateChannel privateChannel;

    public PrivateCommandSender(DiscordBot dbot, Message message) {
        super(dbot, message);
        privateChannel = message.getPrivateChannel();
    }

    public PrivateChannel getPrivateChannel() {
        if(privateChannel == null) privateChannel = getUser().openPrivateChannel().complete();
        return privateChannel;
    }

    @Override
    public void sendMessage(String message) {
        getPrivateChannel().sendMessage(message).queue();
    }
}
