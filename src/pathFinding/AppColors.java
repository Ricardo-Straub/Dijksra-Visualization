package pathFinding;

import java.awt.*;

public enum AppColors {
    blue(0x829ED0), red(0x975767), green(0x006761),
    darkGray(0x404756), lightGray(0xe6e6e6), lightGray2(0xd4d4d4),
    white(Color.white.getRGB()), purple(0x966990), yellow(0xfccc72);

    private final int color;

    private AppColors(int color) {
        this.color = color;
    }

    public Color getColorObject() {
        return new Color(color);
    }
}
