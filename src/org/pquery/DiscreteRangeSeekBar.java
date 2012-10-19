package org.pquery;

import android.content.Context;
import android.view.MotionEvent;

public class DiscreteRangeSeekBar<T extends Number> extends RangeSeekBar<T> {

    private double normalizedStepSize;

    @SuppressWarnings("unchecked")
    public DiscreteRangeSeekBar(T absoluteMinValue, T absoluteMaxValue, T stepSize, Context context)
            throws IllegalArgumentException {
        super(absoluteMinValue, absoluteMaxValue, context);

        this.normalizedStepSize = valueToNormalized((T) numberType.toNumber(absoluteMinValue.doubleValue()
                + stepSize.doubleValue()));
    }

    @Override
    protected void trackTouchEvent(MotionEvent event) {
        final int pointerIndex = event.findPointerIndex(mActivePointerId);
        final float x = event.getX(pointerIndex);

        if (Thumb.MIN.equals(pressedThumb)) {
            setNormalizedMinValue(findClosestNormalizedStep(screenToNormalized(x)));
        } else if (Thumb.MAX.equals(pressedThumb)) {
            setNormalizedMaxValue(findClosestNormalizedStep(screenToNormalized(x)));
        }
    }

    /**
     * Return normalized location of the nearest step or max (if closer)
     */
    private double findClosestNormalizedStep(double value) {
        int numbStepsBelow = (int) Math.floor(value / normalizedStepSize);

        double stepBelow = normalizedStepSize * numbStepsBelow;
        double stepAbove = Math.min(normalizedStepSize * (numbStepsBelow + 1), 1.0d);

        double distanceBelow = value - stepBelow;
        double distanceAbove = stepAbove - value;

        if (distanceBelow < distanceAbove)
            return stepBelow;
        return stepAbove;
    }
}