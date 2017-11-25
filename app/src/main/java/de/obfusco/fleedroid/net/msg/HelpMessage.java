package de.obfusco.fleedroid.net.msg;

public class HelpMessage extends Message {
    private final boolean isHelpNeeded;

    @Override
    public String toString() {
        return "Hilfe wird " +
                (isHelpNeeded() ? "" : "nicht") +
                " benötigt";
    }

    private HelpMessage(boolean isHelpNeeded) {
        super();
        this.isHelpNeeded = isHelpNeeded;
    }

    public static HelpMessage parse(String data) {
        String[] parts = data.split("-");
        return new HelpMessage(parts.length == 2 && parts[1].equals("ON"));
    }

    public boolean isHelpNeeded() {
        return isHelpNeeded;
    }
}
