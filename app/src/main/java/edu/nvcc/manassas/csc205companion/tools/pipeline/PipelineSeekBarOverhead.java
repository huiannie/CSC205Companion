
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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import edu.nvcc.manassas.csc205companion.R;
import edu.nvcc.manassas.csc205companion.app.AppSettings;
import edu.nvcc.manassas.csc205companion.io.Savelog;
import edu.nvcc.manassas.csc205companion.tools.util.PixelUtil;
import edu.nvcc.manassas.csc205companion.tools.util.Thumb;


public class PipelineSeekBarOverhead extends PipelineSeekBar {
    private static final String TAG = PipelineSeekBarOverhead.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    private static final int HEIGHT_IN_DP = 30;
    private static final int TEXT_LATERAL_PADDING_IN_DP = 3;
    private static final int INITIAL_PADDING_IN_DP = 8;
    private static final int LINE_WIDTH_IN_DP = 1;
    private static final double HEIGHT_FACTOR = 6.5;
    private static final int DEFAULT_TEXT_SIZE_IN_DP = 14;
    private static final int DEFAULT_TEXT_DISTANCE_TO_BUTTON_IN_DP = 8;
    private static final int DEFAULT_TEXT_DISTANCE_TO_TOP_IN_DP = 8;


    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Thumb thumb;

    private float mLineWidth = 0;
    private float INITIAL_PADDING;
    private float padding;

    private Pipeline pipeline;
    private int numberOfThumbs = 0;
    private int pressedThumbs[] = null;
    private boolean pressedOverheadThumb = false;

    private boolean notifyWhileDragging = false;
    private OnSeekBarChangeListener listener;


    /**
     * An invalid pointer id.
     */
    public static final int INVALID_POINTER_ID = 255;

    public static final int ACTION_POINTER_INDEX_MASK = 0x0000ff00, ACTION_POINTER_INDEX_SHIFT = 8;

    private float mDownMotionX;
    private float mDownMotionY;
    private float mLastX;

    private int mActivePointerId = INVALID_POINTER_ID;

    private int mScaledTouchSlop;

    private boolean mIsDragging;

    private int mTextOffset;
    private int mTextSize;
    private int mDistanceToTop;

    private float mPipelineOffset;
    private float mOverheadOffset;
    private RectF mTasksRect;
    private RectF mPipelineRect;
    private RectF mVerticalBarRect;
    private RectF mPipelineStageRect;
    private RectF mOverheadRect;
    private RectF mOverheadFractRect;


