package hamlah.pin.complice;

import android.support.annotation.NonNull;

import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public interface CompliceAPI {
    @POST("/oauth/token?grant_type=authorization_code")
    Observable<AuthResponse> authorize(@Query("code") String authCode,
                                        @Query("client_id") String clientId,
                                        @Query("client_secret") String clientSecret,
                                        // &username=%1$s&password=%2$s&client_secret=%2$s&
                                        @Query("redirect_uri") String redirectUri);

    @GET("/api/u/newtabpage.json")
    Observable<String> loadCurrentTask(@Header("Authorization") @NonNull String authorization);
}
