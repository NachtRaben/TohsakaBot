package com.nachtraben.tohsaka.commands;

import com.nachtraben.core.util.TimedCache;
import com.nachtraben.core.command.DiscordCommandSender;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.core.util.Utils;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;
import com.nachtraben.tohsaka.ImageRating;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.core.EmbedBuilder;
import net.kodehawa.lib.imageboards.entities.YandereImage;
import net.kodehawa.lib.imageboards.util.Imageboards;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class YandereCommand extends Command {

    private static final Random RAND = new Random();
    private static final Logger LOGGER = LoggerFactory.getLogger(YandereCommand.class);
    private TimedCache<Long, Set<String>> guildSearchCache = new TimedCache<>(TimeUnit.MINUTES.toMillis(30), TimedCache.TimeoutPolicy.ACCESS);
    private TimedCache<Long, Set<String>> userSearchCache = new TimedCache<>(TimeUnit.MINUTES.toMillis(30), TimedCache.TimeoutPolicy.ACCESS);


    private final Random r = new Random();

    public YandereCommand() {
        super("yandere", "(tag)", "Gets an image from Yandere");
        super.setAliases(Arrays.asList("yande.re", "yand", "yd"));
        super.setFlags(Arrays.asList("--safe", "--questionable", "--explicit", "-seq"));
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof DiscordCommandSender) {
            DiscordCommandSender sendee = (DiscordCommandSender) sender;
            GuildCommandSender gcs = sendee instanceof GuildCommandSender ? (GuildCommandSender) sendee : null;

            boolean safe = flags.containsKey("safe") || flags.containsKey("s");
            boolean questionable = flags.containsKey("questionable") || flags.containsKey("q");
            boolean explicit = flags.containsKey("explicit") || flags.containsKey("e");
            boolean isSearch = args.containsKey("tag");

            ImageRating rating = gcs != null && gcs.getTextChannel().isNSFW() ? ImageRating.EXPLICIT : ImageRating.SAFE;
            if (explicit && questionable)
                rating = ImageRating.QANDE;
            else if (explicit)
                rating = ImageRating.EXPLICIT;
            else if (questionable)
                rating = ImageRating.QUESTIONABLE;
            else if (safe)
                rating = ImageRating.SAFE;

            if (gcs != null && !gcs.getTextChannel().isNSFW() && !rating.equals(ImageRating.SAFE)) {
                gcs.sendMessage(ChannelTarget.GENERIC, "Sorry, " + gcs.getMember().getAsMention() + " but I can't satisfy that desire in here. " + EmojiParser.parseToUnicode(":cry:"));
                return;
            }

            ImageRating finalRating = rating;
            List<YandereImage> images = isSearch ? Imageboards.YANDERE.onSearchBlocking(100, args.get("tag").replace(" ", "_")) : Imageboards.YANDERE.getBlocking(RAND.nextInt(1024));
            if (images != null) {
                Set<String> cache = gcs != null ? guildSearchCache.computeIfAbsent(gcs.getGuildId(), set -> new HashSet<>()) : userSearchCache.computeIfAbsent(sendee.getUserID(), set -> new HashSet<>());
                images = images.stream().filter(image -> finalRating.matches(image.getRating().toLowerCase()) && image.getTags().stream().noneMatch(tag -> tag.equalsIgnoreCase("loli"))).filter(image -> !cache.contains(image.getImageUrl())).collect(Collectors.toList());
                if (images.isEmpty()) {
                    if (isSearch) {
                        if (!rating.equals(ImageRating.SAFE))
                            sendee.sendMessage(ChannelTarget.NSFW, "Sorry, but I didn't find anything for the tag, `" + args.get("tag") + "`.");
                        else
                            sendee.sendMessage(ChannelTarget.GENERIC, "Sorry, but I didn't find anything for the tag, `" + args.get("tag") + "`.");
                    } else {
                        if (!rating.equals(ImageRating.SAFE))
                            sendee.sendMessage(ChannelTarget.NSFW, "Sorry, but I wasn't able to find an image.");
                        else
                            sendee.sendMessage(ChannelTarget.GENERIC, "Sorry, but I wasn't able to find an image.");
                    }
                    return;
                }
                YandereImage selection = images.get(RAND.nextInt(images.size()));
                cache.add(selection.getImageUrl());
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(Utils.randomColor());
                eb.setAuthor(isSearch ? args.get("tag") : "Yandere", selection.getImageUrl(), null);
//                eb.setDescription("[link](" + selection.getImageUrl() + ")");
                eb.setImage(selection.getImageUrl());
                eb.setFooter("Requested by: " + (gcs != null ? gcs.getMember().getEffectiveName() : sendee.getName()), sendee.getUser().getAvatarUrl());
                if (finalRating.equals(ImageRating.SAFE))
                    sendee.sendMessage(ChannelTarget.GENERIC, eb.build());
                else
                    sendee.sendMessage(ChannelTarget.NSFW, eb.build());
            } else {
                // TODO: Query error, could be invalid page, could be an actual outage. Re-Query?
                sendee.sendMessage("Sorry, but I was unable to query the Yande.re, try again later.");
            }
        }
    }
}
