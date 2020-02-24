package bean;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;
import java.util.List;

public class SaveSong extends LitePalSupport implements Serializable {
    private List<String> singers;
    private String songName;
    private String SONGURL1="%7B%22req_0%22%3A%7B%22module%22%3A%22vkey.GetVkeyServer%22%2C%22method%22%3A%22CgiGetVkey%22%2C%22param%22%3A%7B%22guid%22%3A%22358840384%22%2C%22songmid%22%3A%5B%22";
    private String SONGURL2="%22%5D%2C%22songtype%22%3A%5B0%5D%2C%22uin%22%3A%221443481947%22%2C%22loginflag%22%3A1%2C%22platform%22%3A%2220%22%7D%7D%2C%22comm%22%3A%7B%22uin%22%3A%2218585073516%22%2C%22format%22%3A%22json%22%2C%22ct%22%3A24%2C%22cv%22%3A0%7D%7D";
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

    public String getSONGURL1() {
        return SONGURL1;
    }

    public void setSONGURL1(String SONGURL1) {
        this.SONGURL1 = SONGURL1;
    }

    public String getSONGURL2() {
        return SONGURL2;
    }

    public void setSONGURL2(String SONGURL2) {
        this.SONGURL2 = SONGURL2;
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
