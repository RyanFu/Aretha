/*
 * Copyright 2012 Tang Ke
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 *        
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aretha.widget;

import com.aretha.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * {@link PageIndicator} is a view for developer to indicate current page or
 * others which need to show the index to user.
 * 
 * @author Tank
 * 
 */
public class PageIndicator extends View {
	private int mActiveDotIndex;
	private int mDotNumber;
	private float mDotRadius;
	private float mDotSpacing;
	private int mDotColor;

	private Paint mPaint;

	private float[] mDownPoint;
	private OnPageChangeListener mOnPageChangeListener;
	private int mTouchSlop;

	public PageIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.PageIndicator);

		mActiveDotIndex = a.getInt(R.styleable.PageIndicator_activePage, 0);
		mDotNumber = a.getInt(R.styleable.PageIndicator_pageCount, 0);
		mDotRadius = a.getDimension(R.styleable.PageIndicator_dotRadius, 6);
		mDotSpacing = a.getDimension(R.styleable.PageIndicator_dotSpacing, 12);
		mDotColor = a.getColor(R.styleable.PageIndicator_dotColor, Color.WHITE);

		a.recycle();
		initialize();
	}

	public PageIndicator(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PageIndicator(Context context) {
		this(context, null);
	}

	private void initialize() {
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(mDotColor);

		mTouchSlop = new ViewConfiguration().getScaledTouchSlop();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		final int width = getWidth();
		final int height = getHeight();
		final int dotNumber = mDotNumber;
		final float dotSpacing = mDotSpacing;
		final float dotRadius = mDotRadius;
		float spacingLeft = (width - dotNumber * mDotRadius * 2 - (dotNumber - 1)
				* dotSpacing) / 2;
		int spacingTop = (height - getPaddingTop() - getPaddingBottom()) / 2
				+ getPaddingTop();

		for (int index = 0; index < dotNumber; index++) {
			if (mActiveDotIndex == index) {
				mPaint.setAlpha(255);
			} else {
				mPaint.setAlpha(100);
			}

			canvas.drawCircle(getPaddingLeft() + spacingLeft + index
					* dotRadius * 2 + index * dotSpacing + dotRadius,
					spacingTop, dotRadius, mPaint);
		}

		super.onDraw(canvas);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);

		int measuredWidth = width;
		int measuredHeight = height;

		final float dotRadius = mDotRadius;

		if (widthMode == MeasureSpec.AT_MOST) {
			float dotWidth = dotRadius * 2;
			measuredWidth = Math.round(Math.min(mDotNumber * dotWidth
					+ (mDotNumber - 1) * mDotSpacing + getPaddingLeft()
					+ getPaddingRight(), measuredWidth));
		}

		if (heightMode == MeasureSpec.AT_MOST) {
			measuredHeight = Math.round(Math.min(2 * dotRadius
					+ getPaddingTop() + getPaddingBottom(), measuredHeight));
		}

		setMeasuredDimension(resolveSize(measuredWidth, widthMeasureSpec),
				resolveSize(measuredHeight, heightMeasureSpec));
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final OnPageChangeListener onPageChangeListener = mOnPageChangeListener;

		int action = event.getAction();
		float y = event.getY();
		float x = event.getX();
		int touchSlop = mTouchSlop;

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (mDownPoint == null) {
				mDownPoint = new float[2];
			}
			mDownPoint[0] = x;
			mDownPoint[1] = y;
			return true;
		case MotionEvent.ACTION_UP:
			if (x - mDownPoint[0] <= touchSlop
					&& y - mDownPoint[1] <= touchSlop) {
				int pageIndex = mActiveDotIndex;
				if (x < getLeft() + getWidth() / 2) {
					if (onPageChangeListener != null && pageIndex > 0) {
						onPageChangeListener.onPrevPage();
					}
					pageIndex--;
				} else {
					if (onPageChangeListener != null
							&& pageIndex < Math.max(0, mDotNumber - 1)) {
						onPageChangeListener.onNextPage();
					}
					pageIndex++;
				}
				setActivePage(pageIndex);
			}
		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	public void setActivePage(int index) {
		int resolveIndex = Math.max(0, Math.min(index, mDotNumber - 1));

		if (mOnPageChangeListener != null
				&& resolveIndex != this.mActiveDotIndex) {
			mOnPageChangeListener.onPageChange(this.mActiveDotIndex);
		}

		this.mActiveDotIndex = resolveIndex;
		invalidate();

	}

	public int getActivePageIndex() {
		return mActiveDotIndex;
	}

	public void setPageNumber(int number) {
		this.mDotNumber = Math.max(0, number);
		setActivePage(this.mActiveDotIndex);
		invalidate();
	}

	public int getPageNumber() {
		return this.mDotNumber;
	}

	public void setDotColor(int color) {
		this.mDotColor = color;
		mPaint.setColor(color);
		invalidate();
	}

	public int getDotColor() {
		return this.mDotColor;
	}

	/**
	 * Set the radius of every dot(in scaled pixel)
	 * 
	 * @param radius
	 */
	public void setDotRadius(float radius) {
		this.mDotRadius = radius;
		requestLayout();
		invalidate();
	}

	/**
	 * Set the radius of every dot
	 * 
	 * @param radius
	 * @param unit
	 *            See {@link TypedValue}
	 */
	public void setDotRadius(float radius, int unit) {
		final Resources resources = getContext().getResources();
		this.mDotRadius = TypedValue.applyDimension(unit, radius,
				resources.getDisplayMetrics());
		requestLayout();
		invalidate();
	}

	/**
	 * 
	 * @return the dot radius(in pixel)
	 */
	public float getDotRadius() {
		return this.mDotRadius;
	}

	/**
	 * Set dot spacing(in scaled pixel)
	 * 
	 * @param spacing
	 */
	public void setDotSpacing(float spacing) {
		setDotSpacing(spacing, TypedValue.COMPLEX_UNIT_DIP);
	}

	/**
	 * Set dot spacing
	 * 
	 * @param spacing
	 * @param unit
	 *            See {@link TypedValue}
	 */
	public void setDotSpacing(float spacing, int unit) {
		final Resources resources = getContext().getResources();
		this.mDotSpacing = TypedValue.applyDimension(unit, spacing,
				resources.getDisplayMetrics());
		invalidate();
	}

	/**
	 * 
	 * @return the dot spacing(in pixel)
	 */
	public float getDotSpacing() {
		return this.mDotSpacing;
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		SavedState savedState = new SavedState(super.onSaveInstanceState());

		savedState.activeDotIndex = mActiveDotIndex;
		savedState.dotNumber = mDotNumber;
		savedState.dotRadius = mDotRadius;
		savedState.dotSpacing = mDotSpacing;
		savedState.dotColor = mDotColor;

		return savedState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());

		mActiveDotIndex = savedState.activeDotIndex;
		mDotNumber = savedState.dotNumber;
		mDotRadius = savedState.dotRadius;
		mDotSpacing = savedState.dotSpacing;
		setDotColor(savedState.dotColor);
	}

	public OnPageChangeListener getOnPageChangeListener() {
		return mOnPageChangeListener;
	}

	public void setOnPageChangeListener(
			OnPageChangeListener onPageChangeListener) {
		this.mOnPageChangeListener = onPageChangeListener;
	}

	static class SavedState extends BaseSavedState {
		int activeDotIndex;
		float dotRadius;
		float dotSpacing;
		int dotNumber;
		int dotColor;

		public SavedState(Parcel source) {
			super(source);
			activeDotIndex = source.readInt();
			dotRadius = source.readInt();
			dotSpacing = source.readInt();
			dotNumber = source.readInt();
			dotColor = source.readInt();
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(activeDotIndex);
			dest.writeFloat(dotRadius);
			dest.writeFloat(dotSpacing);
			dest.writeInt(dotNumber);
			dest.writeInt(dotColor);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Creator<PageIndicator.SavedState>() {

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}

			@Override
			public SavedState createFromParcel(Parcel source) {
				return new SavedState(source);
			}
		};
	}

	/**
	 * Callback for page index changed.
	 * 
	 * @author Tank
	 * 
	 */
	public static interface OnPageChangeListener {
		public void onPageChange(int pageIndex);

		public void onNextPage();

		public void onPrevPage();
	}
}
