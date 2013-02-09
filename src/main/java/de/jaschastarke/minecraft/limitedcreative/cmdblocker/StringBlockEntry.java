package de.jaschastarke.minecraft.limitedcreative.cmdblocker;

public class StringBlockEntry implements ICmdBlockEntry {
    private String str;
    public StringBlockEntry(String cmd) {
        str = cmd;
    }

    @Override
    public boolean test(String cmd) {
        return cmd.startsWith(this.str);
    }
    
    public String toString() {
        return str;
    }
}
