/*
 * Copyright (c) 2015,2016 Annie Hui @ NVCC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.nvcc.manassas.csc205companion.tools.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Thumb {
    public final Bitmap image ;
    public final Bitmap pressedImage;
    public final float width;
    public final float height;
    public final float halfWidth;
    public final float halfHeight;
    public final float widthBound;
    public final float heightBound;

    public Thumb(Context context, int imageId, int pressedImageId) {
        // TODO: need to test the image ids
        image = BitmapFactory.decodeResource(context.getResources(), imageId);
        pressedImage = BitmapFactory.decodeResource(context.getResources(), pressedImageId);
        width = image.getWidth();
        height = image.getHeight();
        halfWidth = 0.5f * width;
        halfHeight = 0.5f * height;
        widthBound = 1.5f * width; // extra margin for locking on a thin thumb (double its effective bound)
        heightBound = 1.5f * height; // extra margin for a motion
    }

    public int getHeight() {
        if (image==null) return 0;
        else return image.getHeight();
    }

    public int getWidth() {
        if (image==null) return 0;
        else return image.getWidth();
    }

}
