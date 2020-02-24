package CustomView;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;

import com.example.mvpmymusic.R;

import java.lang.annotation.Target;

public class CustomRelativeLayout extends RelativeLayout {

    private final int DURATION_ANIMATION = 1000;
    private final int INDEX_BACKGROUND = 0;
    private final int INDEX_FOREGROUND = 1;
    /**
     * LayerDrawable[0]: background drawable
     * LayerDrawable[1]: foreground drawable
     */
    private LayerDrawable layerDrawable;
    private ObjectAnimator objectAnimator;

    public CustomRelativeLayout (Context context) {
        this(context, null);
    }

    public CustomRelativeLayout (Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomRelativeLayout (Context context, AttributeSet attrs, int
            defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    //初始化层次Drawable
    public void initLayerDrawable(Drawable drawable) {
        Drawable backgroundDrawable = getContext().getDrawable(R.drawable.ic_blackground);
        Drawable[] drawables = new Drawable[2];

        /*初始化时先将前景与背景颜色设为一致*/
        drawables[INDEX_BACKGROUND] = drawable;
        drawables[INDEX_FOREGROUND]= backgroundDrawable;

        //LayerDrawable表示一种层次化的Drawable集合，通俗的说就是多个Drawable叠加起来的结果
        layerDrawable = new LayerDrawable(drawables);
    }

    public void initObjectAnimator() {
        //实例化objectAnimator为从0到1变化的float值
        objectAnimator = ObjectAnimator.ofFloat(this, "number", 1.0f, 0.98f);
        objectAnimator.setDuration(DURATION_ANIMATION);//设置动画时间为0.5s
        objectAnimator.setInterpolator(new AccelerateInterpolator());
        objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int foregroundAlpha = (int) ((float) animation.getAnimatedValue() * 255);//这样，foreGroundAlpha的值会在0.5秒内从0匀速上升到255
                /*动态设置Drawable的透明度，让前景图逐渐显示*/
                layerDrawable.getDrawable(INDEX_FOREGROUND).setAlpha(foregroundAlpha);
                CustomRelativeLayout.this.setBackground(layerDrawable);//设置自定义view的背景
            }
        });
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }
    @TargetApi(23)
    public void setForeground(Drawable drawable) {
        layerDrawable.setDrawable(INDEX_FOREGROUND, drawable);
    }

    @TargetApi(23)
    public void setBackground1(Drawable drawable){
        Log.i("CustomRelativeLayout","setBackGround called");
        layerDrawable.setDrawable(INDEX_BACKGROUND,drawable);
    }

    //对外提供方法，用于开始渐变动画
    public void beginAnimation() {
        objectAnimator.start();
    }
}
