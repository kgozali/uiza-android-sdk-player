package testlibuiza.sample.guidecallapi;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface Service {
    //https://docs.uiza.io/#retrieve-an-user
    @GET("/api/public/v3/admin/user")
    Observable<Object> retrieveAnUser(@Query("id") String id);

    //https://docs.uiza.io/#list-all-users
    @GET("/api/public/v3/admin/user")
    Observable<Object> listAllUser();
}
