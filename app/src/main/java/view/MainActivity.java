package view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mvpmymusic.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

import Util.CommonUtils;
import Util.Constant;
import Util.Currsong;
import bean.CurrentSong;
import bean.LoveSong;
import butterknife.Bind;
import butterknife.ButterKnife;
import event.PlayingStatusEvent;
import service.DownloadService;
import service.PlayService;
import view.fragment.LoveSongFragment;
import view.fragment.MainFragment;
import view.fragment.RecentPlayFragment;
import view.fragment.SearchFragment;
import view.fragment.SearchResultFragment;

public class MainActivity extends AppCompatActivity  {
    private MainFragment mainFragment;
    private SearchFragment searchFragment;
    private RecentPlayFragment recentPlayFragment;
    private SearchResultFragment searchResultFragment;
    private LoveSongFragment loveSongFragment;
    private Handler handler;

    public void setLoveSongFragment(LoveSongFragment loveSongFragment) {
        this.loveSongFragment = loveSongFragment;
    }

    public void setSearchResultFragment(SearchResultFragment searchResultFragment) {
        this.searchResultFragment = searchResultFragment;
    }

    private int FRAGMENTNUMBERS=0;
    private List<Fragment> fragmentList=new ArrayList<>();//管理当前的Fragment

    @Bind(R.id.songname_main)
    TextView songname_main;

    @Bind(R.id.singername_main)
    TextView singername_main;

    @Bind(R.id.albumimage_main)
    ImageView albumimage_main;

    @Bind(R.id.play_pause_main)
    ImageView play_pause;

    @Bind(R.id.statuslayout_main)
    RelativeLayout statusLayout;

