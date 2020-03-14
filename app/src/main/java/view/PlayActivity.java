package view;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.mvpmymusic.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import CustomView.CustomRelativeLayout;
import CustomView.DiscView;
import CustomView.DownloadBar;
import Util.CommonUtils;
import Util.Constant;
import Util.Currsong;
import bean.CurrentSong;
import bean.LoveSong;
import bean.OnlineSong;
import bean.RecentSong;
import bean.SaveSong;
import butterknife.Bind;
import butterknife.ButterKnife;
import event.PlayingStatusEvent;
import service.DownloadService;
import service.PlayService;

import static org.litepal.LitePalApplication.getContext;

public class PlayActivity extends AppCompatActivity implements IPlayView{

    private PlayService.PlayBinder playBinder;
    private DownloadService.DownloadBinder downloadBinder;
    MediaPlayer mp=new MediaPlayer();
    Thread seekbarThread;
    Bitmap albumBitmap;
    BroadcastReceiver broadcastReceiver;

    @Bind(R.id.download_bar)
    DownloadBar downloadBar;

    @Bind(R.id.back)
    ImageView back;

    @Bind(R.id.play_pause_playactivity)
    ImageView play_pause;

    @Bind(R.id.last_song)
    ImageView last_song;

    @Bind(R.id.next_song)
    ImageView next_song;
//
    @Bind(R.id.songname_activityplay)
    TextView songname;
//
    @Bind(R.id.love)
    Button love;

    @Bind(R.id.download)
    Button download;
//
    @Bind(R.id.singername_activityplay)
    TextView singername;
//
    @Bind(R.id.seekbar)
    SeekBar seekBar;
//
    @Bind(R.id.current_progress)
    TextView currentProgress;

    @Bind(R.id.songlength)
    TextView songLength;

    @Bind(R.id.custom_relativelayout)
    CustomRelativeLayout customRelativeLayout;

    @Bind(R.id.disc_view)
    DiscView discView;

    @Bind(R.id.iv_disc_background)
    ImageView discBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        //状态栏透明化
        View decorView = getWindow().getDecorView();
        if (true) {
            if (Build.VERSION.SDK_INT >= 22) {
                Log.i("PlayActivity","called");
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                getWindow().setStatusBarColor(Color.TRANSPARENT);
            }
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            getWindow().setStatusBarColor(getContext().getResources().getColor(R.color.actionBarColor));
        }

        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        //绑定播放服务
        Intent playIntent = new Intent(PlayActivity.this,PlayService.class);
        bindService(playIntent,playConnection, Context.BIND_AUTO_CREATE);
        //绑定下载服务
        Intent downloadIntent=new Intent(PlayActivity.this, DownloadService.class);
        bindService(downloadIntent,downloadConnection,Context.BIND_AUTO_CREATE);


