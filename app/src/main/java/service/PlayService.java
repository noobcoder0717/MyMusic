package service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.List;

import Util.CommonUtils;
import Util.Constant;
import Util.Currsong;
import bean.CurrentSong;
import bean.LoveSong;
import bean.RecentSong;
import event.PlayingStatusEvent;

public class PlayService extends Service {
    private int currentPosition;
    public static MediaPlayer mp = new MediaPlayer();
    private PlayBinder playBinder=new PlayBinder();
    public PlayService() {}

    @Override
    public void onCreate(){//第一次创建服务时调用
        super.onCreate();
        System.out.println("playService onCreate");
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){//每次服务启动时调用
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onDestroy(){//所有绑定该服务的都unbind了的时候调用
        super.onDestroy();
        System.out.println("playService onDestroy");
        mp.stop();
        mp.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                saveRecentSong(Currsong.getCurrentSong());
                playNextSong();
            }
        });

        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                if(Currsong.getSTATUS()==Constant.PREPARE){
                    Currsong.setSTATUS(Constant.PLAYING);
                    EventBus.getDefault().post(new PlayingStatusEvent());
                }
                mp.start();
            }
        });
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                Log.i("PlayService","onError:what "+what+" extra "+extra);
                return true;
            }
        });
        return playBinder;
    }

    public class PlayBinder extends Binder {
        public MediaPlayer getMediaPlayer() {
            Log.i("PlayActivity", mp.getDuration() + "");
            return mp;
        }

        //播放歌曲
        public void play(CurrentSong cs) {
            LoveSong ls = new LoveSong();
            ls.setPlaying("no");
            ls.updateAll("isPlaying = ?", "yes");
            if (!cs.isNeedPay()) {
                try {
                    Currsong.setCurrentSong(cs);
                    Currsong.setSTATUS(Constant.PREPARE);
                    String path= CommonUtils.songPath +cs.getSongName()+cs.getSongmId()+".m4a";
                    File file=new File(path);
                    currentPosition = cs.getPosition();
                    if(file.exists()){
                        mp.reset();
                        mp.setDataSource(path);
                        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mp.prepare();
                    }
                    else{
                        mp.reset();//reset不能在release之后调用。
                        mp.setDataSource(cs.getUrl());
                        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mp.prepareAsync();
                    }
                    Log.i("PlayService", cs.getUrl());
                } catch (IOException e) {
                    Log.i("PlayService", e.getMessage());
                }
            } else {
                Toast.makeText(getApplicationContext(), "暂未获得该歌曲版权，换首歌吧！", Toast.LENGTH_SHORT).show();
                currentPosition++;
                playNextSong();
            }
        }
    }

//        //播放最近播放歌曲
//        public void play(RecentSong recentSong){
//            LoveSong ls=new LoveSong();
//            ls.setPlaying("no");
//            ls.updateAll("isPlaying = ?","yes");
//            try{
//                Log.i("PlayActivity",mp.toString());
//                Currsong.setCurrRecentSong(recentSong);
//                Currsong.setSTATUS(Constant.STATUS_PREPARERECENTSONG);
//                recentCurrentPosition=recentSong.getPosition();
//                mp.reset();//reset不能在release之后调用。
//                mp.setDataSource(recentSong.getUrl());
//                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                mp.prepareAsync();
//                Log.i("PlayService",recentCurrentPosition+"/"+LitePal.findAll(RecentSong.class).size()+"");
//            }catch (IOException e){
//                Log.i("PlayService",e.getMessage());
//            }
//        }
//
//        //播放收藏歌曲
//        public void play(LoveSong loveSong){
//            try{
//                Log.i("PlayActivity",mp.toString());
//
//                //修改歌曲状态
//                LoveSong ls=new LoveSong();
//                ls.setPlaying("yes");
//                ls.updateAll("url = ?",loveSong.getUrl());
//
//                Currsong.setCurrLoveSong(loveSong);
//                Currsong.setSTATUS(Constant.STATUS_PREPARELOVESONG);
//                loveCurrentPosition=loveSong.getPosition();
//                mp.reset();//reset不能在release之后调用。
//                mp.setDataSource(loveSong.getUrl());
//                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                mp.prepareAsync();
//                Log.i("PlayService",loveSong.getPosition()+"");
//            }catch (IOException e){
//                Log.i("PlayService",e.getMessage());
//            }
//        }
//    }

    public void playNextSong() {
        switch(Currsong.getSTATUS()){
            case Constant.PLAYING:
                List<CurrentSong> currentSongList = LitePal.findAll(CurrentSong.class);
                CurrentSong cs;
                if (currentPosition == currentSongList.size() - 1) {
                    cs = currentSongList.get(0);
                    playBinder.play(cs);
                } else {
                    cs = currentSongList.get(currentPosition + 1);
                    playBinder.play(cs);
                }
                break;

        }
    }

    public void saveRecentSong(CurrentSong cs){
        Log.i("PlayService","saveRecentSong");
        List<RecentSong> songList=LitePal.findAll(RecentSong.class);
        RecentSong recentSong=new RecentSong();
        recentSong.setAlbummid(cs.getAlbummid());
        recentSong.setAlbumName(cs.getAlbumName());
        recentSong.setNeedPay(cs.isNeedPay());
        recentSong.setSingers(cs.getSingers());
        recentSong.setSongName(cs.getSongName());
        recentSong.setUrl(cs.getUrl());
        recentSong.setSongmId(cs.getSongmId());
        recentSong.setInterval(cs.getInterval());
        recentSong.setDuration(cs.getDuration());
        if(!AlreadyExist(recentSong)){
            Log.i("PlayService","song don't exist");
            recentSong.setPosition(songList.size());
            recentSong.save();
        }
    }
//    public void saveRecentSong(LoveSong ls){
//        Log.i("PlayService","saveRecentSong");
//        List<RecentSong> songList=LitePal.findAll(RecentSong.class);
//        RecentSong recentSong=new RecentSong();
//        recentSong.setAlbummid(ls.getAlbummid());
//        recentSong.setAlbumName(ls.getAlbumName());
//        recentSong.setNeedPay(ls.isNeedPay());
//        recentSong.setSingers(ls.getSingers());
//        recentSong.setSongName(ls.getSongName());
//        recentSong.setUrl(ls.getUrl());
//        recentSong.setSongmId(ls.getSongmId());
//        recentSong.setInterval(ls.getInterval());
//        if(!AlreadyExist(recentSong)){
//            Log.i("PlayService","song don't exist");
//            recentSong.setPosition(songList.size());
//            recentSong.save();
//        }
//        Log.i("PlayService",LitePal.findAll(RecentSong.class).size()+"");
////        else{//如果这首歌已存在于最近播放列表中，则将其移至最近播放列表的最顶端
////
////        }
//    }

    public Boolean AlreadyExist(RecentSong recentSong){
        List<RecentSong> recentSongList=LitePal.findAll(RecentSong.class);
        if(recentSongList.size()==0)
            return false;
        else{
            for(RecentSong rs:recentSongList) {
                if (recentSong.getUrl().equals(rs.getUrl()))//用url来判断两首歌是否相同
                    return true;
            }
            return false;
        }
    }



}