    public PipelineSeekBarOverhead(Context context) {
        super(context);
        init(context, new Pipeline());
    }
    public PipelineSeekBarOverhead(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, new Pipeline());
    }
    public PipelineSeekBarOverhead(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, new Pipeline());
    }

    public PipelineSeekBarOverhead(Context context, Pipeline pipeline) {
        super(context);
        if (pipeline==null) pipeline = new Pipeline();
        init(context, pipeline);
    }



    private void init(Context context, Pipeline pipe) {
        this.pipeline = pipe;

        this.numberOfThumbs = this.pipeline.getNumberOfMarkers();
        thumb = new Thumb(context, R.mipmap.seek_thumb_tight, R.mipmap.seek_thumb_tight);
        pressedThumbs = null;
        pressedOverheadThumb = false;

        INITIAL_PADDING = PixelUtil.dpToPx(context, INITIAL_PADDING_IN_DP);

        mTextSize = PixelUtil.dpToPx(context, DEFAULT_TEXT_SIZE_IN_DP);
        mDistanceToTop = PixelUtil.dpToPx(context, DEFAULT_TEXT_DISTANCE_TO_TOP_IN_DP);
        mTextOffset = mTextSize + PixelUtil.dpToPx(context, DEFAULT_TEXT_DISTANCE_TO_BUTTON_IN_DP) + mDistanceToTop;

        mLineWidth = PixelUtil.dpToPx(context, LINE_WIDTH_IN_DP);

        // seekbar components in color

        mTasksRect = new RectF(padding,  mTextOffset,  getWidth() - padding,  mTextOffset+thumb.height );
        // pipeline background
        mPipelineOffset = thumb.height/2;
        mPipelineRect = new RectF(padding,  mTasksRect.bottom+mPipelineOffset,  getWidth()-padding,  mTasksRect.bottom+mPipelineOffset+thumb.height*2);
        // a vertical bar partitions the stages of the pipeline
        mVerticalBarRect = new RectF(0,  mPipelineRect.top, 0,  mPipelineRect.bottom);
        // each stage of the pipeline in color
        mPipelineStageRect = new RectF(padding,  mPipelineRect.top+mTextOffset,  getWidth()-padding,  mPipelineRect.bottom);
        // fixed cost of overhead

        mOverheadOffset = mPipelineRect.bottom+mTextOffset+mTextSize;
        mOverheadRect = new RectF(padding,  mOverheadOffset,  getWidth()-padding,  mOverheadOffset+thumb.height);
        mOverheadFractRect = new RectF(mOverheadRect.left, mOverheadRect.top, mOverheadRect.right, mOverheadRect.bottom);

        // make SeekBar focusable. This solves focus handling issues in case EditText widgets are being used along with the SeekBar within ScollViews.
        setFocusable(true);
        setFocusableInTouchMode(true);
        mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }


    public void setPipeLine(Context context, Pipeline pipeline) {
        if (pipeline!=null) {
            if (pipeline instanceof Pipeline) {
                init(context, (Pipeline) pipeline);
                // must refresh after change
                invalidate();
            } // else, ignore request
        }
    }

    public boolean isNotifyWhileDragging() {
        return notifyWhileDragging;
    }
    public void setNotifyWhileDragging(boolean flag) {
        this.notifyWhileDragging = flag;
    }


    public double getSelectedValue(int pos) {
        return pipeline.getMarker(pos);
    }
    public double[] getSelectedValues() {
        return pipeline.getMarkers();
    }



    /**
     * Handles thumb selection and movement. Notifies listener callback on certain events.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!isEnabled()) {
            return false;
        }

        int pointerIndex;

        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                // Remember where the motion event started
                mActivePointerId = event.getPointerId(event.getPointerCount() - 1);
                pointerIndex = event.findPointerIndex(mActivePointerId);
                mDownMotionX = event.getX(pointerIndex);
                mDownMotionY = event.getY(pointerIndex);
                // No last value
                mLastX = -1;

                // Give priority to the thumbs on the pipeline
                pressedThumbs = evalPressedPipelineThumb(mDownMotionX, mDownMotionY);
                if (pressedThumbs==null) { // if no thumbs on the pipeline are pressed, then consider the overhead thumb
                    pressedOverheadThumb = evalPressedOverheadThumb(mDownMotionX, mDownMotionY);
                }

                // Only handle thumb presses.
                if (pressedThumbs == null && pressedOverheadThumb==false) {
                    return super.onTouchEvent(event);
                }


                setPressed(true);
                invalidate();
                onStartTrackingTouch();
                trackTouchEvent(event);
                attemptClaimDrag();

                break;
            case MotionEvent.ACTION_MOVE:
                if (pressedThumbs != null || pressedOverheadThumb) {
                    if (mIsDragging) {
                        if (pressedThumbs!=null) {
                            // may lose the thumb when Y is out
                            if (!isInPipelineThumbRangeY(event.getY())) {
                                onStopTrackingTouch();
                                setPressed(false);
                                pressedThumbs = null;
                                invalidate();
                            }
                            else {
                                trackTouchEvent(event);
                            }
                        }
                        else if (pressedOverheadThumb) {
                            // may lose the thumb when Y is out
                            if (!isInOverheadThumbRangeY(event.getY())) {
                                onStopTrackingTouch();
                                setPressed(false);
                                pressedOverheadThumb = false;
                                invalidate();
                            }
                            else {
                                trackTouchEvent(event);
                            }
                        }
                    } else {
                        // Scroll to follow the motion event
                        pointerIndex = event.findPointerIndex(mActivePointerId);
                        final float x = event.getX(pointerIndex);

                        if (Math.abs(x - mDownMotionX) > mScaledTouchSlop) {
                            setPressed(true);
                            invalidate();
                            onStartTrackingTouch();
                            trackTouchEvent(event);
                            attemptClaimDrag();
                        }
                    }

                    if (notifyWhileDragging && listener != null) {
                        listener.onSeekBarValuesChanged(this);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(false);
                } else {
                    // Touch up when we never crossed the touch slop threshold
                    // should be interpreted as a tap-seek to that location.
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                }

                pressedThumbs = null;
                pressedOverheadThumb = false;
                invalidate();
                if (listener != null) {
                    listener.onSeekBarValuesChanged(this);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = event.getPointerCount() - 1;
                mDownMotionX = event.getX(index);
                mDownMotionY = event.getY(index);
                mActivePointerId = event.getPointerId(index);
                invalidate();
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsDragging) {
                    onStopTrackingTouch();
                    setPressed(false);
                }
                invalidate(); // see above explanation
                break;
        }
        return true;
    }

    private final void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & ACTION_POINTER_INDEX_MASK) >> ACTION_POINTER_INDEX_SHIFT;

        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose
            // a new active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mDownMotionX = ev.getX(newPointerIndex);
            mDownMotionY = ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }



    private final void trackTouchEvent(MotionEvent event) {
        final int pointerIndex = event.findPointerIndex(mActivePointerId);
        final float x = event.getX(pointerIndex);
        final float y = event.getY(pointerIndex);

        int direction = 0;
        if (mLastX!=-1) {
            if (x>mLastX) direction = 1; // positive, going right
            else if (x<mLastX) direction = -1; // negative, going left
            else direction = 0;
            // else, no horizontal motion
        }

        // When tracking, give priority to the pipeline thumbs
        if (pressedThumbs !=null) {

            if (pressedThumbs.length==1) {
                Savelog.d(TAG, debug, "one thumb=" + pressedThumbs[0]);

                // Only 1 thumb. Move that one.
                setNormalizedPipelineValue(pressedThumbs[0], screenToNormalized(x));
            }
            else {

                // need to decide which one to move
                int first = pressedThumbs[0];
                int last = pressedThumbs[pressedThumbs.length-1];


                double minX = pipeline.getMarker(first);
                double maxX = pipeline.getMarker(last);
                Savelog.d(TAG, debug, "first thumb=" + first + " last thumb=" + last);
                Savelog.d(TAG, debug, "first thumb X=" + minX + " last thumb X=" + maxX);

                double normalizedX = screenToNormalized(x);
                int moved = 0;
                boolean hasMoved = false;

                if (normalizedX < minX && direction!=1) {   // direction may be 0 or -1
                    Savelog.d(TAG, debug, "going left. Move " + first);
                    // choose the first. Reduce the choices to 1
                    setNormalizedPipelineValue(first, normalizedX);
                    moved = first;
                    hasMoved = true;
                }
                else if (normalizedX > maxX && direction!=-1) {  // direction may be 0 or 1
                    Savelog.d(TAG, debug, "going right. Move " + last);
                    // choose the last. Reduce the choices to 1
                    setNormalizedPipelineValue(last, normalizedX);
                    moved = last;
                    hasMoved = true;
                }
                else {
                    Savelog.d(TAG, debug, "Move the closest.");
                    double distanceX[] = new double[pressedThumbs.length];
                    double minDistanceX = 0;
                    int minPos = 0;
                    for (int index=0; index<pressedThumbs.length; index++) {
                        distanceX[index] = Math.abs(getSelectedValue(pressedThumbs[index]) - normalizedX);
                        if (index==0) {
                            minDistanceX = distanceX[index];
                            minPos = pressedThumbs[index];
                        }
                        else {
                            if (minDistanceX>distanceX[index]) {
                                minDistanceX = distanceX[index];
                                minPos = pressedThumbs[index];
                            }
                        }
                    }

                    setNormalizedPipelineValue(minPos, normalizedX);
                    moved = minPos;
                    hasMoved = true;

                }
                if (hasMoved) {
                   // Now try to re-calculate the thumbs. Make sure that we at least keep 1
                    // Lose others if necessary

                    int newPressedThumb[] = evalPressedPipelineThumb(x, y);
                    if (newPressedThumb==null) {
                        pressedThumbs = new int[1];
                        pressedThumbs[0] = moved;
                    }
                    else {
                        pressedThumbs = newPressedThumb;
                    }
                }

            }
        }
        else if (pressedOverheadThumb) {
            // if no thumbs on pipeline are tracked, then track the overhead thumb
            setNormalizedOverheadValue(screenToNormalized(x));
        }

        // Now record the down location has the last location
        mLastX = x;
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any ancestors from stealing events in the drag.
     */
    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    /**
     * This is called when the user has started touching this widget.
     */
    void onStartTrackingTouch() {
        mIsDragging = true;
    }

    /**
     * This is called when the user either releases his touch or the touch is canceled.
     */
    void onStopTrackingTouch() {
        mIsDragging = false;
    }

    /**
     * Ensures correct size of the widget.
     */
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 200;
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }

        int height = (int)(thumb.image.getHeight()*HEIGHT_FACTOR) + PixelUtil.dpToPx(getContext(), HEIGHT_IN_DP);


        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
            height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
        }
        setMeasuredDimension(width, height);
    }

    /**
     * Draws the widget on the given canvas.
     */
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Savelog.d(TAG, debug, "onDraw() #nodes=" + numberOfThumbs);

        paint.setTextSize(mTextSize);
        paint.setStyle(Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);

        {
            // draw start and end labels on seekbar
            String minTaskLabel = String.format("%.0f", pipeline.DEFAULT_MINIMUM);
            String maxTaskLabel = String.format("%.0f", pipeline.DEFAULT_MAXIMUM);
            float minMaxLabelSize = Math.max(paint.measureText(minTaskLabel), paint.measureText(maxTaskLabel));

            float minMaxHeight = mTasksRect.bottom;
            canvas.drawText(minTaskLabel, 0, minMaxHeight, paint);
            canvas.drawText(maxTaskLabel, getWidth() - minMaxLabelSize, minMaxHeight, paint);

            padding = INITIAL_PADDING + minMaxLabelSize + thumb.halfWidth;
        }


        // draw seek bar active range line
        for (int stage=0; stage<=numberOfThumbs; stage++) {
            if (stage==0)
                mTasksRect.left = normalizedToScreen(Pipeline.DEFAULT_MINIMUM);
            else
                mTasksRect.left = normalizedToScreen(pipeline.getMarker(stage-1));

            if (stage==numberOfThumbs)
                mTasksRect.right = normalizedToScreen(Pipeline.DEFAULT_MAXIMUM);
            else
                mTasksRect.right = normalizedToScreen(pipeline.getMarker(stage));

            paint.setColor(pipeline.getColor(stage));
            canvas.drawRect(mTasksRect, paint);

            Savelog.d(TAG, debug, "Task left=" + mTasksRect.left + " top=" + mTasksRect.top + " bottom=" + mTasksRect.bottom + " right=" + mTasksRect.right);
        }






        // draw thumbs
        for (int pos=0; pos<numberOfThumbs; pos++) {

            boolean pressed = false;
            // There may be more than one thumb pressed
            if (pressedThumbs !=null) {
                for (int index = 0; index < pressedThumbs.length; index++)
                    if (pressedThumbs[index] == pos) pressed = true;
            }
            drawPipelineThumb(normalizedToScreen(pipeline.getMarker(pos)), pressed, canvas);
        }

        // draw pipeline
        mPipelineRect.left = normalizedToScreen(Pipeline.DEFAULT_MINIMUM);
        mPipelineRect.right = normalizedToScreen(Pipeline.DEFAULT_MAXIMUM);
        Savelog.d(TAG, debug, "pipeline box left=" + mPipelineRect.left + " top=" + mPipelineRect.top + " right=" + mPipelineRect.right + " bottom=" + mPipelineRect.bottom);
        paint.setColor(Color.GRAY);
        canvas.drawRect(mPipelineRect, paint);



        // give text a bit more space here so it doesn't get cut off
        int offset = PixelUtil.dpToPx(getContext(), TEXT_LATERAL_PADDING_IN_DP);


        // draw stages on pipeline
        for (int stage=0; stage<=numberOfThumbs; stage++) {
            // The fixed part of utilization
            double overheadStart = pipeline.getStageRelativeWidth()*(stage);
            double overheadLength = pipeline.getStageRelativeWidth()*pipeline.getStageMinUtilization();
            double overheadEnd = overheadStart + overheadLength;
            mPipelineStageRect.left = normalizedToScreen(overheadStart);
            mPipelineStageRect.right = normalizedToScreen(overheadEnd);
            paint.setColor(pipeline.getOverheadColor());
            canvas.drawRect(mPipelineStageRect, paint);


            // The variable part of utilization
            double stageStart = pipeline.getStageRelativeWidth()*(stage) + pipeline.getStageRelativeWidth()*pipeline.getStageMinUtilization();
            double stageLength = pipeline.getStageRelativeWidth()*(pipeline.getStageUtilization(stage)-pipeline.getStageMinUtilization());
            double stageEnd = stageStart + stageLength;

            // reuse this rect
            mPipelineStageRect.left = normalizedToScreen(stageStart);
            mPipelineStageRect.right = normalizedToScreen(stageEnd);
            paint.setColor(pipeline.getColor(stage));
            canvas.drawRect(mPipelineStageRect, paint);

            // The labels right on top
            double fraction = pipeline.getStageUtilization(stage)*100;
            String textPercentage = String.format("%.0f", fraction) + "%";
            float textPercentageWidth = paint.measureText(textPercentage);

            // do not allow label to overshoot into the previous stage
            float minTextPercentageStartXCoord = normalizedToScreen(overheadStart) + offset;
            float textPercentageStartXCoord = mPipelineStageRect.right-textPercentageWidth - offset;
            if (textPercentageStartXCoord < minTextPercentageStartXCoord)
                textPercentageStartXCoord = minTextPercentageStartXCoord;

            // percentage on top of the stage bar
            paint.setColor(Color.WHITE);
            canvas.drawText(textPercentage, textPercentageStartXCoord, mPipelineStageRect.top - offset, paint);

        }


        // partition the pipeline into fixed panels using vertical bars
        paint.setColor(Color.BLACK);
        for (int pos=0; pos<numberOfThumbs; pos++) {
            mVerticalBarRect.left = normalizedToScreen(pipeline.getStageRelativeWidth()*(pos+1)) - mLineWidth/2;
            mVerticalBarRect.right = normalizedToScreen(pipeline.getStageRelativeWidth()*(pos+1)) + mLineWidth/2;
            canvas.drawRect(mVerticalBarRect, paint);
        }

        paint.setTextSize(mTextSize);
        paint.setColor(Color.WHITE);

        // draw the text on seekbar
        for (int pos=0; pos<numberOfThumbs; pos++) {
            String text = Integer.toString(pos) + "=" + String.format("%.2f", getSelectedValue(pos));
            float textWidth = paint.measureText(text) + offset;
            canvas.drawText(text,
                    normalizedToScreen(pipeline.getMarker(pos)) - textWidth * 0.5f,
                    mDistanceToTop + mTextSize,
                    paint);
        }

        {   // draw the text of stage length on pipeline
            double stageLength = pipeline.getStageTime();
            String textStageLength = String.format("%.2f", stageLength);
            float textWidth = paint.measureText(textStageLength);
            for (int stage=0; stage<=numberOfThumbs; stage++) {

                // stage length under the stage bar
                canvas.drawText(textStageLength,
                        normalizedToScreen(pipeline.getStageRelativeWidth()*(stage+0.5)) - textWidth/2,
                        mPipelineRect.bottom + mTextSize,
                        paint);

            }
        }


        Savelog.d(TAG, debug, "pipeline length=" + pipeline.getTpipelined());

        {   // put start and end labels on pipeline
            String minPipelineLabel = String.format("%.2f", 0f);
            String maxPipelineLabel = String.format("%.2f", pipeline.getTpipelined());
            float minMaxLabelSize = Math.max(paint.measureText(minPipelineLabel), paint.measureText(maxPipelineLabel));

            float minMaxHeight = mPipelineRect.bottom;
            canvas.drawText(minPipelineLabel, 0, minMaxHeight, paint);
            canvas.drawText(maxPipelineLabel, getWidth() - minMaxLabelSize, minMaxHeight, paint);
        }

        {   // Draw the overhead seekbar
            paint.setColor(Color.GRAY);
            mOverheadRect.left = normalizedToScreen(pipeline.DEFAULT_MINIMUM);
            mOverheadRect.right = normalizedToScreen(pipeline.DEFAULT_MAXIMUM);
            canvas.drawRect(mOverheadRect, paint);
            paint.setColor(pipeline.getOverheadColor());

            mOverheadFractRect.left = normalizedToScreen(pipeline.DEFAULT_MINIMUM);
            mOverheadFractRect.right = normalizedToScreen(pipeline.getOverhead());
            canvas.drawRect(mOverheadFractRect, paint);
            drawOverheadThumb(mOverheadFractRect.right, false, canvas);
        }


        {    // put labels on the overhead seekbar
            paint.setTextSize(mTextSize);
            paint.setColor(Color.WHITE);

            String minOverheadLabel = String.format("%.0f", Pipeline.DEFAULT_MINIMUM);
            String maxOverheadLabel = String.format("%.0f", Pipeline.DEFAULT_MAXIMUM);
            float minMaxLabelSize = Math.max(paint.measureText(minOverheadLabel), paint.measureText(maxOverheadLabel));

            float minMaxHeight = mOverheadRect.bottom;
            canvas.drawText(minOverheadLabel, 0, minMaxHeight, paint);
            canvas.drawText(maxOverheadLabel, getWidth() - minMaxLabelSize, minMaxHeight, paint);

            String text = String.format("%.2f", pipeline.getOverhead());

            // put overhead value on top of the thumb
            float textWidth = paint.measureText(text) + offset;
            canvas.drawText(text,
                    normalizedToScreen(pipeline.getOverhead()) - textWidth * 0.5f,
                        mOverheadRect.top - offset, paint);
        }

    }


    private void drawPipelineThumb(float screenCoord, boolean pressed, Canvas canvas) {
        Savelog.d(TAG, debug, "drawing pipeline thumb at " + screenCoord);
        Bitmap thumbToDraw;
        thumbToDraw = pressed ? thumb.pressedImage : thumb.image;
        canvas.drawBitmap(thumbToDraw, screenCoord - thumb.halfWidth, mTextOffset, paint);
    }

    private void drawOverheadThumb(float screenCoord, boolean pressed, Canvas canvas) {
        Savelog.d(TAG, debug, "drawing overhead thumb at " + screenCoord);
        Bitmap thumbToDraw;
        thumbToDraw = pressed ? thumb.pressedImage : thumb.image;
        canvas.drawBitmap(thumbToDraw, screenCoord - thumb.halfWidth, mOverheadOffset, paint);
    }


    /**
     * Decides which (if any) thumb in the pipeline is touched by the given coordinates.
     * The pressed thumb or null if none has been touched.
     */
    private int[] evalPressedPipelineThumb(float touchX, float touchY) {
        int result[] = null;
        boolean thumbInRange[] = new boolean[numberOfThumbs];

        int count = 0;
        for (int pos=0; pos<numberOfThumbs; pos++) {
            thumbInRange[pos] = isInPipelineThumbRangeX(touchX, pipeline.getMarker(pos)) && isInPipelineThumbRangeY(touchY);
            if (thumbInRange[pos]) {
                count++;
            }
            Savelog.d(TAG, debug, "pos=" + pos + " at " + normalizedToScreen(pipeline.getMarker(pos)) + " touch=" + touchX + " inrange=" + thumbInRange[pos]);
        }
        if (count>0) {
            Savelog.d(TAG, debug, "inrange count=" + count);
            int index=0;
            result = new int[count];
            for (int pos=0; pos<numberOfThumbs; pos++) {
                if (thumbInRange[pos]) {
                    Savelog.d(TAG, debug, " thumb" + pos + " in");
                    result[index] = pos;
                    index++;
                }
            }

        }
        return result;
    }

    private boolean evalPressedOverheadThumb(float touchX, float touchY) {
        return isInOverheadThumbRangeX(touchX, pipeline.getOverhead()) && isInOverheadThumbRangeY(touchY);
    }


    private boolean isInPipelineThumbRangeX(float touchX, double normalizedThumbValue) {
        return Math.abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumb.widthBound;
    }

    private boolean isInPipelineThumbRangeY(float touchY) {
        double yLocationOfThumb = mTextOffset + thumb.halfHeight;
        Savelog.d(TAG, debug, "y=" + touchY + " ystarted=" + yLocationOfThumb + "ybound=" + thumb.heightBound);
        return Math.abs(touchY - yLocationOfThumb) <= thumb.heightBound;
    }


    private boolean isInOverheadThumbRangeX(float touchX, double normalizedThumbValue) {
        return Math.abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumb.widthBound;
    }

    private boolean isInOverheadThumbRangeY(float touchY) {
        double yLocationOfThumb = mOverheadOffset + thumb.halfHeight;
        Savelog.d(TAG, debug, "y=" + touchY + " ystarted=" + yLocationOfThumb + "ybound=" + thumb.heightBound);
        return Math.abs(touchY - yLocationOfThumb) <= thumb.heightBound;
    }


    private void setNormalizedPipelineValue(int pos, double value) {
        pipeline.setMarker(pos, value);
        invalidate();
    }

    private void setNormalizedOverheadValue(double value) {
        pipeline.setOverhead(value);
        invalidate();
    }



    /**
     * Converts a normalized value into screen space.
     */
    private float normalizedToScreen(double normalizedCoord) {
        return (float) (padding + normalizedCoord * (getWidth() - 2 * padding));
    }


    /**
     * Converts screen space x-coordinates into normalized values.
     */
    private double screenToNormalized(float screenCoord) {
        int width = getWidth();
        if (width <= 2 * padding) {
            // prevent division by zero, simply return 0.
            return Pipeline.DEFAULT_MINIMUM;
        } else {
            double result = (screenCoord - padding) / (width - 2 * padding);
            return Math.min(Pipeline.DEFAULT_MAXIMUM, Math.max(Pipeline.DEFAULT_MINIMUM, result));
        }
    }

    /*
     * Registers given listener callback to notify about changed selected values.
     */
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        this.listener = listener;
    }


}