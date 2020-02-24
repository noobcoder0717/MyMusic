package bean;

import java.util.List;

public class DownloadInformation {
    private List<String> singers;
    private String songName;
    private String songmId;
    private String albumName;
    private String url;
    private String albummid;
    private boolean needPay;
    private int position;
    private int interval;
    private String isPlaying;//这里不能默认设值，否则可能会出现会更新不了数据库该字段的值的情况
    private long duration;

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

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAlbummid() {
        return albummid;
    }

    public void setAlbummid(String albummid) {
        this.albummid = albummid;
    }

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

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getIsPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(String isPlaying) {
        this.isPlaying = isPlaying;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
