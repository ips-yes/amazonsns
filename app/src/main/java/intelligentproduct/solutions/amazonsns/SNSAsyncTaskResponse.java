package intelligentproduct.solutions.amazonsns;

/**
 * Created by mariettam on 6/13/17.
 */

public class SNSAsyncTaskResponse {
    public final String responseString;
    public final Throwable error;

    public SNSAsyncTaskResponse(String responseString, Throwable error){
        this.responseString  = responseString;
        this.error = error;
    }
}
