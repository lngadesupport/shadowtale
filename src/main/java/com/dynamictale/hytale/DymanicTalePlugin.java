package com.dynamictale.hytale;

import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import javax.annotation.Nonnull;

/** Independent Hytale camera plugin. It contains no graphics-mod dependency. */
public final class DymanicTalePlugin extends JavaPlugin {
    private CameraInputWatcher inputWatcher;

    public DymanicTalePlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        inputWatcher = new CameraInputWatcher();
        PacketAdapters.registerInbound(inputWatcher);
    }

    public CameraInputWatcher inputWatcher() {
        return inputWatcher;
    }
}
