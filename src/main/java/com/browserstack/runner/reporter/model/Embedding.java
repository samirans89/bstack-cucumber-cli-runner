package com.browserstack.runner.reporter.model;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Embedding {
    @JsonProperty("name")
    private String embeddingId;
    private String data;
    @JsonProperty("mime_type")
    private String mimeType;

    public Embedding() {
    }

    public String getEmbeddingId() {
        return embeddingId;
    }

    public String getData() {
        return data;
    }

    public String getMimeType() {
        return mimeType;
    }
}
