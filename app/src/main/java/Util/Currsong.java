package Util;

import bean.CurrentSong;
import bean.LoveSong;
import bean.OnlineSong;
import bean.RecentSong;
import bean.SaveSong;

public class Currsong {

    public static int getSTATUS() {
        return STATUS;
    }

    public static void setSTATUS(int STATUS) {
        Currsong.STATUS = STATUS;
    }

    private static CurrentSong currentSong;
    private static int STATUS=Constant.NOTPLAYING;
    public static CurrentSong getCurrentSong() {
        return currentSong;
    }

    public static void setCurrentSong(CurrentSong currentSong) {
        Currsong.currentSong = currentSong;
    }


}
