package io.github.skunkworks1983.timeclock.ui;

import java.awt.Point;
import java.awt.Window;
import java.awt.image.BufferedImage;

public class CursorUtil
{
    public static void hideCursorInWindow(Window window)
    {
        //hide cursor (https://stackoverflow.com/a/10687248/5217216)
        window.setCursor(window.getToolkit().createCustomCursor(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB ),
                                                  new Point(),
                                                  null ));
    }
}
