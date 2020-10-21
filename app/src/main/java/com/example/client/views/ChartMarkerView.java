package com.example.client.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import com.example.client.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

public class ChartMarkerView extends MarkerView {

    private TextView textView;
    private boolean isNegativeValue;

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource the layout resource to use for the MarkerView
     */
    public ChartMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);

        textView = findViewById(R.id.textView);
    }

    @Override
    public MPPointF getOffset() {
        if (isNegativeValue)
            return new MPPointF(-((float) getWidth() / 2), 0);
        else
            return new MPPointF(-((float) getWidth() / 2), -getHeight());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        isNegativeValue = e.getY() < 0;
        textView.setText("" + (int) Math.abs(e.getY()));
        super.refreshContent(e, highlight);
    }
}
