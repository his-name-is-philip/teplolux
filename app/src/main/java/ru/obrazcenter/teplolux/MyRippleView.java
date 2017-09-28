// TODO: disable animation in selected RippleView
package ru.obrazcenter.teplolux;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;


import static android.animation.ValueAnimator.ofObject;
import static android.graphics.Color.TRANSPARENT;
import static android.support.v4.content.ContextCompat.getColor;
import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;
import static ru.obrazcenter.teplolux.StartActivity.activity1;
import static ru.obrazcenter.teplolux.StartActivity.isInSelectedItemMode;
import static ru.obrazcenter.teplolux.StartActivity.theProject;

public class MyRippleView extends RelativeLayout {
    private int WIDTH;
    private int HEIGHT;
    private int frameRate = 10;
    private int rippleDuration = 400;
    private int rippleAlpha = 90;
    private Handler canvasHandler;
    private float radiusMax = 0;
    private boolean animationRunning = false;
    private int timer = 0;
    private int timerEmpty = 0;
    private int durationEmpty = -1;
    private float x = -1;
    private float y = -1;
    private int zoomDuration;
    private float zoomScale;
    private ScaleAnimation scaleAnimation;
    private Boolean hasToZoom;
    private Boolean isCentered;
    private Integer rippleType;
    private Paint paint;
    private Bitmap originBitmap;
    private int rippleColor;
    private int ripplePadding;
    private GestureDetector gestureDetector;
    private final Runnable invalidateRunnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

    private OnRippleCompleteListener onCompletionListener;

    boolean pressed; // only for background
    boolean longPressed = false; // only for background
    static MyRippleView pressedItem;

    private ValueAnimator colorAnimator;

    public MyRippleView(Context context) {
        super(context);
    }

    public MyRippleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MyRippleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    /**
     * Method that initializes all fields and sets listeners
     *
     * @param context Context used to create this view
     * @param attrs   Attribute used to initialize fields
     */
    private void init(final Context context, final AttributeSet attrs) {
        if (isInEditMode())
            return;

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleView);
        //noinspection deprecation
        rippleColor = typedArray.getColor(R.styleable.RippleView_rv_color, getResources()
                .getColor(R.color.rippelColor));
        rippleType = typedArray.getInt(R.styleable.RippleView_rv_type, 0);
        hasToZoom = typedArray.getBoolean(R.styleable.RippleView_rv_zoom, false);
        isCentered = typedArray.getBoolean(R.styleable.RippleView_rv_centered, false);
        rippleDuration = typedArray.getInteger(R.styleable.RippleView_rv_rippleDuration, rippleDuration);
        frameRate = typedArray.getInteger(R.styleable.RippleView_rv_framerate, frameRate);
        rippleAlpha = typedArray.getInteger(R.styleable.RippleView_rv_alpha, rippleAlpha);
        ripplePadding = typedArray.getDimensionPixelSize(R.styleable.RippleView_rv_ripplePadding, 0);
        canvasHandler = new Handler();
        zoomScale = typedArray.getFloat(R.styleable.RippleView_rv_zoomScale, 1.03f);
        zoomDuration = typedArray.getInt(R.styleable.RippleView_rv_zoomDuration, 200);
        typedArray.recycle();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(rippleColor);
        paint.setAlpha(rippleAlpha);
        setWillNotDraw(false);
        this.setDrawingCacheEnabled(true);
        this.setClickable(true);

