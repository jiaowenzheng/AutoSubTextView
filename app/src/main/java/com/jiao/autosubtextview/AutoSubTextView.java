package com.jiao.autosubtextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * @program: FoldText_Java
 * @description:
 * @author: Mr.Wang
 * @create: 2020-01-11 23:30
 **/
public class AutoSubTextView extends AppCompatTextView {

    private static final String ELLIPSIZE_END = "...";
    private static final int MAX_LINE = 2;
    private int mShowMaxLine;

    private boolean flag;
    private CharSequence mOriginalText;

    public AutoSubTextView(Context context) {
        this(context, null);
    }


    public AutoSubTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoSubTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mShowMaxLine = MAX_LINE;

        if (attrs != null) {
            TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.AutoSubTextView);
            mShowMaxLine = arr.getInt(R.styleable.AutoSubTextView_max_lines, MAX_LINE);
            arr.recycle();
        }
    }

    @Override
    public void setText(final CharSequence text, final TextView.BufferType type) {
        if (TextUtils.isEmpty(text) || mShowMaxLine == 0) {
            super.setText(text, type);
        } else if (!flag) {
            getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    getViewTreeObserver().removeOnPreDrawListener(this);
                    flag = true;
                    formatText(text, type);
                    return true;
                }
            });
        } else {
            formatText(text, type);
        }

    }

    private void formatText(CharSequence text, final TextView.BufferType type) {
        mOriginalText = text;
        Layout layout = getLayout();
        if (layout == null || !layout.getText().equals(mOriginalText)) {
            super.setText(mOriginalText, type);
            layout = getLayout();
        }
        if (layout == null) {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    measureSubText(getLayout(),type);
                }
            });
        } else {
            measureSubText(layout,type);
        }


    }

    private void measureSubText(Layout layout, final TextView.BufferType type) {
        if (layout.getLineCount() > mShowMaxLine) {
            SpannableStringBuilder span = new SpannableStringBuilder();
            int start = layout.getLineStart(mShowMaxLine - 1);
            int end = layout.getLineVisibleEnd(mShowMaxLine - 1);

            TextPaint paint = getPaint();
            StringBuilder builder = new StringBuilder(ELLIPSIZE_END);

            float textWidth = paint.measureText(builder.toString());
            end -= paint.breakText(mOriginalText, start, end, false, textWidth, null)+1;

            CharSequence ellipsize = mOriginalText.subSequence(0, end);
            span.append(ellipsize);
            span.append(ELLIPSIZE_END);
            super.setText(span, type);
        }else{
            super.setText(mOriginalText, type);
        }


    }

}
