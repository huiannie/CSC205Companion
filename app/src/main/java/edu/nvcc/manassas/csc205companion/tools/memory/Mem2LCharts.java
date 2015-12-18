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

package edu.nvcc.manassas.csc205companion.tools.memory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;

import edu.nvcc.manassas.csc205companion.R;
import edu.nvcc.manassas.csc205companion.app.AppSettings;
import edu.nvcc.manassas.csc205companion.io.Savelog;
import edu.nvcc.manassas.csc205companion.tools.util.PixelUtil;
import edu.nvcc.manassas.csc205companion.tools.util.Thumb;


public class Mem2LCharts extends ImageView {
    private static final String TAG = Mem2LCharts.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;


    private static final int CHART_MINDIM_IN_DP = 200;

    private static final int THUMB_MINHEIGHT_IN_DP = 64;
    private static final int THUMB_MINWIDTH_IN_DP = 64;

    private static final int TEXT_MINSPACE_IN_DP = 24; // Use for text labels
    private static final int BOX_PORTRAIT_MINWIDTH_IN_DP = CHART_MINDIM_IN_DP + THUMB_MINWIDTH_IN_DP + TEXT_MINSPACE_IN_DP *2;
    private static final int BOX_PORTRAIT_MINHEIGHT_IN_DP = CHART_MINDIM_IN_DP*2 + THUMB_MINHEIGHT_IN_DP*2 + TEXT_MINSPACE_IN_DP*3;

    private static final int BOX_LANDSCAPE_MINWIDTH_IN_DP = CHART_MINDIM_IN_DP*2 + THUMB_MINWIDTH_IN_DP + TEXT_MINSPACE_IN_DP*3;
    private static final int BOX_LANDSCAPE_MINHEIGHT_IN_DP = CHART_MINDIM_IN_DP + THUMB_MINHEIGHT_IN_DP + TEXT_MINSPACE_IN_DP*2;

    private static final int DEFAULT_TEXT_SIZE_IN_DP = 14;
    private static final float DEFAULT_MARKER_THICKNESS_IN_PX = 1f;
    private static final int DEFAULT_MARKER_WIDTH_IN_PX = 10;
    private static final int DEFAULT_TEXTPADDING_IN_PX = 10;

    private static final int INVALID_POINTER_ID = 255;
    private static final int ACTION_POINTER_INDEX_MASK = 0x0000ff00, ACTION_POINTER_INDEX_SHIFT = 8;

    private static final double LONG_RATIO = 3;

    public static final int MaxLogNumberOfZeros = 10;

    private double LogBase = 10d;
    private static final double MinLogscaledH = 1.0d;
    private double MaxLogscaledH = LogBase;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Thumb upThumb;
    private Thumb leftThumb;

    private boolean isPortrait = true;

    private int textSpaceX;
    private int textSpaceY;
    private int chartDim;
    private RectF boundingBox;
    private RectF charts[] = new RectF[2];
    private RectF seekbars[] = new RectF[3];
    private PointF pointers[] = new PointF[3];
    private int textSize;
    final float markerThickness = (float) DEFAULT_MARKER_THICKNESS_IN_PX;
    final int markerWidth = DEFAULT_MARKER_WIDTH_IN_PX;
    final int textPadding = DEFAULT_TEXTPADDING_IN_PX;

    private float mDownMotionX;
    private float mDownMotionY;
    private int mActivePointerId = INVALID_POINTER_ID;
    private int mScaledTouchSlop;
    private boolean mIsDragging;
    private int pressedThumb;
    private boolean notifyWhileDragging = false;
    private OnSeekBarChangeListener listener;
    private boolean isLong = false;

    private Tiers tiers;
    private boolean isLogscaled;


    public Mem2LCharts(Context context, Tiers tiers) {
        super(context);
        init(context, tiers);
    }


    private void init(Context context, Tiers tiers) {
        if (tiers==null)
            this.tiers = new Tiers();
        else
            this.tiers = tiers;

        isLogscaled = false;


        upThumb = new Thumb(context, R.mipmap.seek_pointer_up, R.mipmap.seek_pointer_up);
        leftThumb = new Thumb(context, R.mipmap.seek_pointer_left, R.mipmap.seek_pointer_left);

        pressedThumb = -1;

        // make SeekBar focusable. This solves focus handling issues in case EditText widgets are being used along with the SeekBar within ScollViews.
        setFocusable(true);
        setFocusableInTouchMode(true);
        mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Initialize width and height
        int width;
        int height;

        Savelog.d(TAG, debug, "onMeasure()");

        if (isPortrait) {
            width = PixelUtil.dpToPx(getContext(), BOX_PORTRAIT_MINWIDTH_IN_DP);
            height = PixelUtil.dpToPx(getContext(), BOX_PORTRAIT_MINHEIGHT_IN_DP);
        }
        else {
            width = PixelUtil.dpToPx(getContext(), BOX_LANDSCAPE_MINWIDTH_IN_DP);
            height = PixelUtil.dpToPx(getContext(), BOX_LANDSCAPE_MINHEIGHT_IN_DP);
        }

        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
            height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
        }

