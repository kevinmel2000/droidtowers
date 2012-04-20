package com.happydroids.droidtowers;

import com.badlogic.gdx.graphics.Color;

import static java.lang.Integer.valueOf;

public class ColorUtil {
  public static Color rgba(String colorStr) {
    if (colorStr.charAt(0) == '#') {
      colorStr = colorStr.substring(1);
    }

    if (colorStr.length() < 8) {
      colorStr = colorStr + "FF";
    }

    return new Color(valueOf(colorStr.substring(0, 2), 16) / 255f,
                            valueOf(colorStr.substring(2, 4), 16) / 255f,
                            valueOf(colorStr.substring(4, 6), 16) / 255f,
                            valueOf(colorStr.substring(6, 8), 16) / 255f);
  }
}
