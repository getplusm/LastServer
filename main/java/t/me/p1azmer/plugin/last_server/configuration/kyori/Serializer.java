package t.me.p1azmer.plugin.last_server.configuration.kyori;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class Serializer implements ComponentSerializer<Component, Component, String> {

    private final ComponentSerializer<Component, Component, String> serializer;

    public Serializer(@NotNull ComponentSerializer<Component, Component, String> serializer) {
        this.serializer = serializer;
    }

    @NotNull
    @Override
    public Component deserialize(@NotNull String input) {
        return this.serializer.deserialize(input);
    }

    @NotNull
    @Override
    public String serialize(@NotNull Component component) {
        return this.serializer.serialize(component);
    }

    @NotNull
    public ComponentSerializer<Component, Component, String> getSerializer() {
        return this.serializer;
    }
}