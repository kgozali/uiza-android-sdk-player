
package vn.uiza.restapi.model.v3.livestreaming.retrievealiveevent;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.uiza.restapi.model.v3.metadata.getdetailofmetadata.Data;

public class ResultRetrieveALiveEvent {

    @SerializedName("data")
    private List<Data> data = null;
    @SerializedName("metadata")
    private Metadata metadata;
    @SerializedName("version")
    private long version;
    @SerializedName("datetime")
    private String datetime;
    @SerializedName("policy")
    private String policy;
    @SerializedName("requestId")
    private String requestId;
    @SerializedName("serviceName")
    private String serviceName;
    @SerializedName("message")
    private String message;
    @SerializedName("code")
    private long code;
    @SerializedName("type")
    private String type;

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}