package com.jp.wasabeef.glide.transformations.internal;

import android.graphics.Bitmap;

/**
 * Copyright (C) 2015 Wasabeef
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class FastBlur {

    private FastBlur() {
        // This constructor is intentionnaly empty.
        // It's private to prevent instantiation of this class.
    }
    
    // Stack Blur v1.0 from
    // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
    //
    // Java Author: Mario Klingemann <mario at quasimondo.com>
    // http://incubator.quasimondo.com
    // created Feburary 29, 2004
    // Android port : Yahel Bouaziz <yahel at kayenko.com>
    // http://www.kayenko.com
    // ported april 5th, 2012

    // This is a compromise between Gaussian Blur and Box blur
    // It creates much better looking blurs than Box Blur, but is
    // 7x faster than my Gaussian Blur implementation.
    //
    // I called it Stack Blur because this describes best how this
    // filter works internally: it creates a kind of moving stack
    // of colors whilst scanning through the image. Thereby it
    // just has to add one new block of color to the right side
    // of the stack and remove the leftmost color. The remaining
    // colors on the topmost layer of the stack are either added on
    // or reduced by one, depending on if they are on the right or
    // on the left side of the stack.
    //
    // If you are using this algorithm in your code please add
    // the following line:
    //
    // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>
    public static Bitmap blur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {
        Bitmap bitmap = prepareBitmap(sentBitmap, canReuseInBitmap);
        int[] pix = bitmap.getPixels();
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
    
        int[] r = new int[w * h];
        int[] g = new int[w * h];
        int[] b = new int[w * h];
        int[] vmin = new int[Math.max(w, h)];
        int[][] data = new int[][]{r, g, b, vmin, pix};
    
        initializeArrays(pix, w, h, r, g, b, vmin);
        calculateSums(data, radius + radius + 1);
        setPixels(bitmap, pix, w, h);
    
        return bitmap;
    }
    
    private static Bitmap prepareBitmap(Bitmap sentBitmap, boolean canReuseInBitmap) {
        return canReuseInBitmap ? sentBitmap : sentBitmap.copy(sentBitmap.getConfig(), true);
    }
    
    private static void initializeArrays(int[] pix, int w, int h, int[] r, int[] g, int[] b, int[] vmin) {
        for (int i = 0; i < w * h; i++) {
            int p = pix[i];
            r[i] = (p >> 16) & 0xff;
            g[i] = (p >> 8) & 0xff;
            b[i] = p & 0xff;
        }
        // Initialize vmin array with the maximum possible value
        Arrays.fill(vmin, Integer.MAX_VALUE);
    }
    
    private static void calculateSums(int[][] data, int div) {
        int w = data[0].length;
        int h = data.length;
        int[] r = data[0];
        int[] g = data[1];
        int[] b = data[2];
    
        for (int i = 0; i < w * h; i++) {
            r[i] = r[i] / div;
            g[i] = g[i] / div;
            b[i] = b[i] / div;
        }
    }
    
    private static void setPixels(Bitmap bitmap, int[] pix, int w, int h) {
        for (int i = 0; i < w * h; i++) {
            int p = (0xff << 24) | (r[i] << 16) | (g[i] << 8) | b[i];
            pix[i] = p;
        }
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
    }
}