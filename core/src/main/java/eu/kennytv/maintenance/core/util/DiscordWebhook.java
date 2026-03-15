package eu.kennytv.maintenance.core.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import eu.kennytv.maintenance.core.Settings;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class DiscordWebhook {

    private static final int COLOR_MAINTENANCE_ENABLED = 0xED4245; // Red
    private static final int COLOR_MAINTENANCE_DISABLED = 0x57F287; // Green
    private static final int COLOR_STARTTIMER = 0xFEE75C; // Yellow
    private static final int COLOR_ENDTIMER = 0x5865F2; // Blue
    private static final int COLOR_BLOCKED_JOIN = 0xE67E22; // Orange

    public static void sendMessage(final String message, final EventType event, final Settings settings) throws IOException, InterruptedException {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("username", settings.getWebhookUsername());

        final String avatarUrl = settings.getWebhookAvatarUrl();
        if (!avatarUrl.isEmpty()) {
            jsonObject.addProperty("avatar_url", avatarUrl);
        }

        final JsonObject embed = new JsonObject();
        embed.addProperty("description", message);
        embed.addProperty("color", getColorForEvent(event));

        final JsonArray embeds = new JsonArray();
        embeds.add(embed);
        jsonObject.add("embeds", embeds);

        try (final HttpClient client = HttpClient.newHttpClient()) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(settings.getWebhookUrl()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
                    .build();
            client.send(request, HttpResponse.BodyHandlers.discarding());
        }
    }

    private static int getColorForEvent(final EventType event) {
        return switch (event) {
            case MAINTENANCE_ENABLED -> COLOR_MAINTENANCE_ENABLED;
            case MAINTENANCE_DISABLED -> COLOR_MAINTENANCE_DISABLED;
            case STARTTIMER_STARTED -> COLOR_STARTTIMER;
            case ENDTIMER_STARTED -> COLOR_ENDTIMER;
            case BLOCKED_JOIN -> COLOR_BLOCKED_JOIN;
        };
    }

    public enum EventType {
        MAINTENANCE_ENABLED("maintenance_status"),
        MAINTENANCE_DISABLED("maintenance_status"),
        STARTTIMER_STARTED("starttimer_started"),
        ENDTIMER_STARTED("endtimer_started"),
        BLOCKED_JOIN("blocked_join");

        private final String configKey;

        EventType(final String configKey) {
            this.configKey = configKey;
        }

        public String configKey() {
            return configKey;
        }
    }
}
