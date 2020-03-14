package CustomView;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.example.mvpmymusic.R;

import Util.CommonUtils;
import Util.Constant;
import Util.Currsong;

public class DiscView extends RelativeLayout {

    /*唱针当前所处的状态*/
    private enum NeedleAnimatorStatus {
        /*移动时：从唱盘往远处移动*/
        TO_FAR_END,
        /*移动时：从远处往唱盘移动*/
        TO_NEAR_END,
        /*静止时：离开唱盘，表示暂停*/
        IN_FAR_END,
        /*静止时：贴近唱盘，表示正在播放*/
        IN_NEAR_END
    }

    ImageView needle;//指针

    ObjectAnimator needleAnimator;//指针动画

    private ObjectAnimator objectAnimator;


    //是否在暂停歌曲的情况下切歌
    private boolean pauseToNextSong=false;

    public static final int DURATION_NEEDLE_ANIAMTOR = 250;

    //初始的指针状态
    private NeedleAnimatorStatus needleAnimatorStatus = NeedleAnimatorStatus.IN_FAR_END;

    //屏幕宽高
    private int mScreenWidth, mScreenHeight;

    public DiscView(Context context) {
        this(context, null);
    }

    public DiscView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DiscView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScreenWidth = CommonUtils.getScreenWidth(context);
        mScreenHeight = CommonUtils.getScreenHeight(context);
    }

    @Override
    public void onFinishInflate(){
        super.onFinishInflate();
        initDisc();
        initNeedle();
        initObjectAnimator();
    }

    //初始化唱片
    public void initDisc(){
        //给唱片设置图片，旋转的animator
        ImageView discBackground=findViewById(R.id.iv_disc_background);
        objectAnimator=getDiscObjectAnimator(discBackground);
        discBackground.setImageDrawable(getDiscDrawable(BitmapFactory.decodeResource(getResources(),R.drawable.default_disc)));//初始化，暂时没有专辑图片，先用这张图作为默认图

        //给唱片设置一个marginTop
        int marginTop = (int) (CommonUtils.SCALE_DISC_MARGIN_TOP * mScreenHeight);
        LayoutParams layoutParams = (LayoutParams) discBackground
                .getLayoutParams();
        layoutParams.setMargins(0, marginTop, 0, 0);
        discBackground.setLayoutParams(layoutParams);
    }

    private void initNeedle() {
        needle =  findViewById(R.id.iv_needle);

        //指针的宽高
        int needleWidth = (int) (CommonUtils.SCALE_NEEDLE_WIDTH * mScreenWidth);
        int needleHeight = (int) (CommonUtils.SCALE_NEEDLE_HEIGHT * mScreenHeight);

        /*设置手柄的外边距为负数，让其隐藏一部分*/
        int marginTop = (int) (CommonUtils.SCALE_NEEDLE_MARGIN_TOP * mScreenHeight) * -1;
        int marginLeft = (int) (CommonUtils.SCALE_NEEDLE_MARGIN_LEFT * mScreenWidth);

        Bitmap originBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_needle);
        Bitmap bitmap = Bitmap.createScaledBitmap(originBitmap, needleWidth, needleHeight, false);//创建一张bitmap，宽高为needleWidth，needleHeight

        LayoutParams layoutParams = (LayoutParams) needle.getLayoutParams();
        layoutParams.setMargins(marginLeft, marginTop, 0, 0);

        int pivotX = (int) (CommonUtils.SCALE_NEEDLE_PIVOT_X * mScreenWidth);
        int pivotY = (int) (CommonUtils.SCALE_NEEDLE_PIVOT_Y * mScreenWidth);

        //设置指针轴点的坐标
        needle.setPivotX(pivotX);
        needle.setPivotY(pivotY);

        needle.setRotation(CommonUtils.ROTATION_INIT_NEEDLE);//设置指针旋转角度
        needle.setImageBitmap(bitmap);
        needle.setLayoutParams(layoutParams);
    }

    private void initObjectAnimator() {
        //指针的旋转动画，从-30°转到0
        needleAnimator = ObjectAnimator.ofFloat(needle, View.ROTATION, CommonUtils
                .ROTATION_INIT_NEEDLE, 0);
        needleAnimator.setDuration(DURATION_NEEDLE_ANIAMTOR);
        needleAnimator.setInterpolator(new AccelerateInterpolator());
        needleAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                /**
                 * 根据动画开始前NeedleAnimatorStatus的状态，
                 * 即可得出动画进行时NeedleAnimatorStatus的状态
                 **/
                Log.i("DiscView","onAnimationStart:"+Currsong.getSTATUS()+needleAnimatorStatus.toString());
                if (needleAnimatorStatus == NeedleAnimatorStatus.IN_FAR_END) {
                    needleAnimatorStatus = NeedleAnimatorStatus.TO_NEAR_END;
                } else if (needleAnimatorStatus == NeedleAnimatorStatus.IN_NEAR_END) {
                    needleAnimatorStatus = NeedleAnimatorStatus.TO_FAR_END;
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {

                if (needleAnimatorStatus == NeedleAnimatorStatus.TO_NEAR_END) {
                    needleAnimatorStatus = NeedleAnimatorStatus.IN_NEAR_END;//正在播放歌曲
                    playDiscAnimator();//唱片开始转动
                } else if (needleAnimatorStatus == NeedleAnimatorStatus.TO_FAR_END) {
                    needleAnimatorStatus = NeedleAnimatorStatus.IN_FAR_END;
                }
                Log.i("DiscView","onAnimationEnd "+needleAnimatorStatus.toString());
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
    }


    public ObjectAnimator getDiscObjectAnimator(ImageView disc) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(disc, View.ROTATION, 0, 360);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setDuration(30 * 1000);//30秒转一圈
        objectAnimator.setInterpolator(new LinearInterpolator());
        return objectAnimator;
    }

    /**
     * 得到唱盘图片
     * 唱盘图片由空心圆盘及音乐专辑图片“合成”得到
     *
     */
    public Drawable getDiscDrawable(Bitmap bitmap) {
        int discSize = (int) (mScreenWidth * CommonUtils.SCALE_DISC_SIZE);//比例设计
        int musicPicSize = (int) (mScreenWidth * CommonUtils.SCALE_MUSIC_PIC_SIZE);

        Bitmap bitmapDisc = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R
                .drawable.ic_disc), discSize, discSize, false);
        Bitmap bitmapMusicPic = Bitmap.createScaledBitmap(bitmap, musicPicSize, musicPicSize, true);

        BitmapDrawable discDrawable = new BitmapDrawable(bitmapDisc);//黑色圆圈
        RoundedBitmapDrawable roundMusicDrawable = RoundedBitmapDrawableFactory.create
                (getResources(), bitmapMusicPic);

        //抗锯齿
        discDrawable.setAntiAlias(true);
        roundMusicDrawable.setAntiAlias(true);

        Drawable[] drawables = new Drawable[2];
        drawables[0] = roundMusicDrawable;//在后面
        drawables[1] = discDrawable;//在前面

        LayerDrawable layerDrawable = new LayerDrawable(drawables);
        int musicPicMargin = (int) ((CommonUtils.SCALE_DISC_SIZE - CommonUtils
                .SCALE_MUSIC_PIC_SIZE) * mScreenWidth / 2);
        //调整专辑图片的四周边距，让其显示在正中
        layerDrawable.setLayerInset(0, musicPicMargin, musicPicMargin, musicPicMargin,
                musicPicMargin);

        return layerDrawable;//将这个图层返回给discbackground
    }

    /*播放动画*/
    private void playAnimator() {
        /*唱针处于远端时，直接播放动画*/
        if (needleAnimatorStatus == NeedleAnimatorStatus.IN_FAR_END) {
            needleAnimator.start();
        }
    }

    /*暂停动画*/
    private void pauseAnimator() {
        /*播放时暂停动画*/
        if (needleAnimatorStatus == NeedleAnimatorStatus.IN_NEAR_END) {
            pauseDiscAnimator();
        }
        /*唱针往唱盘移动时暂停动画*/
        else if (needleAnimatorStatus == NeedleAnimatorStatus.TO_NEAR_END) {
            needleAnimator.reverse();
            /*
             若动画在没结束时执行reverse方法，则不会执行监听器的Start方法，此时需要手动设置
             */
            needleAnimatorStatus = NeedleAnimatorStatus.TO_FAR_END;
        }
    }


    /*播放唱盘动画*/
    private void playDiscAnimator() {
        if(objectAnimator.isPaused()&&pauseToNextSong) {
            objectAnimator.start();
            pauseToNextSong=false;
        }
        else if(objectAnimator.isPaused())
            objectAnimator.resume();
        else{
            objectAnimator.pause();
            objectAnimator.start();
        }
    }

    /*暂停唱盘动画*/
    private void pauseDiscAnimator() {
        needleAnimatorStatus = NeedleAnimatorStatus.TO_FAR_END;
        objectAnimator.pause();
        needleAnimator.reverse();//反方向执行
    }

    public void play() {
        playAnimator();
    }
    public void pause() {
        pauseAnimator();
    }
    public void restart1() {
//        restartAnimator();
        needleAnimator.reverse();
        objectAnimator.start();
        objectAnimator.pause();
    }
    public void restart2(){
        needleAnimator.start();
        objectAnimator.resume();
    }


}
