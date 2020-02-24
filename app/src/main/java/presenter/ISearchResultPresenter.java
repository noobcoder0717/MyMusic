package presenter;

import java.util.List;

import bean.SongUrlSorted;

public interface ISearchResultPresenter {
    void getResult(String info);
    List<String> loadSingers();
    List<String> loadSongnameList();
    List<List<String>> loadSingerList();
    List<String> loadSongmidList();
    List<String> loadAlbumList();
    List<SongUrlSorted> loadSongUrlList();
    List<Integer> loadSongDuration();
    List<String> loadAlbummidList();
    List<Integer> loadIntervalList();
}
