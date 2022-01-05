package com.browserstack.runner.reporter.utils;

import com.browserstack.runner.RunCucumberTest;
import com.browserstack.runner.reporter.CustomReporter;
import com.browserstack.runner.CucumberCLIRunner;
import com.browserstack.runner.reporter.model.Feature;
import com.browserstack.runner.reporter.model.Scenario;
import com.browserstack.runner.reporter.model.Platform;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReportUtil {

    List<Scenario> allScenarios = new ArrayList<>();
    List<Scenario> allRerunScenarios = new ArrayList<>();
    List<Platform> allPlatforms = new ArrayList<>();

    public void createInitialFeatureList(File dir, boolean isRerun) throws IOException {

        File lastModFileInDir = getLastModFileInDir(dir);
        Platform platform = new Platform(dir.getName(), Instant.ofEpochMilli(lastModFileInDir.lastModified()));

        FilenameFilter jsonFileFilter = (file, name) -> name.toLowerCase().endsWith(".json");
        String[] filesList = dir.list(jsonFileFilter);

        assert filesList != null;
        List<Feature> allFeatures = new ArrayList<>();
        for (String fileName : filesList) {
            String jsonString = FileUtils.readFileToString(new File(dir.getAbsolutePath() + "/" + fileName), StandardCharsets.UTF_8.toString());
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode featuresObj = objectMapper.readTree(jsonString);
            JsonNode elementsObj = featuresObj.get(0).get("elements");

            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            List<Scenario> currentScenarios = objectMapper.readValue(elementsObj.toString(), new TypeReference<List<Scenario>>() {
            });

            if(currentScenarios != null && currentScenarios.size() > 0) {
                currentScenarios.forEach(scenario -> {

                    scenario.setFeatureId(scenario.getScenarioId().substring(0, scenario.getScenarioId().indexOf(";")));
                    if(scenario.getBefore() != null && scenario.getBefore().size() > 0) {
                        scenario.setPlatformName(scenario.getBefore().get(0).getOutput().get(0).substring(ReporterConstants.LOG_PLATFORM_FOR_REPORT.length()-2).trim());
                    }

                    scenario.setRerun(isRerun);

                    Scenario checkDuplicate = allRerunScenarios
                            .stream()
                            .filter(scenario1 -> scenario.getScenarioId().equals(scenario1.getScenarioId())
                                    && scenario.getPlatformName().equals(scenario1.getPlatformName())
                                    && scenario.getFeatureId().equals(scenario1.getFeatureId()))
                            .findAny()
                            .orElse(null);

                    if(checkDuplicate == null) {
                        allScenarios.add(scenario);
                    }

                });
            }

            List<Feature> currentFeatures = objectMapper.readValue(jsonString, new TypeReference<List<Feature>>() {
            });

            if(currentFeatures != null && currentFeatures.size() > 0) {
                currentFeatures.forEach(feature -> feature.setPlatformName(dir.getName()));
                allFeatures.addAll(currentFeatures);
            }
        }

        platform.setFeatureList(allFeatures);
        allPlatforms.add(platform);
    }

    public void create(CucumberCLIRunner customParallelRunner) {

        prepareRelevantListsWrapper(ReporterConstants.CUCUMBER_RERUN_JSON_REPORTS_DIR, true);
        allRerunScenarios = new ArrayList<>(allScenarios);

        prepareRelevantListsWrapper(ReporterConstants.CUCUMBER_JSON_REPORTS_DIR, false);

        allPlatforms.forEach(platform -> {
            String platformName = platform.getPlatformName();
            List<Feature> listFeaturesByPlatform = platform.getFeatureList().stream().filter(feature ->
                    feature.getPlatformName().equalsIgnoreCase(platformName))
                    .collect(Collectors.toList());
            rearrangeScenariosByPlatform(platformName, listFeaturesByPlatform);
            generateReportFiles(platformName, listFeaturesByPlatform, platform.getPlatformCompleteTime(), customParallelRunner);

        });

    }


    private void prepareRelevantListsWrapper(String cucumberRerunJsonReportsDir, boolean isRerun) {
        File basePath = new File(cucumberRerunJsonReportsDir);
        FileFilter directoryFilter = File::isDirectory;

        File[] directoriesArr = basePath.listFiles(directoryFilter);

        if (directoriesArr != null) {

            for (File dir : directoriesArr) {
                try {
                    createInitialFeatureList(dir, isRerun);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    private void rearrangeScenariosByPlatform(String platformName, List<Feature> listFeaturesByPlatform) {

        if(listFeaturesByPlatform!= null && listFeaturesByPlatform.size() > 0) {
            listFeaturesByPlatform.forEach(feature -> {
            //    feature.setName(feature.getName() + " - " + platformName);
                List<Scenario> filteredScenarios = new ArrayList<>();
                if (allScenarios != null && allScenarios.size() > 0) {
                    allScenarios.forEach(scenario -> {
                        if(scenario != null) {
                            if (scenario.getFeatureId().equalsIgnoreCase(feature.getFeatureId()) && platformName.equalsIgnoreCase(scenario.getPlatformName())) {
                               // scenario.setName(scenario.getName() + " - " + platformName);
                                filteredScenarios.add(scenario);
                            }
                        }
                    });
                }
                feature.setScenarios(filteredScenarios);
            });
        }
    }

    private void generateReportFiles(String platformName, List<Feature> listFeaturesByPlatform, Instant platformCompleteTime, CucumberCLIRunner customParallelRunner) {
        try {
            new CustomReporter(platformName, listFeaturesByPlatform, platformCompleteTime).create(customParallelRunner);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getLastModFileInDir(File dir) throws IOException {
        return Files.walk(Paths.get(dir.getAbsolutePath()))
                .sorted((f1, f2) -> -(int)(f1.toFile().lastModified() - f2.toFile().lastModified()))
                .skip(1)
                .findFirst().get().toFile();
    }

    public static void main(String[] args) {
        System.setProperty("capabilities.config", "conf/capabilities-parallel-browsers.yml");
        new ReportUtil().create(new CucumberCLIRunner(RunCucumberTest.class));
    }
}



