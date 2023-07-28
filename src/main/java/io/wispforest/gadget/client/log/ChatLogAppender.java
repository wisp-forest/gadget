package io.wispforest.gadget.client.log;

import io.wispforest.gadget.mixin.client.ChatHudAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ChatLogAppender extends AbstractAppender {
    public static final ChatLogAppender INSTANCE = new ChatLogAppender();
    public static final MessageIndicator MESSAGE_INDICATOR = new MessageIndicator(
        0x0096FF,
        null,
        Text.translatable("chat.tag.gadget.loud_logging"),
        "Why are you seeing this?"
    );
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT);

    private final Layout<String> layout = PatternLayout.newBuilder()
        .withPattern("[%d{HH:mm:ss} %level] (%logger{1}) %msg{nolookups}")
        .build();
    private final Set<String> allowedLoggerNames = new HashSet<>();

    protected ChatLogAppender() {
        super("gadget:chat_log", null, null, false, null);
    }

    public static void init() {
        INSTANCE.start();
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addAppender(INSTANCE);
    }

    public Set<String> allowedLoggerNames() {
        return allowedLoggerNames;
    }

    private String shortenLoggerName(String orig) {
        var split = orig.split("\\.");

        for (int j = 0; j < split.length; j++) {
            String part = split[j];

            for (int i = 0; i < part.length(); ++i) {
                if (!Character.isLetterOrDigit(part.charAt(i))) {
                    return orig;
                }
            }

            if (j < split.length - 1) {
                split[j] = String.valueOf(part.charAt(0));
            }
        }

        return String.join(".", split);
    }

    private Text fromLogEvent(LogEvent event) {
        var formatDate = TIME_FORMATTER.format(
            Instant.ofEpochMilli(event.getInstant().getEpochMillisecond())
                .atZone(ZoneId.systemDefault())
        );

        return Text.translatable(
            "text.gadget.log_entry",
            formatDate,
            Text.translatable("text.gadget.log_level." + event.getLevel().getStandardLevel().name().toLowerCase(Locale.ROOT)),
            shortenLoggerName(event.getLoggerName()),
            event.getMessage().getFormattedMessage()
        );
    }

    @Override
    public void append(LogEvent event) {
        if (!allowedLoggerNames.contains(event.getLoggerName()))
            return;

        var text = fromLogEvent(event);
        var client = MinecraftClient.getInstance();

        client.execute(() -> {
            if (client.player == null) return;

            ((ChatHudAccessor) client.inGameHud.getChatHud())
                .callAddMessage(
                    text,
                    null,
                    client.inGameHud.getTicks(),
                    MESSAGE_INDICATOR,
                    false
                );
        });
    }
}
