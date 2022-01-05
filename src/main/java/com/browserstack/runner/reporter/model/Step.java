package com.browserstack.runner.reporter.model;

import java.util.ArrayList;
import java.util.List;

import static io.cucumber.core.backend.Status.*;

public class Step {
    private String name;
    private String keyword;
    private Result result;
    private List<Hook> before = new ArrayList<>();
    private List<Hook> after = new ArrayList<>();
    private List<Embedding> embeddings = new ArrayList<>();
    private List<String> output = new ArrayList<>();
    private List<String> rowData = new ArrayList<>();

    public Step () {

    }

    public String getName() {
        return name;
    }

    public String getKeyword() {
        return keyword;
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

    public List<String> getRowData() {
        return rowData;
    }

    public boolean passed() {
        return result.getStatus().equalsIgnoreCase(PASSED.toString()) || result.getStatus().equalsIgnoreCase(SKIPPED.toString());
    }
}