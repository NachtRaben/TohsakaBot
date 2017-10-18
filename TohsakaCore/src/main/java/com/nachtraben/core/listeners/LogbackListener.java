package com.nachtraben.core.listeners;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import com.nachtraben.core.DiscordBot;
import com.nachtraben.core.util.DateTimeUtil;
import com.nachtraben.core.util.Utils;
import com.nachtraben.core.util.WebhookLogger;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.Date;


public class LogbackListener<E> extends AppenderBase<E> {

    private DiscordBot bot;

    public LogbackListener(DiscordBot bot) {
        this.bot = bot;
    }

    @Override
    protected void append(E eventObject) {
        if (eventObject instanceof ILoggingEvent) {
            ILoggingEvent event = (ILoggingEvent) eventObject;
            WebhookLogger logger = bot.getWlogger();
            Level level = event.getLevel();
            if (level.isGreaterOrEqual(Level.ERROR)) {
                TextChannel channel = bot.getConfig().getErrorLogChannel();
                if (channel != null) {
                    try {
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setDescription(String.format("***[%s]: %s***", level.levelStr, event.getMessage().substring(0, Math.min(event.getMessage().length(), MessageEmbed.TEXT_MAX_LENGTH))));
                        eb.setColor(Utils.randomColor());
                        if(logger != null)
                            logger.send(String.format("[%s][%s][%s]: %s", DateTimeUtil.formatTime(event.getTimeStamp()), level.levelStr, name, event.getFormattedMessage()));
                        if (event.getThrowableProxy() != null) {
                            String throwable = getStackTrace(event);
                            if(logger != null)
                                logger.send(String.format("[%s][%s][%s]: %s", DateTimeUtil.formatTime(event.getTimeStamp()), level.levelStr, name, throwable));
                            eb.addField("Stack:", throwable.substring(0, Math.min(throwable.length(), MessageEmbed.VALUE_MAX_LENGTH - eb.getDescriptionBuilder().length())), false);
                        }
                        eb.setFooter(new Date(event.getTimeStamp()).toString(), null);
                        channel.sendMessage(eb.build()).queue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if(logger != null) {
                    String name = event.getLoggerName().contains(".") ? event.getLoggerName().substring(event.getLoggerName().lastIndexOf(".") + 1, event.getLoggerName().length()) : event.getLoggerName();
                    logger.send(String.format("[%s][%s][%s]: %s", DateTimeUtil.formatTime(event.getTimeStamp()), level.levelStr, name, event.getFormattedMessage()));
                    if (event.getThrowableProxy() != null) {
                        String throwable = getStackTrace(event);
                        logger.send(String.format("[%s][%s][%s]: %s", DateTimeUtil.formatTime(event.getTimeStamp()), level.levelStr, name, throwable));
                    }
                }
            }
        }
    }

    private String getStackTrace(ILoggingEvent event) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ThrowableProxy e = (ThrowableProxy) event.getThrowableProxy();
        if(e.getCause() != null)
            e = (ThrowableProxy) e.getCause();
        e.getThrowable().printStackTrace(pw);
        return sw.toString();
    }

    public static void install(DiscordBot bot) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setPattern("%msg");
        ple.setContext(lc);
        ple.start();
        LogbackListener<ILoggingEvent> listener = new LogbackListener<>(bot);
        listener.setContext(lc);
        listener.start();
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).addAppender(listener);
    }

}