package xyz.hanks.library;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * click ripple effect for any view
 * Created by hanks on 15/12/23.
 */
public class SmallBang extends View {

    int[] colors = {0xFFDF4288, 0xFFCD8BF8,0XFF2B9DF2,0XFFA4EEB4,0XFFE097CA,0XFFCAACC6,0XFFC5A5FC,0XFFF5BC16,0XFFF2DFC8,0XFFE1BE8E,0XFFC8C79D};
    private long ANIMATE_DURATION = 1000;
    private float MAX_RADIUS = 150;
    private float MAX_CIRCLE_RADIUS = 100;
    private float progress;
    private Paint circlePaint;
    private float RING_WIDTH = 10;
    private float P1 = 0.15f;
    private float P2 = 0.28f;
    private float P3 = 0.30f;
    private int DOT_NUMBER = 16;
    private float DOT_BIG_RADIUS = 8;
    private float DOT_SMALL_RADIUS = 5;


    // 将下面的view变小
    // 画圆半径从小到大,同时颜色渐变 (P1)
    // 当半径到达 MAX_RADIUS, 开始画空心圆,空闲圆半径变大,画笔宽度从MAX_RADIUS变小
    // 当空心圆半径达到 MAX_RADIUS - RINGWIDTH (P2), 此时变成圆环,在圆环上生成个DOT_NUMBER个小圆,均匀分布
    // 空心圆继续变大,逐渐圆环消失; 同时小圆向外扩散,扩散过程小圆半径减小,颜色渐变;同时下面的view逐渐变大 (P3)
    public SmallBang(Context context) {
        super(context);
        init(null, 0);
    }

    public SmallBang(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SmallBang(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SmallBang(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        //

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(Color.BLACK);

    }

    public void bang() {
        ValueAnimator valueAnimator = new ValueAnimator().ofFloat(0, 1).setDuration(ANIMATE_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                progress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.start();
        initDots();
    }

    List<Dot> dotList = new ArrayList<>();
    private void initDots(){

        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < DOT_NUMBER*2; i++) {
            Dot dot = new Dot();
            dot.startColor = colors[random.nextInt(99999) % colors.length];
            dot.endColor = colors[random.nextInt(99999) % colors.length];
            dotList.add(dot);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {

        Log.d("SmallBang", "draw" + progress);

        if(progress>=0 && progress<=P1) {
            float progress1 = 1f / P1 * progress;
            if (progress1 > 1) progress1 = 1;
            int startColor = colors[0];
            int endColor = colors[1];
            circlePaint.setStyle(Paint.Style.FILL);
            circlePaint.setColor(evaluateColor(startColor, endColor, progress1));
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, MAX_CIRCLE_RADIUS * progress1, circlePaint);
        }else if(progress>P1){

            if(progress>P1 && progress<=P3) {
                float progress2 = (progress - P1) / (P3 - P1);
                if (progress2 < 0) progress2 = 0;
                if (progress2 > 1) progress2 = 1;

                Log.d("SmallBang", "draw2======" + progress2);
                circlePaint.setStyle(Paint.Style.STROKE);
                float strokeWidth = (MAX_CIRCLE_RADIUS) * (1 - progress2);
                circlePaint.setStrokeWidth(strokeWidth);

                canvas.drawCircle(getWidth() / 2, getHeight() / 2, (MAX_CIRCLE_RADIUS) * progress2 + strokeWidth / 2, circlePaint);
            }
            if (progress >= P2) {
                circlePaint.setStyle(Paint.Style.FILL);
                float progress3 = (progress - P2) / (1 - P2);
                float r = MAX_CIRCLE_RADIUS + progress3 * (MAX_RADIUS - MAX_CIRCLE_RADIUS);


                for (int i = 0; i < dotList.size(); i += 2) {
                    Dot dot = dotList.get(i);
                    circlePaint.setColor(evaluateColor(dot.startColor, dot.endColor, progress3));

                    float x = (float) (r * Math.cos(i * 2 * Math.PI / DOT_NUMBER)) + getWidth() / 2;
                    float y = (float) (r * Math.sin(i * 2 * Math.PI / DOT_NUMBER)) + getHeight() / 2;
                    canvas.drawCircle(x, y, DOT_BIG_RADIUS * (1 - progress3), circlePaint);

                    Dot dot2 = dotList.get(i + 1);

                    circlePaint.setColor(evaluateColor(dot2.startColor, dot2.endColor, progress3));
                    float x2 = (float) (r * Math.cos(i * 2 * Math.PI / DOT_NUMBER + 0.2)) + getWidth() / 2;
                    float y2 = (float) (r * Math.sin(i * 2 * Math.PI / DOT_NUMBER + 0.2)) + getHeight() / 2;
                    canvas.drawCircle(x2, y2, DOT_SMALL_RADIUS * (1 - progress3), circlePaint);

                }
            }
        }
    }


    class Dot {
        int startColor;
        int endColor;
    }

    private int evaluateColor(int startValue, int endValue, float fraction) {
        if (fraction <= 0) {
            return startValue;
        }
        if (fraction >= 1) {
            return endValue;
        }
        int startInt = startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return ((startA + (int) (fraction * (endA - startA))) << 24) | ((startR + (int) (fraction * (endR - startR))) << 16) | ((startG + (int) (fraction * (endG - startG))) << 8) | ((startB + (int) (fraction * (endB - startB))));
    }
}