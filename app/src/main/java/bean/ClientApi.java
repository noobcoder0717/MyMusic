package bean;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ClientApi {
    @GET("client_search_cp?")
    Observable<Information> getInformation(@Query("n") int requestNum, @Query("w") String songname, @Query("format") String json);

//    @GET("api/search/get/web?")
//    @Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36")
//    Observable<SingerImage> getSingerImage(@Query("s") String singerName,@Query("type") int type);

    @GET("musicu.fcg?")
    Observable<SongUrl> getSongUrl(@Query("format") String json,@Query(value = "data",encoded = true) String data);


}
