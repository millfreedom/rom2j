package ua.millfreedom.rom2.console;

import static ua.millfreedom.rom2.model.color.Utils.clamp255;

public class Utils {
    public static String ansi(String value) {
        return "\u001B[" + value + "m";
    }
    public static String color24(int r, int g, int b)
    {
        return ansi("38;2;" + clamp255(r) + ";" + clamp255(g) + ";" + clamp255(b));
    }
    public static String colorReset()
    {
        return ansi("39");
    }

}