        computeLayoutDimensions(width, height);
        setMeasuredDimension(width, height);
    }

    private int getChartDimension(int width, int height) {
        int chartDim;

        int minTextSpace = PixelUtil.dpToPx(getContext(), TEXT_MINSPACE_IN_DP);
        if (isPortrait) {
            int dim1 = width - 2*minTextSpace - leftThumb.getWidth();
            int dim2 = (height - 3*minTextSpace - 2*upThumb.getHeight()) / 2;
            chartDim = dim1 < dim2 ? dim1 : dim2;
        }
        else { // landscape
            int dim1 = (width - 3*minTextSpace - leftThumb.getWidth()) / 2;
            int dim2 = height - 2*minTextSpace - upThumb.getHeight();
            chartDim = dim1 < dim2 ? dim1 : dim2;
        }
        return chartDim;
    }

    private void setTextSpace(int chartDim, int width, int height) {
        if (isPortrait) {
            textSpaceX = (width - chartDim - leftThumb.getWidth()) / 2;
            textSpaceY = (height - 2*chartDim - 2*upThumb.getHeight()) / 3;
        }
        else {
            textSpaceX = (width - 2*chartDim - leftThumb.getWidth()) / 3;
            textSpaceY = (height - chartDim - upThumb.getHeight()) / 2;
        }
    }


    private void setBoundingBox(int width, int height) {
        if (boundingBox==null)
            boundingBox = new RectF(0, 0, width, height);
        else {
            boundingBox.left = 0;
            boundingBox.top = 0;
            boundingBox.right = width;
            boundingBox.bottom = height;
        }
    }

    private void setChart(int index) {
        if (index<0 || index>=charts.length) return;

        int xStart, yStart, xEnd, yEnd;

        if (index==0) {
            // Chart 1
            xStart = textSpaceX;
            yStart = textSpaceY;
            xEnd = xStart + chartDim;
            yEnd = yStart + chartDim;
        }
        else {
            // Chart 2
            if (isPortrait) {
                xStart = textSpaceX;
                yStart = 2*textSpaceY + chartDim + upThumb.getHeight();
                xEnd = xStart + chartDim;
                yEnd = yStart + chartDim;
            }
            else {
                xStart = 2*textSpaceX + chartDim + leftThumb.getWidth();
                yStart = textSpaceY;
                xEnd = xStart + chartDim;
                yEnd = yStart + chartDim;
            }
        }

        if (charts[index]==null)
            charts[index] = new RectF(xStart, yStart, xEnd, yEnd);
        else {
            charts[index].left = xStart;
            charts[index].top = yStart;
            charts[index].right = xEnd;
            charts[index].bottom = yEnd;
        }
    }


    private void setSeekbar(int index) {
        if (index<0 || index>=seekbars.length) return;

        int xStart, yStart, xEnd, yEnd;

        if (index==0) {
            // The seekbar directly under chart1
            xStart = textSpaceX;
            yStart = textSpaceY + chartDim;
            xEnd = xStart + chartDim;
            yEnd = yStart + upThumb.getHeight();
        }
        else if (index==1) {
            // The seekbar directly under chart2
            if (isPortrait) {
                xStart = textSpaceX;
                yStart = 2*textSpaceY + 2*chartDim + upThumb.getHeight();
                xEnd = xStart + chartDim;
                yEnd = yStart + upThumb.getHeight();
            }
            else {
                xStart = 2*textSpaceX + chartDim + leftThumb.getWidth();
                yStart = textSpaceY + chartDim;
                xEnd = xStart + chartDim;
                yEnd = yStart + upThumb.getHeight();
            }
        }
        else {
            // The vertical seekbar
            xStart = textSpaceX + chartDim;
            yStart = textSpaceY;
            xEnd = xStart + leftThumb.getWidth();
            yEnd = yStart + chartDim;
        }

        if (seekbars[index]==null)
            seekbars[index] = new RectF(xStart, yStart, xEnd, yEnd);
        else {
            seekbars[index].left = xStart;
            seekbars[index].top = yStart;
            seekbars[index].right = xEnd;
            seekbars[index].bottom = yEnd;

            Savelog.d(TAG, debug, "seekbar[" + index + "] left=" + xStart + " top=" + yStart + " right=" + xEnd + " bottom=" + yEnd);
        }
    }

    public void setLogBase(int numberOfZeroes) {
        if (numberOfZeroes<1 || numberOfZeroes>MaxLogNumberOfZeros) {
            numberOfZeroes = MaxLogNumberOfZeros;
        }
        double logbase = Math.pow(10, numberOfZeroes);
        LogBase = logbase;
        MaxLogscaledH = LogBase;
    }
    public double getLogBase() {
        return LogBase;
    }

    private double getLogScaledH(double h) {
        double expo = Math.pow(LogBase, h);
        return (expo - MinLogscaledH) / (MaxLogscaledH - MinLogscaledH);
    }
    private double getLinearScaledH(double logscaledH) {
        double expo = logscaledH * (MaxLogscaledH-MinLogscaledH) + MinLogscaledH;
        return Math.log10(expo) / Math.log10(LogBase);
    }


    private float hToChartCoordX(int index, double h) {
        if (isLogscaled)
            return hToChartCoordXLog(index, h);
        else
            return hToChartCoordXLinear(index, h);
    }

    private float hToChartCoordXLinear(int index, double h) {
        if (index<0 || index>=charts.length) return -1.0f;
        if (charts[index]==null) return -1.0f;

        float deltaX = (float)h * chartDim;
        return charts[index].left + deltaX;
    }

    // use log scale
    private float hToChartCoordXLog(int index, double h) {
        if (index<0 || index>=charts.length) return -1.0f;
        if (charts[index]==null) return -1.0f;

        double logscaledH = getLogScaledH(h);
        float deltaX = (float)logscaledH * chartDim;
        return charts[index].left + deltaX;
    }


    private float hToSeekbarCoordX(int index, double h) {
        if (isLogscaled)
            return hToSeekbarCoordXLog(index, h);
        else
            return hToSeekbarCoordXLinear(index, h);
    }

    private float hToSeekbarCoordXLinear(int index, double h) {
        if (index<0 || index>=seekbars.length) return -1.0f;
        if (seekbars[index]==null) return -1.0f;

        float deltaX = (float)h * chartDim;
        return seekbars[index].left + deltaX  - upThumb.halfWidth;
    }

    // use log scale
    private float hToSeekbarCoordXLog(int index, double h) {
        if (index<0 || index>=seekbars.length) return -1.0f;
        if (seekbars[index]==null) return -1.0f;

        double logscaledH = getLogScaledH(h);
        float deltaX = (float)logscaledH * chartDim;
        return seekbars[index].left + deltaX  - upThumb.halfWidth;
    }


    private float hToSeekbarCoordY(int index, double h) {
        if (index<0 || index>=seekbars.length) return -1.0f;
        if (seekbars[index]==null) return -1.0f;
        return seekbars[index].top;
    }

    private float EATtoChartCoordY(int index, double normalizedEAT) {
        if (index<0 || index>=charts.length) return -1.0f;
        if (charts[index]==null) return -1.0f;

        float deltaY = (float)((1.0 - normalizedEAT) * chartDim);
        return charts[index].top + deltaY;
    }

    private float t1ToSeekbarCoordX(int index, double t1EATratio) {
        if (index<0 || index>=seekbars.length) return -1.0f;
        if (seekbars[index]==null) return -1.0f;
        return seekbars[index].left;
    }

    private float t1ToSeekbarCoordY(int index, double t1EATratio) {
        if (index<0 || index>=seekbars.length) return -1.0f;
        if (seekbars[index]==null) return -1.0f;

        float deltaY = (float)((1.0 - t1EATratio) * chartDim);
        return seekbars[index].top + deltaY  - leftThumb.halfHeight;
    }

    private float effToChartCoordY(int index, double eff) {
        if (index<0 || index>=charts.length) return -1.0f;
        if (charts[index]==null) return -1.0f;

        return charts[index].top + (float)(1.0 - eff) * chartDim;
    }


    private double screenCoordToH(int index, float x) {
        if (isLogscaled)
            return screenCoordToHLog(index, x);
        else
            return screenCoordToHLinear(index, x);
    }

    private double screenCoordToHLinear(int index, float x) {
        if (index!=0 && index!=1) return tiers.getH();
        if (x<=seekbars[index].left) x = seekbars[index].left;
        if (x>=seekbars[index].right) x = seekbars[index].right;
        Savelog.d(TAG, debug, "seekbar left=" + seekbars[index].left + " right=" + seekbars[index].right + " x=" + x + " h=" + tiers.getH());

        float xOffset = seekbars[index].left;
        double newH = (double) (x-xOffset) / chartDim;
        Savelog.d(TAG, debug, "New h is=" + newH);
        return newH;
    }

    // recover linear-scaled h from the logscaled h
    private double screenCoordToHLog(int index, float x) {
        double logscaledH = getLinearScaledH(tiers.getH());

        if (index!=0 && index!=1) return logscaledH;
        if (x<=seekbars[index].left) x = seekbars[index].left;
        if (x>=seekbars[index].right) x = seekbars[index].right;

        Savelog.d(TAG, debug, "seekbar left=" + seekbars[index].left + " right=" + seekbars[index].right + " x=" + x + " h=" + logscaledH);

        float xOffset = seekbars[index].left;
        double newLogscaledH = (double) (x-xOffset) / chartDim;

        Savelog.d(TAG, debug, "New h is=" + newLogscaledH);
        return getLinearScaledH(newLogscaledH);
    }


    private double screenCoordTot1EATratio(float y) {
        if (y<=seekbars[2].top) y = seekbars[2].top;
        if (y>=seekbars[2].bottom) y = seekbars[2].bottom;

        float yOffset = seekbars[2].top;
        double t1EATRatio = (1.0 - (y-yOffset) / chartDim);
        return t1EATRatio;
    }

    public void refreshPointers() {
        setPointer(0);
        setPointer(1);
        setPointer(2);
        invalidate();
    }

    private void setPointer(int index) {
        if (index<0 || index>=pointers.length) return;
        if (seekbars[index]==null) return;

        float xStart;
        float yStart;

        if (index==0) {
            // use h
            xStart = hToSeekbarCoordX(index, tiers.getH());
            yStart = hToSeekbarCoordY(index, tiers.getH());
        }
        else if (index==1) {
            // use h
            xStart = hToSeekbarCoordX(index, tiers.getH());
            yStart = hToSeekbarCoordY(index, tiers.getH());
        }
        else {
            // use normalized T1
            double t1EATratio = tiers.getT1EATratio();
            xStart = t1ToSeekbarCoordX(index, t1EATratio);
            yStart = t1ToSeekbarCoordY(index, t1EATratio);
        }
        if (pointers[index]==null)
            pointers[index] = new PointF(xStart, yStart);
        else {
            pointers[index].x = xStart;
            pointers[index].y = yStart;
        }
    }


    private void computeLayoutDimensions(int width, int height) {
        Savelog.d(TAG, debug, "computeLayoutDimensions(" + width + "," + height + ")");
        if (width>0 && height>0) {

            // Set up orientation
            if (height>width)
                isPortrait = true;
            else
                isPortrait = false;

            Savelog.d(TAG, debug, "aspect ratio = " + (double)width/height);
            if ((double)width/height >= LONG_RATIO) {
                isLong = true;
            }
            else isLong = false;

            chartDim = getChartDimension(width, height);
            setTextSpace(chartDim, width, height);

            setBoundingBox(width, height);
            setChart(0);
            setChart(1);
            setSeekbar(0);
            setSeekbar(1);
            setSeekbar(2);
            setPointer(0);
            setPointer(1);
            setPointer(2);

            textSize = PixelUtil.dpToPx(getContext(), DEFAULT_TEXT_SIZE_IN_DP);
        }
        else {
            Savelog.d(TAG, debug, "width or height is 0");
        }

    }



    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Savelog.d(TAG, debug, "onDraw()");

        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);


        paint.setColor(Color.GRAY);
        canvas.drawRect(charts[0], paint);
        canvas.drawRect(charts[1], paint);

        paint.setColor(getResources().getColor(R.color.background_black));
        canvas.drawRect(seekbars[0], paint);
        canvas.drawRect(seekbars[1], paint);
        canvas.drawRect(seekbars[2], paint);


        paint.setColor(tiers.getTypeColor());
        drawChart(0, canvas);
        drawChart(1, canvas);

        paint.setColor(Color.WHITE);
        drawEATLabels(0, canvas);
        drawEffLabels(1, canvas);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        canvas.drawRect(boundingBox, paint);
        canvas.drawRect(charts[0], paint);
        canvas.drawRect(charts[1], paint);

        drawThumb(0, false, canvas);
        drawThumb(1, false, canvas);
        drawThumb(2, false, canvas);

        paint.setColor(Color.GRAY);
        drawScaleLabel(canvas);
    }

    private void drawThumb(int index, boolean pressed, Canvas canvas) {
        if (index<0 || index>=pointers.length) return;
        Bitmap thumbToDraw;
        float screenCoordX = pointers[index].x;
        float screenCoordY = pointers[index].y;
        Savelog.d(TAG, debug, "drawing thumb " + index + " at x=" + screenCoordX + " y=" + screenCoordY);

        if (index==0 || index==1) {
            thumbToDraw = pressed ? upThumb.pressedImage : upThumb.image;
        }
        else {
            thumbToDraw = pressed ? leftThumb.pressedImage : leftThumb.image;
        }
        canvas.drawBitmap(thumbToDraw, screenCoordX , screenCoordY, paint);
    }

    private void drawChart(int index, Canvas canvas) {
        if (index<0 || index>=charts.length) return;
        if (index==0) {
            Path p = getEATCurve(index);
            canvas.drawPath(p, paint);
        }
        if (index==1) {
            Path p = getEfficiencyCurve(index);
            canvas.drawPath(p, paint);
        }
    }



    private Path getEATPolygon(int chartIndex) {
        double EATmax = tiers.getEAT(Tiers.MIN_H);

        Path path = new Path();
        path.reset();
        path.moveTo(hToChartCoordX(chartIndex, Tiers.MIN_H), EATtoChartCoordY(chartIndex, 0));
        path.lineTo(hToChartCoordX(chartIndex, Tiers.MIN_H), EATtoChartCoordY(chartIndex, tiers.getEAT(Tiers.MIN_H) / EATmax));
        path.lineTo(hToChartCoordX(chartIndex, Tiers.MAX_H), EATtoChartCoordY(chartIndex, tiers.getEAT(Tiers.MAX_H) / EATmax));
        path.lineTo(hToChartCoordX(chartIndex, Tiers.MAX_H), EATtoChartCoordY(chartIndex, 0));
        path.lineTo(hToChartCoordX(chartIndex, Tiers.MIN_H), EATtoChartCoordY(chartIndex, 0));
        return path;
    }


    private Path getEATCurve(int chartIndex) {
        double EATmax = tiers.getEAT(Tiers.MIN_H);
        final int MAX_SEGMENTS = 4;
        final int ControlSize = 3;
        final int count = MAX_SEGMENTS*ControlSize + 1;
        float pointX[] = new float[count];
        float pointY[] = new float[count];

        double hsamples[] = tiers.HSamplesUniform(count);
        for (int index=0; index<count; index++) {
            // Savelog.d(TAG, debug, "h=" + hsamples[index] + " EAT=" + tiers.getEAT(hsamples[index]));
            pointX[index] = hToChartCoordX(chartIndex, hsamples[index]);
            pointY[index] = EATtoChartCoordY(chartIndex, tiers.getEAT(hsamples[index]) / EATmax);
        }

        Savelog.d(TAG, debug, "numPoints=" + count);

        Path path = new Path();
        path.reset();
        path.moveTo(hToChartCoordX(chartIndex, Tiers.MIN_H), EATtoChartCoordY(chartIndex, 0));
        path.lineTo(pointX[0], pointY[0]);

        for (int index=1; index<=count-ControlSize; index+=3) {
            path.cubicTo(pointX[index], pointY[index], pointX[index+1], pointY[index+1], pointX[index+2], pointY[index+2]);
        }
        path.lineTo(hToChartCoordX(chartIndex, Tiers.MAX_H), EATtoChartCoordY(chartIndex, 0));
        path.lineTo(hToChartCoordX(chartIndex, Tiers.MIN_H), EATtoChartCoordY(chartIndex, 0));
        return path;
    }


    private Path getEfficiencyCurve(int chartIndex) {

        final int MAX_SEGMENTS = 4;
        final int ControlSize = 3;
        final int count = MAX_SEGMENTS*ControlSize + 1;
        float pointX[] = new float[count];
        float pointY[] = new float[count];

        double hsamples[] = tiers.HSamplesByReflexionOnEff(count);
        for (int index=0; index<count; index++) {
            // Savelog.d(TAG, debug, "h=" + hsamples[index] + " Eff=" + tiers.getEfficiency(hsamples[index]));
            pointX[index] = hToChartCoordX(chartIndex, hsamples[index]);
            pointY[index] = effToChartCoordY(chartIndex, tiers.getEfficiency(hsamples[index]));
        }

        Savelog.d(TAG, debug, "numPoints=" + count);

        Path path = new Path();
        path.reset();
        path.moveTo(hToChartCoordX(chartIndex, Tiers.MIN_H), effToChartCoordY(chartIndex, 0));
        path.lineTo(pointX[0], pointY[0]);

        for (int index=1; index<=count-ControlSize; index+=3) {
            path.cubicTo(pointX[index], pointY[index], pointX[index+1], pointY[index+1], pointX[index+2], pointY[index+2]);
        }
        path.lineTo(hToChartCoordX(chartIndex, Tiers.MAX_H), effToChartCoordY(chartIndex, 0));
        path.lineTo(hToChartCoordX(chartIndex, Tiers.MIN_H), effToChartCoordY(chartIndex, 0));
        return path;
    }



    private void drawEATLabels(int index, Canvas canvas) {
        RectF marker = new RectF();

        String labelMaxEAT = String.format("%.2f", tiers.getEAT(Tiers.MIN_H));
        String labelMinT1 = String.format("%.2f", tiers.getMinT1());
        String labelMaxT1 = String.format("%.2f", tiers.getMaxT1());
        String labelT1 = "T1=" + String.format("%.2f", tiers.getT1());
        String labelT2 = "T2=" + String.format("%.2f", tiers.getT2());
        String labelH = String.format("h=%.3f", tiers.getH());
        String labelEAT = String.format("EAT=%.3f", tiers.getEAT());

        final int textYOffset = textSize/2 - 1;

        paint.setTextSize(textSize);

        float labelSize;
        double delta = 0.01;

        // The MAX EAT label

        double maxEAT = tiers.getEAT(Tiers.MIN_H);
        if (Math.abs(tiers.getT2()-maxEAT)/maxEAT > delta) {  // Draw only when T2 is not on the same position
            labelSize = paint.measureText(labelMaxEAT);

            marker.left = charts[index].left - markerWidth;
            marker.right = charts[index].left;
            marker.top = charts[index].top - markerThickness/2;
            marker.bottom = charts[index].top + markerThickness/2;

            canvas.drawRect(marker, paint);
            canvas.drawText(labelMaxEAT, marker.left - labelSize - textPadding, marker.top + textYOffset, paint);
        }


        // The current T2 label
        labelSize = paint.measureText(labelT2);

        marker.left = charts[index].left - markerWidth;
        marker.right = charts[index].left;
        marker.top = EATtoChartCoordY(index, tiers.getT2EATratio()) - markerThickness/2;
        marker.bottom = EATtoChartCoordY(index, tiers.getT2EATratio()) + markerThickness/2;

        canvas.drawRect(marker, paint);
        canvas.drawText(labelT2, marker.left - labelSize - textPadding, marker.top + textYOffset, paint);


        // The MAX T1 label

        if (Math.abs(tiers.getMaxT1EATratio() - tiers.getT1EATratio()) > delta) {  // Draw only when T1 is not on the same position
            marker.left = charts[index].right;
            marker.right = charts[index].right + markerWidth;
            marker.top = EATtoChartCoordY(index, tiers.getMaxT1EATratio()) - markerThickness/2;
            marker.bottom = EATtoChartCoordY(index, tiers.getMaxT1EATratio()) + markerThickness/2;

            canvas.drawRect(marker, paint);
            canvas.drawText(labelMaxT1, marker.right + textPadding, marker.top + textYOffset, paint);
        }

        // The MIN T1 label

        if (Math.abs(tiers.getT1EATratio() - tiers.getMinT1EATratio()) > delta) {  // Draw only when T1 is not on the same position
            marker.left = charts[index].right;
            marker.right = charts[index].right + markerWidth;
            marker.top = EATtoChartCoordY(index, tiers.getMinT1EATratio()) - markerThickness/2;
            marker.bottom = EATtoChartCoordY(index, tiers.getMinT1EATratio()) + markerThickness/2;

            canvas.drawRect(marker, paint);
            canvas.drawText(labelMinT1, marker.right + textPadding, marker.top + textYOffset, paint);
        }

        // The current T1 label

        marker.left = charts[index].right;
        marker.right = charts[index].right + leftThumb.halfWidth;
        marker.top = EATtoChartCoordY(index, tiers.getT1EATratio()) - markerThickness/2;
        marker.bottom = EATtoChartCoordY(index, tiers.getT1EATratio()) + markerThickness/2;

        // No need to draw the marker. There is the thumb.
        canvas.drawText(labelT1, marker.right + textPadding, marker.top + textYOffset, paint);


        // The current h and EAT
        marker.left = hToChartCoordX(index, tiers.getH()) - markerThickness/2;
        marker.right = hToChartCoordX(index, tiers.getH()) + markerThickness/2;
        marker.top = EATtoChartCoordY(index, tiers.getEAT() / tiers.getEAT(Tiers.MIN_H)); // normalize EAT
        marker.bottom = EATtoChartCoordY(index, 0) + markerThickness/2;
        canvas.drawRect(marker, paint);

        labelSize = paint.measureText(labelH);
        canvas.drawText(labelH, marker.centerX() - labelSize/2, marker.bottom + upThumb.height, paint);

        labelSize = paint.measureText(labelEAT);
        canvas.drawText(labelEAT, marker.centerX() - labelSize/2, marker.top + - textYOffset, paint);
    }




    private void drawEffLabels(int index, Canvas canvas) {
        RectF marker = new RectF();

        double minEfficiency = tiers.getEfficiency(Tiers.MIN_H);
        String label0 = String.format("%.2f", 0d);
        String label1 = String.format("%.2f", 1d);

        String labelMinEff = String.format("%.2f", minEfficiency);
        String labelH = String.format("h=%.3f", tiers.getH());
        String labelEff = String.format("Eff=%.3f", tiers.getEfficiency());

        final int textYOffset = textSize/2 - 1;

        paint.setTextSize(textSize);

        float labelSize;


        // The 0-efficiency label

        marker.left = charts[index].right;
        marker.right = charts[index].right + markerWidth;
        marker.top = effToChartCoordY(index, 0) - markerThickness / 2;
        marker.bottom = effToChartCoordY(index, 0) + markerThickness / 2;

        canvas.drawRect(marker, paint);
        canvas.drawText(label0, marker.right + textPadding, marker.top + textYOffset, paint);



        // The 1-efficiency label

        marker.left = charts[index].right;
        marker.right = charts[index].right + markerWidth;
        marker.top = effToChartCoordY(index, 1) - markerThickness / 2;
        marker.bottom = effToChartCoordY(index, 1) + markerThickness / 2;

        canvas.drawRect(marker, paint);
        canvas.drawText(label1, marker.right + textPadding, marker.top + textYOffset, paint);


        // The MIN EFF label
        labelSize = paint.measureText(labelMinEff);
        marker.top = effToChartCoordY(index, minEfficiency) - markerThickness/2;
        marker.bottom = effToChartCoordY(index, minEfficiency) + markerThickness/2;

        if (!isLong && !isPortrait) {
            // special case: for squary window, draw the marker and label inside the graph
            marker.left = charts[index].left - markerWidth / 2;
            marker.right = charts[index].left + markerWidth / 2;
            canvas.drawRect(marker, paint);
            canvas.drawText(labelMinEff, marker.left + textPadding, marker.top + textYOffset, paint);
        }
        else {
            marker.left = charts[index].left - markerWidth;
            marker.right = charts[index].left;
            canvas.drawRect(marker, paint);
            canvas.drawText(labelMinEff, marker.left - labelSize - textPadding, marker.top + textYOffset, paint);
        }




        // The current h and Eff
        marker.left = hToChartCoordX(index, tiers.getH()) - markerThickness/2;
        marker.right = hToChartCoordX(index, tiers.getH()) + markerThickness/2;
        marker.top = effToChartCoordY(index, tiers.getEfficiency());
        marker.bottom = effToChartCoordY(index, 0) + markerThickness/2;
        canvas.drawRect(marker, paint);

        labelSize = paint.measureText(labelH);
        canvas.drawText(labelH, marker.centerX() - labelSize/2, marker.bottom + upThumb.height, paint);

        labelSize = paint.measureText(labelEff);
        canvas.drawText(labelEff, marker.centerX() - labelSize/2, marker.top + - textYOffset, paint);
    }



    private void drawScaleLabel(Canvas canvas) {
        String labelScale = isLogscaled ? "h in log"+ String.format("%.0f", LogBase) +"-scale" : "h in linear scale";

        final int textYOffset = textSize/2 - 1;
        paint.setTextSize(textSize);

        float labelSize;
        labelSize = paint.measureText(labelScale);
        canvas.drawText(labelScale, boundingBox.right - labelSize*1.2f, boundingBox.bottom - textYOffset*2, paint);

    }



    private int evalPressedThumb(float touchX, float touchY) {
        boolean thumbInRange;
        boolean vThumbInRange;

        thumbInRange = isInHThumbRange(0, touchX, touchY);
        Savelog.d(TAG, debug, "thumb0 in range? " + thumbInRange);
        if (thumbInRange) return 0;

        thumbInRange = isInHThumbRange(1, touchX, touchY);
        Savelog.d(TAG, debug, "thumb1 in range? " + thumbInRange);
        if (thumbInRange) return 1;

        vThumbInRange = isInVThumbRange(2, touchX, touchY);
        Savelog.d(TAG, debug, "thumb2 in range? " + vThumbInRange);
        if (vThumbInRange) return 2;

        return -1;
    }



    private boolean isInHThumbRange(int thumbIndex, float touchX, float touchY) {
        if (thumbIndex==2) return false;

        int vThumbIndex = 2;
        float minT1CoordY = t1ToSeekbarCoordY(vThumbIndex, tiers.getMinT1EATratio()) + leftThumb.halfWidth;
        if (isPortrait) {
            if (touchX < seekbars[thumbIndex].right) {
                // certainly h
                return Math.abs(touchX - hToChartCoordX(thumbIndex, tiers.getH())) <= upThumb.widthBound;
            }
            else if (touchY > minT1CoordY) {
                // certainly h
                return Math.abs(touchX - hToChartCoordX(thumbIndex, tiers.getH())) <= upThumb.widthBound;
            }
        }
        else {
            // landscape
            if (touchX >= seekbars[vThumbIndex].left && touchX <= seekbars[vThumbIndex].right) {
                // certainly not h
                return false;
            }
            else if (thumbIndex==0 && touchX < seekbars[thumbIndex].right) return true;
            else if (thumbIndex==1 && touchX > seekbars[thumbIndex].left) return true;
        }
        return false;

    }


    private boolean isInVThumbRange(int thumbIndex, float touchX, float touchY) {
        if (thumbIndex!=2) return false;

        Savelog.d(TAG, debug, "thumb2 left=" + seekbars[thumbIndex].left + " right=" + seekbars[thumbIndex].right + " x=" + touchX);
        Savelog.d(TAG, debug, "thumb2 top=" + seekbars[thumbIndex].top + " bottom=" + seekbars[thumbIndex].bottom + " y=" + touchY);
        if (isPortrait) {
            int hThumbIndex = 0;
            if (touchX >= seekbars[hThumbIndex].right) {
                return Math.abs(touchY - t1ToSeekbarCoordY(thumbIndex, tiers.getT1EATratio())) < leftThumb.heightBound;
            }
        }
        else {
            int hThumbIndex0 = 0;
            int hThumbIndex1 = 1;
            if (touchX >= seekbars[hThumbIndex0].right && touchX <= seekbars[hThumbIndex1].left) {
                return Math.abs(touchY - t1ToSeekbarCoordY(thumbIndex, tiers.getT1EATratio())) < leftThumb.heightBound;
            }
        }
        return false;
    }




    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!isEnabled()) {
            return false;
        }

        int pointerIndex;

        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                Savelog.d(TAG, debug, "motion event: action_down");
                // Remember where the motion event started
                mActivePointerId = event.getPointerId(event.getPointerCount() - 1);
                pointerIndex = event.findPointerIndex(mActivePointerId);
                mDownMotionX = event.getX(pointerIndex);
                mDownMotionY = event.getY(pointerIndex);


                pressedThumb = evalPressedThumb(mDownMotionX, mDownMotionY);

                Savelog.d(TAG, debug, "Pressed thumb=" + pressedThumb);

                // Only handle upThumb presses.
                if (pressedThumb == -1) {
                    return super.onTouchEvent(event);
                }


                setPressed(true);
                invalidate();
                onStartTrackingTouch();
                trackTouchEvent(event);
                attemptClaimDrag();

                break;
            case MotionEvent.ACTION_MOVE:
                Savelog.d(TAG, debug, "motion event: action_move");
                if (pressedThumb != -1) {

                    if (mIsDragging) {
                        trackTouchEvent(event);

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
                        notifyListener(event);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                Savelog.d(TAG, debug, "motion event: action_up");
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

                pressedThumb = -1;
                invalidate();
                if (listener != null) {
                    notifyListener(event);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                Savelog.d(TAG, debug, "motion event: action_pointer_down");
                final int index = event.getPointerCount() - 1;
                mDownMotionX = event.getX(index);
                mDownMotionY = event.getY(index);
                mActivePointerId = event.getPointerId(index);

                Savelog.d(TAG, debug, "downMotion x=" + mDownMotionX + " y=" + mDownMotionY);

                invalidate();
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                Savelog.d(TAG, debug, "motion event: action_pointer_up");
                onSecondaryPointerUp(event);
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                Savelog.d(TAG, debug, "motion event: action_cancel");
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


    private void notifyListener(MotionEvent event) {
        double newH = tiers.getH();
        double newT1 = tiers.getT1();
        if (pressedThumb==0 || pressedThumb==1) {
            newH = screenCoordToH(pressedThumb, event.getX());
            listener.onSeekBarValuesChanged(this, newT1, newH);
        }
        else if (pressedThumb==2) {
            double t1EATratio = screenCoordTot1EATratio(event.getY());
            newT1 = tiers.getT1FromEATratio(t1EATratio);
            Savelog.d(TAG, debug, "action: newT1=" + newT1);
            listener.onSeekBarValuesChanged(this, newT1, newH);
        }
    }

    public void setLogScaled(boolean isLogscaled) {
        this.isLogscaled = isLogscaled;
        refreshPointers();
    }

    public boolean isLogScaledH() {
        return isLogscaled;
    }

    private final void trackTouchEvent(MotionEvent event) {
        final int pointerIndex = event.findPointerIndex(mActivePointerId);
        final float x = event.getX(pointerIndex);
        final float y = event.getY(pointerIndex);
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

    public boolean isNotifyWhileDragging() {
        return notifyWhileDragging;
    }
    public void setNotifyWhileDragging(boolean flag) {
        this.notifyWhileDragging = flag;
    }



    public interface OnSeekBarChangeListener {
        public void onSeekBarValuesChanged(Mem2LCharts bar, double t1, double h);
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        this.listener = listener;
    }

}
