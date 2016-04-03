package com.ashwinkachhara.circularseekbartest;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by ashwin on 4/2/16.
 */
public class MainMenuListItemLayout extends LinearLayout
        implements WearableListView.OnCenterProximityListener {

    private ImageView mImg;

    private final float mFadedTextAlpha;

    public MainMenuListItemLayout(Context context) {
        this(context, null);
    }

    public MainMenuListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainMenuListItemLayout(Context context, AttributeSet attrs,
                                  int defStyle) {
        super(context, attrs, defStyle);

        mFadedTextAlpha = (float)1.0;//getResources().getInteger(R.integer.action_text_faded_alpha) / 100f;
    }

    // Get references to the icon and text in the item layout definition
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // These are defined in the layout file for list items
        // (see next section)
        mImg = (ImageView) findViewById(R.id.circle);
    }

    @Override
    public void onCenterPosition(boolean animate) {
        mImg.setAlpha(1f);
//        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) mImg.getLayoutParams();
//        p.setMargins(80,0,0,0);
//        mImg.requestLayout();
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        mImg.setAlpha(mFadedTextAlpha);
//        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) mImg.getLayoutParams();
//        p.setMargins(40,0,0,0);
//        mImg.requestLayout();
    }
}

