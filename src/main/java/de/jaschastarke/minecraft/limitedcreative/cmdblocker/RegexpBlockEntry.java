package de.jaschastarke.minecraft.limitedcreative.cmdblocker;

import java.util.regex.Pattern;

public class RegexpBlockEntry implements ICmdBlockEntry {
    private Pattern rx;
    public RegexpBlockEntry(String regex) {
        rx = Pattern.compile(regex);
    }

    @Override
    public boolean test(String cmd) {
        return rx.matcher(cmd).matches();
    }
    
    public String toString() {
        return rx.pattern();
    }
}
