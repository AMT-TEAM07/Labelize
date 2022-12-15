package ch.heig.amt07.dataobjectservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Optional;

public class DataObjectResponse implements Serializable {

    @JsonProperty("message")
    private String message;

    @JsonProperty("object_name")
    private String objectName;

    @JsonProperty("signed_url")
    private Optional<String> signedUrl;

    @JsonProperty("url_expiration_seconds")
    private Optional<Long> expiration;

    public DataObjectResponse() {
        this("", "", Optional.empty(), Optional.empty());
    }

    public DataObjectResponse(String message, String objectName, Optional<String> signedUrl, Optional<Long> expiration) {
        this.message = message;
        this.objectName = objectName;
        this.signedUrl = signedUrl;
        this.expiration = expiration;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public void setSignedUrl(Optional<String> signedUrl) {
        this.signedUrl = signedUrl;
    }

    public void setExpiration(Optional<Long> expiration) {
        this.expiration = expiration;
    }
}
