package task;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import Util.CommonUtils;
import bean.CurrentSong;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadTask extends AsyncTask<CurrentSong,Integer,Integer>{
    public static final int SUCCESS=0;
    public static final int FAILED=1;
    public static final int PAUSED=2;
    public static final int CANCELED=3;

    private boolean isCanceled=false;
    private boolean isPaused=false;
    private int lastProgress;

    private DownloadListener listener;
    private Context context;

    public DownloadTask(DownloadListener listener, Context context){
        this.listener=listener;
        this.context=context;
    }

    @Override
    protected Integer doInBackground(CurrentSong... downloadInformation){
        String path= CommonUtils.songPath;

        CurrentSong info=downloadInformation[0];//接收传入的参数，即要下载的歌

        InputStream is=null;
        RandomAccessFile savedFile=null;
        File directoryFile=new File(path);//存放歌曲的目录
        File songFile=null;//歌曲文件
        if(!directoryFile.exists()){//文件夹不存在则创建
            directoryFile.mkdirs();
        }
        try{
            long downloadedLength=0;//记录已下载的文件长度
            String downloadUrl=info.getUrl();
            songFile=new File(path+info.getSongName()+info.getSongmId()+".m4a");
            if(songFile.exists()){
                downloadedLength=songFile.length();
            }
            long contentLength=getContentLength(downloadUrl);//发送http请求，计算要下载的文件长度
            if(contentLength==0){//长度为0则返回failed
                return FAILED;//后台任务执行完成，调用onPostExecute
            }else if(contentLength==downloadedLength){//要下载的文件总长度等于已下载的长度，说明下载完成了
                return SUCCESS;//后台任务执行完成，调用onPostExecute
            }

            OkHttpClient client=new OkHttpClient();
            Request request=new Request.Builder()
                    .addHeader("RANGE","bytes="+downloadedLength+"-")//断点下载，指定从哪个字节开始下载
                    .url(downloadUrl)
                    .build();
            Response response=client.newCall(request).execute();//下载
            if(response!=null){
                is=response.body().byteStream();
                savedFile=new RandomAccessFile(songFile,"rw");
                savedFile.seek(downloadedLength);
                byte[] b=new byte[1024];
                int total=0;
                int len;
                while((len=is.read(b))!=-1) {//一次读取1024个字节直到结束
                    if (isCanceled)
                        return CANCELED;//后台任务执行完成，调用onPostExecute
                    else if (isPaused)
                        return PAUSED;//后台任务执行完成，调用onPostExecute
                    else {
                        total += len;
                        savedFile.write(b,0,len);//向savedFile中写入b数组从下标0开始的len个元素
                        //计算已下载的百分比
                        int progress=(int)((total+downloadedLength)*100/contentLength);
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return SUCCESS;//后台任务执行完成，调用onPostExecute
            }

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                if(is!=null)
                    is.close();
                if(savedFile!=null)
                    savedFile.close();
                if(isCanceled&&songFile!=null)
                    songFile.delete();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return FAILED;//后台任务执行完成，调用onPostExecute
    }

    @Override
    protected void onProgressUpdate(Integer... values){
        int progress=values[0];
        if(progress>lastProgress){
            listener.onProgress(progress);
            lastProgress=progress;
        }
    }

    @Override
    protected void onPostExecute(Integer status){
        switch(status){
            case SUCCESS:
                listener.onSuccess();
                break;
            case FAILED:
                listener.onFailed();
                break;
            case PAUSED:
                listener.onPaused();
                break;
            case CANCELED:
                listener.onCanceled();
                break;
                default:
                    break;
        }
    }

    public void pauseDownload(){
        isPaused=true;
    }

    public void cancelDownload(){
        isCanceled=true;
    }

    private long getContentLength(String url) throws IOException {
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        if(response!=null&&response.isSuccessful()){
            long contentLength=response.body().contentLength();
            response.body().close();
            return contentLength;
        }
        return 0;
    }

}
