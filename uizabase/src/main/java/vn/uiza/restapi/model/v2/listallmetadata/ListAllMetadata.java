
package vn.uiza.restapi.model.v2.listallmetadata;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.uiza.restapi.model.v2.listallentity.Metadata;

public class ListAllMetadata {

    @SerializedName("data")
    private List<Datum> data = null;
    @SerializedName("metadata")
    private Metadata metadata;
    @SerializedName("version")
    private int version;
    @SerializedName("datetime")
    private String datetime;
    @SerializedName("name")
    private String name;
    @SerializedName("message")
    private String message;
    @SerializedName("code")
    private int code;
    @SerializedName("type")
    private String type;

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}