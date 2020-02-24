package service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.io.File;

import Util.CommonUtils;
import bean.CurrentSong;
import task.DownloadListener;
import task.DownloadTask;

public class DownloadService extends Service {
    private DownloadBinder downloadBinder=new DownloadBinder();
    private DownloadTask downloadTask;
    private CurrentSong downloadSong;

    private DownloadListener listener=new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            Toast.makeText(getApplicationContext(),"已下载"+progress+"%",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSuccess() {
            downloadTask=null;
            Toast.makeText(getApplicationContext(),"下载完成",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downloadTask=null;
            Toast.makeText(getApplicationContext(),"下载失败",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            downloadTask=null;
            Toast.makeText(getApplicationContext(),"暂停下载",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            downloadTask=null;
            Toast.makeText(getApplicationContext(),"取消下载",Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onCreate(){//第一次创建服务时调用
        super.onCreate();
        System.out.println("DownloadService onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){//每次服务启动时调用
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onDestroy(){//所有绑定该服务的都unbind了的时候调用
        super.onDestroy();
        System.out.println("DownloadService onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent){
        return downloadBinder;
    }

    public class DownloadBinder extends Binder {
        public void startDownload(CurrentSong downloadInformation){
            if(downloadTask==null){
                downloadSong=downloadInformation;
                downloadTask=new DownloadTask(listener,getApplicationContext());
                downloadTask.execute(downloadInformation);//执行doInBackground方法
            }
        }

        public void pauseDownload(){
            if(downloadTask!=null){
                downloadTask.pauseDownload();
            }
        }

        public void cancelDownload(){
            if(downloadTask!=null){
                downloadTask.cancelDownload();
            }
            if(downloadSong!=null){//取消下载，需要把下载到一半的文件删除
                String path= CommonUtils.songPath;
                File file=new File(path+downloadSong.getSongName()+downloadSong.getSongmId()+".m4a");
                if(file.exists()){
                    file.delete();
                }
            }
        }

    }
}
