package bean;

import org.litepal.crud.LitePalSupport;

import java.util.List;

public class OnlineSong extends LitePalSupport {
    private List<String> singers;
    private String songName;
    private String songmId;
    private String albumName;
    private String url;
    private String albummid;
    private int position;
    private int interval;//歌曲时长

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    private boolean needPay;

    public boolean isNeedPay() {
        return needPay;
    }

    public void setNeedPay(boolean needPay) {
        this.needPay = needPay;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getAlbummid() {
        return albummid;
    }

    public void setAlbummid(String albummid) {
        this.albummid = albummid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    private long duration;//歌曲全长
    private long onlineCurrentPosition;//当前播放位置

    public List<String> getSingers() {
        return singers;
    }

    public void setSingers(List<String> singers) {
        this.singers = singers;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongmId() {
        return songmId;
    }

    public void setSongmId(String songmId) {
        this.songmId = songmId;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getonlineCurrentPosition() {
        return onlineCurrentPosition;
    }

    public void setonlineCurrentPosition(long onlineCurrentPosition) {
        this.onlineCurrentPosition = onlineCurrentPosition;
    }
}
