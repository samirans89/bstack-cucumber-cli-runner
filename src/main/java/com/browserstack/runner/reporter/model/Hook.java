package com.browserstack.runner.reporter.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.cucumber.core.backend.Status.PASSED;
import static io.cucumber.core.backend.Status.SKIPPED;

public class Hook {


    private String location;
    private Result result;
    private List<Embedding> embeddings = new ArrayList<>();
    private List<String> output = new ArrayList<>();

    public Hook() {

    }

    @JsonProperty("match")
    private void unpackNameFromNestedObject(Map<String, String> match) {
        location = match.get("location");
    }

    public String getLocation() {
        return location;
    }

    public Result getResult() {
        return result;
    }

    public List<Embedding> getEmbeddings() {
        return embeddings;
    }

    public List<String> getOutput() {
        return output;
    }

    public boolean passed() {
        return result.getStatus().equalsIgnoreCase(PASSED.toString()) || result.getStatus().equalsIgnoreCase(SKIPPED.toString());
    }

    @Override
    public String toString() {
        return "Hook{" +
                "location='" + location + '\'' +
                ", result=" + result +
                ", embeddings=" + embeddings +
                ", output=" + output +
                '}';
    }
}