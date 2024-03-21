package com.github.florent37.glidepalette;

import android.support.v4.util.Pair;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class PaletteTarget {

    @BitmapPalette.Profile
    protected int paletteProfile = GlidePalette.Profile.VIBRANT;

    protected List<Pair<View, Integer>> targetsBackground = new List<>();
    protected List<Pair<TextView, Integer>> targetsText = new List<>();

    protected boolean targetCrossfade = false;
    protected int targetCrossfadeSpeed = DEFAULT_CROSSFADE_SPEED;
    protected static final int DEFAULT_CROSSFADE_SPEED = 300;

    public PaletteTarget(@BitmapPalette.Profile int paletteProfile) {
        this.paletteProfile = paletteProfile;
    }

    public void clear() {
        targetsBackground.clear();
        targetsText.clear();

        targetsBackground = null;
        targetsText = null;

        targetCrossfade = false;
        targetCrossfadeSpeed = DEFAULT_CROSSFADE_SPEED;
    }
}