    private PlayService.PlayBinder playBinder;
    private DownloadService.DownloadBinder downloadBinder;
    private ServiceConnection playConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            playBinder = (PlayService.PlayBinder)iBinder;
            Log.i("MainActivity","绑定播放服务");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }

        //请求读写权限
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }

        //设置保存歌曲的路径
        setSaveSongPath();

        EventBus.getDefault().register(this);

        mainFragment=new MainFragment();
        addFragment(mainFragment);//默认显示mainFragment
        showFragment(1);

        Intent playIntent = new Intent(MainActivity.this,PlayService.class);
        bindService(playIntent,playConnection, Context.BIND_AUTO_CREATE);

        Intent downloadIntent=new Intent(MainActivity.this, DownloadService.class);
        bindService(downloadIntent,downloadConnection,Context.BIND_AUTO_CREATE);

        List<LoveSong> list=LitePal.findAll(LoveSong.class);
        if(list.size()!=0) {
            //将所有喜爱歌曲的状态修改为非正在播放
            LoveSong ls = new LoveSong();
            ls.setPlaying("no");
            ls.updateAll("isPlaying = ?", "yes");
        }
        statusLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, PlayActivity.class);
                if(Currsong.getSTATUS()!=Constant.NOTPLAYING) {
                    startActivity(intent);
                }
            }
        });

        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(Currsong.getSTATUS()){
                    case Constant.PLAYING:
                        play_pause.setImageResource(R.drawable.play_32);
                        PlayService.mp.pause();
                        Currsong.setSTATUS(Constant.PAUSE);
                        break;
                    case Constant.PAUSE:
                        play_pause.setImageResource(R.drawable.pause_32);
                        PlayService.mp.start();
                        Currsong.setSTATUS(Constant.PLAYING);
                        break;
                }
            }
        });

        handler=new Handler(){
            @Override
            public void handleMessage(Message msg){//当图片加载到本地后，接收消息让UI更新，
                System.out.println("handle message");
                Glide.with(getApplicationContext()).load(msg.obj).into(albumimage_main);
            }
        };

        //跑马灯效果
        songname_main.setSelected(true);
        singername_main.setSelected(true);

    }

    //将fragment添加到fragment回退栈和list中
    //每个beginTransaction只能commit一次
    //这说明每次getSupportFragmentManager.beginTransaction()获得的FragmentTransaction实例都是不一样的
    public void addFragment(Fragment fragment){
        if(!fragment.isAdded()){//判断当前的fragment是否已被加入到当前activity
            FRAGMENTNUMBERS++;
            //将fragment添加到当前activity中，并为该fragment添加一个标记。如果是第x个fragment，标记则为“fragmentx”,以便后续的查找fragment操作，来控制某个fragment的可见性
            //addToBackStack,是将FragmentTransaction加入到回退栈中
            getSupportFragmentManager().beginTransaction().add(R.id.container,fragment,"fragment"+FRAGMENTNUMBERS).addToBackStack(null).commit();
            fragmentList.add(fragment);//将fragment实例保存到一个list中
        }
    }
    //展示第idx个fragment，非第idx个fragment都hide
    public void showFragment(int idx){
        for(int i=0;i<fragmentList.size();i++){
            if(i+1!=idx)
                getSupportFragmentManager().beginTransaction().hide(fragmentList.get(i)).commit();
        }
        FragmentManager fm=getSupportFragmentManager();
        fm.executePendingTransactions();//添加了这句 findFragmentByTag才不会返回null
        Fragment fragment=fm.findFragmentByTag("fragment"+idx);//通过fragment的tag找到要展示的fragment
        getSupportFragmentManager().beginTransaction().show(fragment).commit();//展示
    }

    public void pop(){//将目前显示的fragment弹出，
        FRAGMENTNUMBERS--;
        fragmentList.remove(fragmentList.size()-1);
        getSupportFragmentManager().popBackStack();//把之前addToBackStack的事务弹出，达到弹出之前添加的fragment的效果
        showFragment(FRAGMENTNUMBERS);
    }

    public void setMainFragment(MainFragment fragment){
        mainFragment=fragment;
    }
    public void setSearchFragment(SearchFragment fragment){
        searchFragment=fragment;
    }
    public void setRecentPlayFragment(RecentPlayFragment fragment){recentPlayFragment=fragment;}
    public MainFragment getMainFragment(){
        return mainFragment;
    }
    public SearchFragment getSearchFragment(){
        return searchFragment;
    }
    public RecentPlayFragment getRecentPlayFragment(){return recentPlayFragment;}
    public int getFRAGMENTNUMBERS(){return FRAGMENTNUMBERS;}


    //重写了实体返回键触发的事件
    @Override
    public void onBackPressed(){
        if(getSupportFragmentManager().getBackStackEntryCount()<=1){
            Log.i("MainActivity","onBackPressed,if");
            Log.i("MainActivity",getSupportFragmentManager().getBackStackEntryCount()+"");
            finish();
        }else{
            Log.i("MainActivity","onBackPressed,else");
            pop();
            Log.i("MainActivity",getSupportFragmentManager().getBackStackEntryCount()+"");
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayingStatusEvent(PlayingStatusEvent event){
        refresh();
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.i("MainActivity","onResume");
        refresh();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.i("playService","MainActivity onDestroy");
        unbindService(playConnection);//断开服务
        unbindService(downloadConnection);//断开服务
        EventBus.getDefault().unregister(this);//解除注册
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

    public void refresh(){
        switch(Currsong.getSTATUS()){
            case Constant.PLAYING:
                play_pause.setImageResource(R.drawable.pause_32);
                songname_main.setText(Currsong.getCurrentSong().getSongName());
                singername_main.setText(getSingerNames(Currsong.getCurrentSong()));
                albumimage_main.setImageResource(R.drawable.default_disc);
                loadAlbumImage(Currsong.getCurrentSong());
                break;
            case Constant.PAUSE:
                play_pause.setImageResource(R.drawable.play_32);
                songname_main.setText(Currsong.getCurrentSong().getSongName());
                singername_main.setText(getSingerNames(Currsong.getCurrentSong()));
                albumimage_main.setImageResource(R.drawable.default_disc);
                loadAlbumImage(Currsong.getCurrentSong());
                break;
        }
    }

    //如果本地有图片则直接加载，没有则建立HTTP连接将图片保存到本地后加载。
    public void loadAlbumImage(CurrentSong cs){
        String path=getApplicationContext().getExternalFilesDir("")+"/mvpmymusic/albumimg/";
        File file=new File(path);
        Log.i("MainActivity",path);
        if(!file.exists()){
            Log.i("MainActivity","新建文件夹");
            file.mkdirs();
        }
        File imgFile=new File(file,cs.getAlbummid()+".png");
        if(!imgFile.exists()){//如果改图片在本地找不到，则发送请求保存到本地
            saveAlbumImageToLocal("http://y.gtimg.cn/music/photo_new/T002R180x180M000"+Currsong.getCurrentSong().getAlbummid()+".jpg",path,cs.getAlbummid());
        }
        else
            Glide.with(this).load(imgFile).into(albumimage_main);
    }



    //将专辑封面保存到本地
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
                        handler.sendMessage(msg);
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

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch(requestCode){
            case 1:
                if(grantResults.length>0&&grantResults[0]!=PackageManager.PERMISSION_GRANTED)
                    finish();
                break;
        }
    }

    //设置保存歌曲的路径
    public void setSaveSongPath(){
        String songPath = getApplicationContext().getExternalFilesDir("")+"/mvpmymusic/song/";
        CommonUtils.setSongPath(songPath);
    }

}
