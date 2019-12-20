
package vn.uiza.restapi.model.v2.getlinkdownload;

import com.google.gson.annotations.SerializedName;

public class Mpd {

    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("url")
    private String url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}