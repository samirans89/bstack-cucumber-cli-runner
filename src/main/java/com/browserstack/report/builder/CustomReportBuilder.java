package com.browserstack.report.builder;

import com.browserstack.report.RunTimeInfo;
import com.browserstack.report.models.Embedding;
import com.browserstack.report.models.Feature;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public final class CustomReportBuilder {

    List<String> SOURCE_CSS_ASSETS = new ArrayList<>(
            Arrays.asList("report/css/chartjs.min.css"
                    , "report/css/dataTables.bootstrap4.min.css"
                    , "report/css/report.min.css"));

    List<String> SOURCE_JS_ASSETS = new ArrayList<>(
            Arrays.asList("report/js/bootstrap.bundle.min.js"
                    , "report/js/chart.min.js"
                    , "report/js/dataTables.bootstrap4.min.js"
                    , "report/js/jquery.dataTables.min.js"
                    , "report/js/jquery-3.5.1.slim.min.js"));

    private CustomReportBuilder() {
    }

    public static void createReport(String reportPath,String platform, RunTimeInfo runTimeInfo, List<Feature> features) throws IOException {
        CustomReportBuilder customReportBuilder = new CustomReportBuilder();
        customReportBuilder.create(reportPath,platform, runTimeInfo, features);
    }

    private void create(String reportPath, String platform, RunTimeInfo runTimeInfo, List<Feature> features) throws IOException {
        createDirectories(reportPath+"/css");
        createDirectories(reportPath+"/js");
        copyAssetsDirectory(SOURCE_CSS_ASSETS, reportPath + "/css");
        copyAssetsDirectory(SOURCE_JS_ASSETS, reportPath + "/js");
        reportPath = reportPath+"/"+platform;
        createDirectories(reportPath);
        generateHtmlReport(reportPath, runTimeInfo, features);
    }

    public void copyAssetsDirectory(List<String> sourceFilesPath, String destinationDirectoryLocation) {

        sourceFilesPath.forEach(filePath -> {
            InputStream in = getClass().getClassLoader().getResourceAsStream(filePath);
            try {
                assert in != null;
                Files.copy(in, Paths.get(destinationDirectoryLocation + File.separator + filePath.substring(filePath.lastIndexOf(File.separator) + 1)));
            } catch (FileAlreadyExistsException ignored){

            }catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

    private void generateHtmlReport(String reportPath, RunTimeInfo runTimeInfo, List<Feature> features) throws IOException {
        final HtmlReportBuilder htmlReportBuilder = HtmlReportBuilder.create(features);
        final List<String> results = htmlReportBuilder.getHtmlTableFeatureRows();
        final List<String> modals = htmlReportBuilder.getHtmlModals();
        final HashMap<String, Object> reportData = new HashMap<>();
        Instant now = Instant.now();
        reportData.put("reportTitle", now.toString().replace("/","-"));
        reportData.put("total", runTimeInfo.getTotal());
        reportData.put("passed", runTimeInfo.getPassed());
        reportData.put("failed", runTimeInfo.getFailed());
        reportData.put("rerun", runTimeInfo.getRerun());
        reportData.put("timestamp", now.toString());
        reportData.put("duration", runTimeInfo.getDuration());
        reportData.put("threads", runTimeInfo.getConcurrency());
        reportData.put("os_name", runTimeInfo.getOs());
        reportData.put("os_arch", runTimeInfo.getOsArch());
        reportData.put("java_version", runTimeInfo.getJavaVersion());
        reportData.put("cucumber_version", runTimeInfo.getCucumberVersion());
        reportData.put("tags", runTimeInfo.getCucumberTags());
        reportData.put("features", runTimeInfo.getGlue());
        reportData.put("results", results);
        reportData.put("modals", modals);

        String normalizedTimestamp = now.toString().replaceAll("([:.])", "-");

        File thisFile = new File(reportPath + "/" + normalizedTimestamp + ".html");
        BufferedWriter writer = new BufferedWriter(new FileWriter(thisFile, false));
        final InputStream in = getClass().getClassLoader().getResourceAsStream(String.format("%s/index.mustache", HtmlReportBuilder.MUSTACHE_TEMPLATES_DIR));
        assert in != null;
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        final Mustache report = new DefaultMustacheFactory().compile(reader, "");
        report.execute(writer, reportData);

        createImageScript(writer, features);

        writer.close();
    }

    private void createImageScript(Writer writer, List<Feature> features) throws IOException {
        final List<Embedding> embeddings = new ArrayList<>();

        features.stream().map(Feature::getScenarios)
                .flatMap(Collection::stream)
                .flatMap(t -> t.getSteps().stream())
                .flatMap(t -> t.getEmbeddings().stream())
                .forEach(embeddings::add);

        final List<Embedding> imageEmbeddings = embeddings.stream().filter(e -> e.getMimeType().startsWith("image")).collect(Collectors.toList());

        writer.write("\n<script>\n");

        for (Embedding embedding : imageEmbeddings) {
            writer.write("document.getElementById('");
            writer.write(embedding.getEmbeddingId());
            writer.write("').src='data:image;base64,");
            writer.write(Base64.getEncoder().encodeToString(embedding.getData()));
            writer.write("'\n\n");
        }

        writer.write("</script>");
    }

    private void createDirectories(String dirPath) throws IOException {

        final File reportDir = new File(dirPath);

        if (!reportDir.exists()) {
            if (!reportDir.mkdirs()) {
                throw new IOException(String.format("Unable to create the %s directory", reportDir));
            }
        }
    }
}