package CustomView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class DownloadBar extends View {

    private static final String TAG = "DownloadBar";

    private float percent = 0;

    private Paint mPaint;
    private Paint mProgressPaint;

    public DownloadBar(Context context){
        super(context);
        init();
    }

    public DownloadBar(Context context, AttributeSet attrs){
        super(context,attrs);
        init();
    }

    public DownloadBar(Context context,AttributeSet attrs,int defStyle){
        super(context,attrs,defStyle);
        init();
    }

    private void init(){
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth((float)10.0);


        mProgressPaint = new Paint();
        mProgressPaint.setColor(Color.RED);
        mProgressPaint.setStrokeWidth((float)10.0);
    }


    @Override
    public void onMeasure(int widthMeasureSpec,int heightMeasureSpec){

        int widthResult = 0;
        int heightResult = 0;

        int widthSpecSize=MeasureSpec.getSize(widthMeasureSpec);
        int widthSpecMode=MeasureSpec.getMode(widthMeasureSpec);

        switch(widthSpecMode){
            case MeasureSpec.EXACTLY:
                widthResult = widthSpecSize;
                break;
            case MeasureSpec.AT_MOST:
                widthResult=200;
                break;
            default:
                break;
        }

        int heightSpecSize=MeasureSpec.getSize(heightMeasureSpec);
        int heightSpecMode=MeasureSpec.getMode(heightMeasureSpec);

        switch(heightSpecMode){
            case MeasureSpec.EXACTLY: {
                heightResult = heightSpecSize;
                break;
            }
            case MeasureSpec.AT_MOST:{
                heightResult=20;
                break;
            }
            default:
                break;
        }

        setMeasuredDimension(widthResult,heightResult);
    }

    @Override
    public void onDraw(Canvas canvas){
        int width = getWidth();
        int height = getHeight();

        int left=getLeft();
        int top=getTop();

        Log.i(TAG,left+" "+top);
        Log.i(TAG,width+" "+height);

        //先画进度条，再画当前进度
        canvas.drawLine(0,0,width,0,mPaint);
        canvas.drawLine(0,0,percent*width,0,mProgressPaint);


    }

    public synchronized void setProgress(float progress){
        this.percent = progress;
        invalidate();
    }

}