        currentProgress.setText(MinuteAndSecond(PlayService.mp.getCurrentPosition()/1000));
        seekBar.setMax(PlayService.mp.getDuration());
        seekBar.setProgress(PlayService.mp.getCurrentPosition());
        init();
        setOnclick();
        setDiscImage();
        setLayoutBackground();
        if(Currsong.getSTATUS()==Constant.PLAYING)
            discView.play();
        if(isLoveSong(Currsong.getCurrentSong()))
            love.setBackgroundResource(R.drawable.loved);


        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction() == "com.example.mvpmymusic.downloadService"){
                    Message msg=Message.obtain();
                    msg.what=intent.getIntExtra("progress",0);
                    downloadHandler.sendMessage(msg);
                }
            }
        };
    }

    private Handler downloadHandler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            int progress = msg.what;
            if(progress<=100)
                downloadBar.setProgress(progress);
            else
                downloadBar.setVisibility(View.GONE);
        }
    };

    private ServiceConnection playConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {//activity与服务成功绑定时调用
            playBinder = (PlayService.PlayBinder)iBinder;
            Log.i("MainActivity","绑定播放服务");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {//activity与服务的连接断开时调用
        }
    };

    private ServiceConnection downloadConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            downloadBinder=(DownloadService.DownloadBinder)iBinder;
            Log.i("MainActivity","绑定下载服务");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    public void setDiscImage(){
        loadAlbumImage(Currsong.getCurrentSong());
    }

    public void setLayoutBackground(){
        BitmapDrawable drawable=null;
        String rootPath=getContext().getExternalFilesDir("")+"/mvpmymusic/albumimg/";
        Bitmap bitmap=BitmapFactory.decodeFile(rootPath+Currsong.getCurrentSong().getAlbummid()+".png");
        drawable=new BitmapDrawable(bitmap);
        customRelativeLayout.initLayerDrawable(drawable);
        customRelativeLayout.initObjectAnimator();
        customRelativeLayout.beginAnimation();
    }

    public void init(){
        switch(Currsong.getSTATUS()){
            case Constant.PREPARE:
                songname.setText(Currsong.getCurrentSong().getSongName());
                singername.setText(getSingerNames(Currsong.getCurrentSong()));
                songLength.setText(MinuteAndSecond(Currsong.getCurrentSong().getInterval()));
                play_pause.setImageResource(R.drawable.play_64);
                setDiscImage();
                break;
            case Constant.PLAYING:
                songname.setText(Currsong.getCurrentSong().getSongName());
                singername.setText(getSingerNames(Currsong.getCurrentSong()));
                songLength.setText(MinuteAndSecond(Currsong.getCurrentSong().getInterval()));
                play_pause.setImageResource(R.drawable.pause_64);
                setDiscImage();
                break;
            case Constant.PAUSE:
                songname.setText(Currsong.getCurrentSong().getSongName());
                singername.setText(getSingerNames(Currsong.getCurrentSong()));
                songLength.setText(MinuteAndSecond(Currsong.getCurrentSong().getInterval()));
                play_pause.setImageResource(R.drawable.play_64);
                setDiscImage();
                break;
        }
        if(Currsong.getSTATUS()!=Constant.NOTPLAYING){
            mp=PlayService.mp;
            seekBar.setMax(mp.getDuration());
            getProgress();
        }
    }




    public String getSingerNames(CurrentSong cs){
        String SingerNames="";
        for(int i=0;i<cs.getSingers().size();i++){
            SingerNames+=cs.getSingers().get(i);
            if(i!=cs.getSingers().size()-1)
                SingerNames+="/";
        }
        return SingerNames;
    }

    //输入歌曲的总秒数，返回xx：xx的格式（xx分xx秒）
    public String MinuteAndSecond(int interval){
        int minutes=interval/60;
        int seconds=interval%60;
        String res="";
        if(minutes<10){
            res+='0';
            res+=minutes;
        }else{
            res+=minutes;
        }
        res+=':';
        if(seconds<10){
            res+='0';
            res+=seconds;
        }else{
            res+=seconds;
        }
        return res;
    }


    //切换歌曲时更新UI
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshUI(PlayingStatusEvent event) {
        init();
        getProgress();
        setLayoutBackground();
        discView.restart2();
    }



    //根据播放状态更新进度条
    public void getProgress(){
        seekbarThread=new Thread(new Runnable() {
            @Override
            public void run() {
                while (Currsong.getSTATUS() == Constant.PLAYING) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessage(0);
                }
            }
        });
        seekbarThread.start();
    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message message){//更新UI，当前播放时间
            currentProgress.setText(MinuteAndSecond(mp.getCurrentPosition()/1000));
            seekBar.setProgress(mp.getCurrentPosition());
        }
    };

    private Handler handler2=new Handler(){
        @Override
        public void handleMessage(Message message){
            String path=(String)message.obj;
            Bitmap bitmap=BitmapFactory.decodeFile(path);
            Drawable drawable=discView.getDiscDrawable(bitmap);
            discBackground.setImageDrawable(drawable);
        }
    };

    public void setOnclick(){
        //收藏歌曲
        love.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isLoveSong(Currsong.getCurrentSong())) {
                    saveLoveSong(Currsong.getCurrentSong());
                    love.setBackgroundResource(R.drawable.loved);
                }else{
                    LitePal.deleteAll(LoveSong.class,"url = ?",Currsong.getCurrentSong().getUrl());
                    love.setBackgroundResource(R.drawable.love);
                }
            }
        });

        //下载歌曲
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadBar.setVisibility(View.VISIBLE);
                downloadBinder.startDownload(Currsong.getCurrentSong());
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //进度条的拖动事件
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean drag) {
                if(drag){//如果用户拖动了进度条,更新播放的位置
                    mp.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //播放暂停、切歌
        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(Currsong.getSTATUS()){
                    case Constant.PLAYING:
                        play_pause.setImageResource(R.drawable.play_64);
                        PlayService.mp.pause();
                        discView.pause();
                        Currsong.setSTATUS(Constant.PAUSE);
                        break;
                    case Constant.PAUSE:
                        play_pause.setImageResource(R.drawable.pause_64);
                        PlayService.mp.start();
                        discView.play();
                        Currsong.setSTATUS(Constant.PLAYING);
                        getProgress();
                        break;
                }
            }
        });

        last_song.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayService.mp.stop();
                PlayService.mp.reset();
                List<CurrentSong> currentSongList=LitePal.findAll(CurrentSong.class);
                int position;
                switch (Currsong.getSTATUS()){
                    case Constant.PLAYING:
                        position=Currsong.getCurrentSong().getPosition()-1;
                        if(position<0)
                            position= currentSongList.size()-1;
                        discView.restart1();
                        playBinder.play(currentSongList.get(position));
                        setDiscImage();
                        break;
                    case Constant.PAUSE:
                        position=Currsong.getCurrentSong().getPosition()-1;
                        if(position<0)
                            position= currentSongList.size()-1;
                        discView.restart1();
                        playBinder.play(currentSongList.get(position));
                        setDiscImage();
                        break;
                }

            }
        });

        next_song.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayService.mp.stop();
                PlayService.mp.reset();
                List<CurrentSong> currentSongList=LitePal.findAll(CurrentSong.class);
                int position;
                switch (Currsong.getSTATUS()){
                    case Constant.PLAYING:
                        position=Currsong.getCurrentSong().getPosition()+1;
                        if(position==currentSongList.size())
                            position= 0;
                        discView.restart1();
                        playBinder.play(currentSongList.get(position));
                        setDiscImage();
                        break;
                    case Constant.PAUSE:
                        position=Currsong.getCurrentSong().getPosition()+1;
                        if(position==currentSongList.size())
                            position= 0;
                        discView.restart1();
                        playBinder.play(currentSongList.get(position));
                        setDiscImage();
                        break;
                }
            }
        });
    }


    public void saveLoveSong(CurrentSong cs){
        List<LoveSong> songList=LitePal.findAll(LoveSong.class);
        LoveSong loveSong=new LoveSong();
        loveSong.setAlbummid(cs.getAlbummid());
        loveSong.setAlbumName(cs.getAlbumName());
        loveSong.setNeedPay(cs.isNeedPay());
        loveSong.setSingers(cs.getSingers());
        loveSong.setSongName(cs.getSongName());
        loveSong.setUrl(cs.getUrl());
        loveSong.setSongmId(cs.getSongmId());
        loveSong.setInterval(cs.getInterval());
        loveSong.setPlaying("no");
        if(!AlreadyExist(loveSong)){
            Log.i("PlayService","song don't exist");
            loveSong.setPosition(songList.size());
            loveSong.save();
        }
    }

    public boolean AlreadyExist(LoveSong lovesong){
        List<LoveSong> loveSongList=LitePal.findAll(LoveSong.class);
        if(loveSongList.size()==0)
            return false;
        else{
            for(LoveSong ls:loveSongList) {
                if (lovesong.getUrl().equals(ls.getUrl()))//用url来判断两首歌是否相同
                    return true;
            }
            return false;
        }
    }

    public void saveAlbumImageToLocal(final String url, final String path, final String albumid){
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap=null;
                HttpURLConnection connection=null;
                try{
                    URL u=new URL(url);
                    connection=(HttpURLConnection)u.openConnection();
                    connection.setRequestMethod("GET");//设置请求方法
                    connection.setConnectTimeout(5000);//设置连接服务器超时时间
                    connection.setReadTimeout(5000);//设置读取数据超时时间
                    connection.connect();//开始连接
                    int connectionCode=connection.getResponseCode();//得到服务器的响应码
                    if(connectionCode==200){//访问成功
                        InputStream is=connection.getInputStream();
                        bitmap = BitmapFactory.decodeStream(is);
                        FileOutputStream fileOutputStream=new FileOutputStream(path+albumid+".png");
                        bitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);
                        fileOutputStream.close();
                        Message msg=Message.obtain();
                        msg.obj=path+albumid+".png";
                        handler2.sendMessage(msg);
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }finally{
                    if(connection!=null)
                        connection.disconnect();//断开连接
                }
            }
        });
        t.start();
    }

    public void loadAlbumImage(CurrentSong cs) {
        String path = getApplicationContext().getExternalFilesDir("") + "/mvpmymusic/albumimg/";
        File file = new File(path);
        if (!file.exists()) {
            Log.i("MainActivity", "新建文件夹");
            file.mkdirs();
        }
        File imgFile = new File(file, cs.getAlbummid() + ".png");
        if (!imgFile.exists()) {//如果改图片在本地找不到，则发送请求保存到本地
            saveAlbumImageToLocal("http://y.gtimg.cn/music/photo_new/T002R180x180M000" + Currsong.getCurrentSong().getAlbummid() + ".png", path, cs.getAlbummid());
        } else {
            Bitmap bitmap=BitmapFactory.decodeFile(path+ Currsong.getCurrentSong().getAlbummid() + ".png");
            Drawable drawable=discView.getDiscDrawable(bitmap);
            discBackground.setImageDrawable(drawable);
        }
    }

    public boolean isLoveSong(CurrentSong cs){
        boolean res=false;
        List<LoveSong> loveSongList=LitePal.findAll(LoveSong.class);
        for(LoveSong ls:loveSongList){
            if(ls.getUrl().equals(cs.getUrl()))
                res=true;
        }
        return res;
    }

    @Override
    protected void onResume(){
        IntentFilter filter=new IntentFilter();
        filter.addAction("com.example.mvpmymusic.downloadService");
        registerReceiver(broadcastReceiver,filter);
        super.onResume();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        unbindService(playConnection);
        unbindService(downloadConnection);
        EventBus.getDefault().unregister(this);
    }


}