        gestureDetector = new GestureDetector(context, new SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent event) {
                super.onLongPress(event);
                longPressed = true;
                if (colorAnimator != null) colorAnimator.cancel();
                if (!isInSelectedItemMode) {
                    activity1.startSelectedItemMode(MyRippleView.this);
                } else {
                    activity1.stopSelectedItemMode();
                    if (activity1.selectedRL.getId() != getId())
                        activity1.startSelectedItemMode(MyRippleView.this);
                }
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                super.onSingleTapConfirmed(e);
                if (isInSelectedItemMode) {
                    activity1.stopSelectedItemMode();
                    if (activity1.selectedRL.getId() != getId()) {
                        //Получение текста из name TextView
                        String name = ((TextView) findViewById(R.id.name_tv)).getText().toString();
                        if (activity1.p())
                            activity1.loadProject(name, true, true);
                        else activity1.loadRoom(name);
                    }
                } //Получение текста из name TextView
                String name = ((TextView) findViewById(R.id.name_tv)).getText().toString();
                if (theProject == null)
                    activity1.loadProject(name, true, true);
                else
                    activity1.loadRoom(name);
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return super.onDown(e);
            }
        });
    }

    private Rect touchedRect;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        super.onTouchEvent(e);
        gestureDetector.onTouchEvent(e);
        int action = e.getAction();
        switch (action) {
            case ACTION_DOWN:
                if (!isInSelectedItemMode) {
                    pressed = true;
                    pressedItem = this;
                    animateRipple(e);
                    setBackgroundColor(getColor(activity1, R.color.pressedBackground));
                    touchedRect = new Rect(getLeft(), getTop(), getRight(), getBottom());
                }
                Log.d("RecyclerItemTouchEvent", "ACTION_DOWN");
                break;
//                case MotionEvent.ACTION_CANCEL:
//                case MotionEvent.ACTION_UP:
//                    final ValueAnimator colorAnimator = ofObject(new ArgbEvaluator(),
//                            getColor(context, R.color.pressedBackground), TRANSPARENT);
//                    colorAnimator.setDuration(1000).addUpdateListener(new AnimatorUpdateListener() {
//                        @Override
//                        public void onAnimationUpdate(ValueAnimator a) {
//                            setBackgroundColor((int) a.getAnimatedValue());
//                        }
//                    });
//                return true;
            case ACTION_MOVE:
                if (touchedRect == null
                        || touchedRect.contains((int) e.getX(), (int) e.getY())) {
                    break;
                }
            case ACTION_UP:
            case ACTION_CANCEL:
                if (!isInSelectedItemMode)
                    animBackground(((ColorDrawable) getBackground()).getColor(), TRANSPARENT);
                Log.d("RecyclerItemTouchEvent", String.format("%d fromMyRipple", e.getAction()));
                return false;

            default:
                Log.d("RecyclerItemTouchEvent", String.format("%d fromMyRipple", e.getAction()));
                break;
        }
        return true;
    }

    private void animBackground(int color1, int color2) {
        colorAnimator = ofObject(new ArgbEvaluator(),
                color1, color2);
        colorAnimator.setDuration(1000).addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator a) {
                setBackgroundColor((int) a.getAnimatedValue());
            }
        });
        colorAnimator.start();
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (animationRunning) {
            if (rippleDuration <= timer * frameRate) {
                animationRunning = false;
                timer = 0;
                durationEmpty = -1;
                timerEmpty = 0;
                canvas.restore();
                invalidate();
                if (onCompletionListener != null) onCompletionListener.onComplete(this);
                return;
            } else
                canvasHandler.postDelayed(invalidateRunnable, frameRate);

            if (timer == 0)
                canvas.save();


            canvas.drawCircle(x, y, radiusMax * (((float) timer * frameRate) / rippleDuration), paint);

            paint.setColor(Color.parseColor("#ffff4444"));

            if (rippleType == 1 && originBitmap != null && (((float) timer * frameRate) / rippleDuration) > 0.4f) {
                if (durationEmpty == -1)
                    durationEmpty = rippleDuration - timer * frameRate;

                timerEmpty++;
                final Bitmap tmpBitmap = getCircleBitmap((int) (radiusMax * (((float) timerEmpty * frameRate) / durationEmpty)));
                canvas.drawBitmap(tmpBitmap, 0, 0, paint);
                tmpBitmap.recycle();
            }

            paint.setColor(rippleColor);

            if (rippleType == 1) {
                if ((((float) timer * frameRate) / rippleDuration) > 0.6f)
                    paint.setAlpha((int) (rippleAlpha - (rippleAlpha * (((float) timerEmpty * frameRate) / durationEmpty))));
                else
                    paint.setAlpha(rippleAlpha);
            } else
                paint.setAlpha((int) (rippleAlpha - (rippleAlpha * (((float) timer * frameRate) / rippleDuration))));

            timer++;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        WIDTH = w;
        HEIGHT = h;

        scaleAnimation = new ScaleAnimation(1.0f, zoomScale, 1.0f, zoomScale, w / 2, h / 2);
        scaleAnimation.setDuration(zoomDuration);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        scaleAnimation.setRepeatCount(1);
    }

    /**
     * Launch Ripple animation for the current view with a MotionEvent
     *
     * @param event MotionEvent registered by the Ripple gesture listener
     */
    public void animateRipple(MotionEvent event) {
        animateRipple(event.getX(), event.getY());
    }

    /**
     * Launch Ripple animation for the current view centered at x and y position
     *
     * @param x Horizontal position of the ripple center
     * @param y Vertical position of the ripple center
     */
    void animateRipple(final float x, final float y) {
        createAnimation(x, y);
    }

    /**
     * Create Ripple animation centered at x, y
     *
     * @param x Horizontal position of the ripple center
     * @param y Vertical position of the ripple center
     */
    private void createAnimation(final float x, final float y) {
        if (this.isEnabled() && !animationRunning) {
            if (hasToZoom)
                this.startAnimation(scaleAnimation);

            radiusMax = Math.max(WIDTH, HEIGHT);

            if (rippleType != 2)
                radiusMax /= 2;

            radiusMax -= ripplePadding;

            if (isCentered || rippleType == 1) {
                this.x = getMeasuredWidth() / 2;
                this.y = getMeasuredHeight() / 2;
            } else {
                this.x = x;
                this.y = y;
            }

            animationRunning = true;

            if (rippleType == 1 && originBitmap == null)
                originBitmap = getDrawingCache(true);

            invalidate();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
//        this.onTouchEvent(event);
        return super.onInterceptTouchEvent(event);
    }

    /**
     * Send a click event if parent view is a Listview instance
     */

    private Bitmap getCircleBitmap(final int radius) {
        final Bitmap output = Bitmap.createBitmap(originBitmap.getWidth(), originBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect((int) (x - radius), (int) (y - radius), (int) (x + radius), (int) (y + radius));

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(x, y, radius, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(originBitmap, rect, rect, paint);

        return output;
    }

    /**
     * Set Ripple color, default is #FFFFFF
     *
     * @param rippleColor New color resource
     */

    public void setRippleColor(@ColorRes int rippleColor) {
        //noinspection deprecation
        this.rippleColor = getResources().getColor(rippleColor);
    }

    public int getRippleColor() {
        return rippleColor;
    }

    public RippleType getRippleType() {
        return RippleType.values()[rippleType];
    }

    /**
     * Set Ripple type, default is RippleType.SIMPLE
     *
     * @param rippleType New Ripple type for next animation
     */
    public void setRippleType(final RippleType rippleType) {
        this.rippleType = rippleType.ordinal();
    }

    public Boolean isCentered() {
        return isCentered;
    }

    /**
     * Set if ripple animation has to be centered in its parent view or not, default is False
     */
    public void setCentered(final Boolean isCentered) {
        this.isCentered = isCentered;
    }

    public int getRipplePadding() {
        return ripplePadding;
    }

    /**
     * Set Ripple padding if you want to avoid some graphic glitch
     *
     * @param ripplePadding New Ripple padding in pixel, default is 0px
     */
    public void setRipplePadding(int ripplePadding) {
        this.ripplePadding = ripplePadding;
    }

    public Boolean isZooming() {
        return hasToZoom;
    }

    /**
     * At the end of Ripple effect, the child views has to zoom
     *
     * @param hasToZoom Do the child views have to zoom ? default is False
     */
    public void setZooming(Boolean hasToZoom) {
        this.hasToZoom = hasToZoom;
    }

    public float getZoomScale() {
        return zoomScale;
    }

    /**
     * Scale of the end animation
     *
     * @param zoomScale Value of scale animation, default is 1.03f
     */
    public void setZoomScale(float zoomScale) {
        this.zoomScale = zoomScale;
    }

    public int getZoomDuration() {
        return zoomDuration;
    }

    /**
     * Duration of the ending animation in ms
     *
     * @param zoomDuration Duration, default is 200ms
     */
    public void setZoomDuration(int zoomDuration) {
        this.zoomDuration = zoomDuration;
    }

    public int getRippleDuration() {
        return rippleDuration;
    }

    /**
     * Duration of the Ripple animation in ms
     *
     * @param rippleDuration Duration, default is 400ms
     */
    public void setRippleDuration(int rippleDuration) {
        this.rippleDuration = rippleDuration;
    }

    public int getFrameRate() {
        return frameRate;
    }

    /**
     * Set framerate for Ripple animation
     *
     * @param frameRate New framerate value, default is 10
     */
    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    public int getRippleAlpha() {
        return rippleAlpha;
    }

    /**
     * Set alpha for ripple effect color
     *
     * @param rippleAlpha Alpha value between 0 and 255, default is 90
     */
    public void setRippleAlpha(int rippleAlpha) {
        this.rippleAlpha = rippleAlpha;
    }

    public void setOnRippleCompleteListener(OnRippleCompleteListener listener) {
        this.onCompletionListener = listener;
    }

    /**
     * Defines a callback called at the end of the Ripple effect
     */
    interface OnRippleCompleteListener {
        void onComplete(MyRippleView rippleView);
    }

    private enum RippleType {
        SIMPLE(0),
        DOUBLE(1),
        RECTANGLE(2);

        int type;

        RippleType(int type) {
            this.type = type;
        }
    }
}