package t.me.p1azmer.plugin.last_server.configuration.kyori;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

public enum Serializers {

    LEGACY_AMPERSAND("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer", "legacyAmpersand"),
    LEGACY_SECTION("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer", "legacySection"),
    MINIMESSAGE("net.kyori.adventure.text.minimessage.MiniMessage", "miniMessage"),
    GSON("net.kyori.adventure.text.serializer.gson.GsonComponentSerializer", "gson"),
    GSON_COLOR_DOWNSAMPLING("net.kyori.adventure.text.serializer.gson.GsonComponentSerializer", "colorDownsamplingGson"),
    PLAIN("net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer", "plainText");

    @Nullable
    private final ComponentSerializer<Component, Component, String> serializer;

    Serializers(String className, String methodName) {
        this.serializer = this.findSerializer(className, methodName);
    }

    /**
     * Used to prevent NoClassDefFoundError exception.
     *
     * @param className  The class name of the serializer holder.
     * @param methodName The method name that returns the serializer.
     * @return The {@link ComponentSerializer}, may be null.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    private ComponentSerializer<Component, Component, String> findSerializer(String className, String methodName) {
        try {
            return (ComponentSerializer<Component, Component, String>) Class.forName(className).getDeclaredMethod(methodName).invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            return null;
        }
    }

    @Nullable
    public ComponentSerializer<Component, Component, String> getSerializer() {
        return this.serializer;
    }
}