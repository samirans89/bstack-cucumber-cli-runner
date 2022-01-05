package com.browserstack.runner.reporter.model;

public class Tag {
    private String name;

    public Tag() {

    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "name='" + name + '\'' +
                '}';
    }
}
