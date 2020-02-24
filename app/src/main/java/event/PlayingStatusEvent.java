package event;

import java.util.List;

import bean.LoveSong;
import bean.OnlineSong;
import bean.RecentSong;
import bean.SaveSong;
import view.fragment.LoveSongFragment;

public class PlayingStatusEvent {
    private OnlineSong os;
    private RecentSong rs;
    private LoveSong ls;

    public RecentSong getRecentSong() {
        return rs;
    }
    public void setRecentSong(RecentSong rs) {
        this.rs = rs;
    }
    public OnlineSong getOnlineSong() {
        return os;
    }

    public PlayingStatusEvent(OnlineSong os) {
        this.os = os;
    }
    public PlayingStatusEvent(RecentSong rs){
        this.rs=rs;
    }
    public PlayingStatusEvent(LoveSong ls){this.ls=ls;}
    public PlayingStatusEvent(){os=null; rs=null;}

}