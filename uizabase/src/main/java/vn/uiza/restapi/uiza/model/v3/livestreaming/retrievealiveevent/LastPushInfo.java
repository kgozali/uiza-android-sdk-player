
package vn.uiza.restapi.uiza.model.v3.livestreaming.retrievealiveevent;

import com.google.gson.annotations.SerializedName;

public class LastPushInfo {

    @SerializedName("streamUrl")
    private String streamUrl;
    @SerializedName("streamKey")
    private String streamKey;

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public String getStreamKey() {
        return streamKey;
    }

    public void setStreamKey(String streamKey) {
        this.streamKey = streamKey;
    }

}
