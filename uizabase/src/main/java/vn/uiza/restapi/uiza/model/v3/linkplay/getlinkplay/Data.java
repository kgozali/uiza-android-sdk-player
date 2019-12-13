
package vn.uiza.restapi.uiza.model.v3.linkplay.getlinkplay;

import com.squareup.moshi.Json;

import java.util.List;

public class Data {

    @Json(name = "cdn")
    private List<Cdn> cdn = null;
    @Json(name = "cdn_fallback")
    private List<Object> cdnFallback = null;
    @Json(name = "appId")
    private String appId;
    @Json(name = "entityId")
    private String entityId;
    @Json(name = "type")
    private String type;
    @Json(name = "fileName")
    private List<FileName> fileName = null;
    @Json(name = "urls")
    private List<Url> urls = null;
    @Json(name = "allRecord")
    private List<AllRecord> allRecord = null;

    public List<Cdn> getCdn() {
        return cdn;
    }

    public void setCdn(List<Cdn> cdn) {
        this.cdn = cdn;
    }

    public List<Object> getCdnFallback() {
        return cdnFallback;
    }

    public void setCdnFallback(List<Object> cdnFallback) {
        this.cdnFallback = cdnFallback;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<FileName> getFileName() {
        return fileName;
    }

    public void setFileName(List<FileName> fileName) {
        this.fileName = fileName;
    }

    public List<Url> getUrls() {
        return urls;
    }

    public void setUrls(List<Url> urls) {
        this.urls = urls;
    }

    public List<AllRecord> getAllRecord() {
        return allRecord;
    }

    public void setAllRecord(List<AllRecord> allRecord) {
        this.allRecord = allRecord;
    }

}
