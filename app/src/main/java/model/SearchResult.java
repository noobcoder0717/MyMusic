package model;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;


import Util.RetrofitFactory;
import bean.ClientApi;
import bean.Information;
import bean.SongUrl;
import bean.SongUrlSorted;
import event.LoadFinishedEvent;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import presenter.ISearchResultPresenter;

public class SearchResult implements ISearchResult {

    ISearchResultPresenter searchResultPresenter;

    //传给singerAdapter
    List<String> singers=new ArrayList<>();

    //传给songAdapter
    List<String> songnameList=new ArrayList<>();
    List<List<String>> singerList=new ArrayList<>();
    List<String> albumList=new ArrayList<>();
    List<String> songmidList=new ArrayList<>();
    List<SongUrlSorted> songUrlList=new ArrayList<>();
    List<Integer> songDuration=new ArrayList<>();
    List<String> albummidList=new ArrayList<>();
    List<Integer> intervalList=new ArrayList<>();


    final String SONGURL1="%7B%22req_0%22%3A%7B%22module%22%3A%22vkey.GetVkeyServer%22%2C%22method%22%3A%22CgiGetVkey%22%2C%22param%22%3A%7B%22guid%22%3A%22358840384%22%2C%22songmid%22%3A%5B%22";
    final String SONGURL2="%22%5D%2C%22songtype%22%3A%5B0%5D%2C%22uin%22%3A%221443481947%22%2C%22loginflag%22%3A1%2C%22platform%22%3A%2220%22%7D%7D%2C%22comm%22%3A%7B%22uin%22%3A%2218585073516%22%2C%22format%22%3A%22json%22%2C%22ct%22%3A24%2C%22cv%22%3A0%7D%7D";

    final ClientApi singerSongApi= RetrofitFactory.getRetrofitOfSinger().create(ClientApi.class);
    final ClientApi songUrlApi=RetrofitFactory.getRetrofitOfSongUrl().create(ClientApi.class);

    int cnt=0;


    public SearchResult(ISearchResultPresenter searchResultPresenter){
        this.searchResultPresenter=searchResultPresenter;
    }

    public void getResult(final String info){
        Log.i("model","getResult");
        singerSongApi.getInformation(10,info,"json")//Observable<Information>,默认只获取10首歌
                .flatMap(new Function<Information, ObservableSource<SongUrl>>() {
                    @Override
                    public ObservableSource<SongUrl> apply(Information information) throws Exception {
                        for(int i=0;i<information.getData().getSong().getList().get(0).getSinger().size();i++){
                            singers.add(information.getData().getSong().getList().get(0).getSinger().get(i).getName());//获得了搜索得到的相关歌手的list
                        }
                        for(int i=0;i<information.getData().getSong().getList().size();i++){
                            List<String> tmp=new ArrayList<>();
                            songnameList.add(information.getData().getSong().getList().get(i).getSongname());
                            for(int j=0;j<information.getData().getSong().getList().get(i).getSinger().size();j++)
                                tmp.add(information.getData().getSong().getList().get(i).getSinger().get(j).getName());
                            singerList.add(tmp);
                            albumList.add(information.getData().getSong().getList().get(i).getAlbumname());
                            songmidList.add(information.getData().getSong().getList().get(i).getSongmid());
                            songDuration.add(information.getData().getSong().getList().get(i).getInterval());
                            albummidList.add(information.getData().getSong().getList().get(i).getAlbummid());
                            intervalList.add(information.getData().getSong().getList().get(i).getInterval());
                        }
                        return songUrlApi.getSongUrl("json",SONGURL1+information.getData().getSong().getList().get(0).getSongmid()+SONGURL2);//变换对象，由它来调用下一个网络请求
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<SongUrl>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(SongUrl songUrl) {
                        handleMultipleSongUrl(0,songnameList.size());
                    }

                    @Override
                    public void onError(Throwable e) {
                        EventBus.getDefault().post(new LoadFinishedEvent("failed"));
                    }

                    @Override
                    public void onComplete() {
                    }
                });

    }


    public void handleMultipleSongUrl(int i, final int length) {
        if (i == length) {
            return;
        } else {
            final int j=i;
            final int finallength=length;
            songUrlApi.getSongUrl("json", SONGURL1 + songmidList.get(i) + SONGURL2)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<SongUrl>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                        }

                        @Override
                        public void onNext(SongUrl songUrl) {
                            SongUrlSorted s=new SongUrlSorted();
                            Log.i("SearchResult",songUrl.getCode()+"");
                            s.setIdx(j);
                            s.setUrl(songUrl.getReq_0().getData().getSip().get(0) + songUrl.getReq_0().getData().getMidurlinfo().get(0).getPurl());
                            if(songUrl.getReq_0().getData().getMidurlinfo().get(0).getPurl().equals("")){
                                s.setNeedPay(true);
                            }else{
                                s.setNeedPay(false);
                            }
                            songUrlList.add(s);
                        }

                        @Override
                        public void onError(Throwable e) {
                            EventBus.getDefault().post(new LoadFinishedEvent("failed"));
                        }

                        @Override
                        public void onComplete() {
                            IncreaseCnt();
                            if(cnt==length){
                                EventBus.getDefault().post(new LoadFinishedEvent("finished"));
                            }
                        }
                    });
            handleMultipleSongUrl(++i,length);
        }
    }

    public List<String> getSingers(){
        return singers;
    }

    public List<String> getSongnameList(){
        return songnameList;
    }
    public List<List<String>> getSingerList(){
        return singerList;
    }
    public List<String> getAlbumList(){
        return albumList;
    }
    public List<String> getSongmidList(){
        return songmidList;
    }
    public List<SongUrlSorted> getSongUrlList(){
        return songUrlList;
    }
    public List<Integer> getSongDuration(){return songDuration;}
    public List<String> getAlbummidList(){return albummidList;}
    public List<Integer> getIntervalList(){return intervalList;}

    public void IncreaseCnt(){
        cnt++;
    }


}
