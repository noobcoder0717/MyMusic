package presenter;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import bean.SongUrlSorted;
import event.LoadFinishedEvent;
import model.ISearchResult;
import model.SearchResult;
import view.ISearchResultView;
import view.fragment.SearchResultFragment;

public class SearchResultPresenter implements ISearchResultPresenter {
    public SearchResult searchResult;
    public ISearchResultView searchResultView;

    public SearchResultPresenter(ISearchResultView searchResultView){

        this.searchResultView=searchResultView;
        searchResult = new SearchResult(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void getResult(String info){
        Log.i("presenter","getResult");
        searchResult.getResult(info);
    }

    @Override
    public List<String> loadSingers(){
         return searchResult.getSingers();
    }
    @Override
    public List<String> loadSongnameList(){
        return searchResult.getSongnameList();
    }
    @Override
    public List<List<String>> loadSingerList(){
        return searchResult.getSingerList();
    }
    @Override
    public List<String> loadSongmidList(){
        return searchResult.getSongmidList();
    }
    @Override
    public List<String> loadAlbumList(){
        return searchResult.getAlbumList();
    }
    @Override
    public List<SongUrlSorted> loadSongUrlList(){return searchResult.getSongUrlList();}
    @Override
    public List<Integer> loadSongDuration(){return searchResult.getSongDuration();}
    @Override
    public List<String> loadAlbummidList(){return searchResult.getAlbummidList();}
    @Override
    public List<Integer> loadIntervalList(){return searchResult.getIntervalList();}

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LoadFinishedEvent event){//当searchResult数据加载完毕后，通知view刷新
        if(event.getMessage().equals("finished")){
            searchResultView.showResult();
            searchResultView.hideDialog();
        }else{
            searchResultView.showFailure();
        }
    }
}
