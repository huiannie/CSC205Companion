
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

package edu.nvcc.manassas.csc205companion.tools.pipeline;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import edu.nvcc.manassas.csc205companion.app.AppSettings;


public abstract class PipelineSeekBar extends ImageView {
    private static final String TAG = PipelineSeekBar.class.getSimpleName() + "_class";
    private static final boolean debug = AppSettings.defaultDebug;

    public PipelineSeekBar(Context context) {
        super(context);
    }

    public PipelineSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PipelineSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public abstract void setPipeLine(Context context, Pipeline pipeline);


    public abstract boolean isNotifyWhileDragging();

    public abstract void setNotifyWhileDragging(boolean flag);


    /**
     * Callback listener interface to notify about changed range values.
     */
    public interface OnSeekBarChangeListener {
        public void onSeekBarValuesChanged(PipelineSeekBar bar);
    }

    public abstract void setOnSeekBarChangeListener(OnSeekBarChangeListener listener);
}
