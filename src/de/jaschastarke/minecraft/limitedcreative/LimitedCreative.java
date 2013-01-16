package de.jaschastarke.minecraft.limitedcreative;

import de.jaschastarke.i18n;
import de.jaschastarke.bukkit.lib.Core;

public class LimitedCreative extends Core {
    private i18n lang;
    @Override
    public void OnInitialize() {
        lang = new i18n("lang/messages");
        addModule(new ModInventories(this));
        addModule(new ModCreativeLimits(this));
        addModule(new ModRegions(this));
        addModule(new ModCmdBlocker(this));
    }

    @Deprecated
    public String L(String msg, Object... objects) {
        return lang.trans(msg, objects);
    }
    public i18n getLocale() {
        return lang;
    }
}
