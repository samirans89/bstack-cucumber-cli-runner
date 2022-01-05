package com.browserstack.runner.reporter;

import com.browserstack.runner.reporter.model.BStackBuildSummary;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BStackAPIReporter {

    String buildId;
    int limit = 100;
    int offset = 0;

    public void create(String buildId) throws IOException {

        this.buildId = buildId;
        FileWriter myWriter = null;
        StringBuilder strBuilderCommon = new StringBuilder();
        StringBuilder strBuilderSummary = new StringBuilder();
        StringBuilder strBuilderDetails = new StringBuilder();

        try {


            Map<String, BStackBuildSummary> dataMapTotal = new HashMap<>();

            strBuilderCommon.append("<!DOCTYPE html><html><head><style>table {  font-family: arial, sans-serif;  border-collapse: collapse;  width: 100%;}td, th {  border: 1px solid #dddddd;  text-align: left;  padding: 5px;}</style></head>");
            strBuilderSummary.append("<h2>Summary</h2><br/><table style='width: 60%'><tr><th>#</th><th>Combination</th><th>Passed</th><th>Failed</th><th>Unmarked</th><th>Total</th></tr>");
            strBuilderDetails.append("<h2>Details</h2><br/><table><tr><th>#</th><th>Build Name</th><th>Project Name</th><th>Session Name</th><th>OS</th><th>OS Version</th><th>Device</th><th>Browser</th><th>Browser Version</th><th>Status</tr>");
            int rowNum = 0;
            while (true) {

                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();

                String credential = Credentials.basic(System.getenv("BROWSERSTACK_USERNAME"), System.getenv("BROWSERSTACK_ACCESS_KEY"));
                Request request = new Request.Builder()
                        .url(String.format("https://api.browserstack.com/automate/builds/%s/sessions.json?limit=%s&offset=%s", buildId, limit, offset))
                        .method("GET", null)
                        .addHeader("Authorization", credential)
                        .build();

                Response response = client.newCall(request).execute();
                assert response.body() != null;
                String jsonData = response.body().string();

                JSONArray jsonArray = new JSONArray(jsonData);
                int sessionLength = jsonArray.length();

                if (sessionLength != 0) {

                    for (int i = 0; i < sessionLength; i++) {

                        JSONObject automationSessionObj = jsonArray.getJSONObject(i).getJSONObject("automation_session");
                        String browserUrl = automationSessionObj.get("browser_url").toString().replaceAll("/builds", "/dashboard/v2/builds");
                        String sessionName = !automationSessionObj.get("name").toString().isEmpty() ? automationSessionObj.get("name").toString() : automationSessionObj.get("hashed_id").toString();
                        strBuilderDetails.append(String.format("<tr><td>%s</td><td>%s</td><td>%s</td><td><a href='%s'>%s</a></td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</tr>"
                                , ++rowNum
                                , automationSessionObj.get("build_name")
                                , automationSessionObj.get("project_name")
                                , browserUrl
                                , sessionName
                                , automationSessionObj.get("os")
                                , automationSessionObj.get("os_version").toString().split("\\.")[0]
                                , (automationSessionObj.isNull("device") ? "" : automationSessionObj.get("device"))
                                , (automationSessionObj.isNull("browser") ? "" : automationSessionObj.get("browser"))
                                , (automationSessionObj.isNull("browser_version") ? "" : automationSessionObj.get("browser_version").toString().split("\\.")[0])
                                , automationSessionObj.get("status")
                        ));
                        String key = automationSessionObj.get("os")
                                + "_"
                                + automationSessionObj.get("os_version").toString().split("\\.")[0]
                                + (automationSessionObj.isNull("browser") ? "" : "_" + automationSessionObj.get("browser"))
                                + (automationSessionObj.isNull("browser_version") ? "" : "_" + automationSessionObj.get("browser_version").toString().split("\\.")[0]);

                        if (dataMapTotal.containsKey(key)) {
                            BStackBuildSummary buildSummary = dataMapTotal.get(key);
                            updateDataMap(automationSessionObj, buildSummary);
                            dataMapTotal.put(key, buildSummary);
                        } else {
                            dataMapTotal.put(key, new BStackBuildSummary(key, 0, 0, 0));
                        }
                    }
                } else {
                    break;
                }

                offset += limit;

            }


            int summaryRowNum = 0;
            for (String key :
                    dataMapTotal.keySet()) {
                int passCount = dataMapTotal.get(key).getPassCount();
                int failCount = dataMapTotal.get(key).getFailCount();
                int unmarkedCount = dataMapTotal.get(key).getUnmarkedCount();
                strBuilderSummary.append(String.format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td>",
                        ++summaryRowNum
                        , key
                        , passCount
                        , failCount
                        , unmarkedCount
                        , (passCount + failCount + unmarkedCount)
                ));

            }

            strBuilderSummary.append("</table><br/><br/>");
            strBuilderDetails.append("</table>");
            File bstackReportDir = new File("target/reports/bstack/");
            if(bstackReportDir.isDirectory()) {
                bstackReportDir.mkdirs();
            }

            myWriter = new FileWriter(String.format(bstackReportDir.getAbsolutePath() + "/output_%s.html", buildId));
            myWriter.write(strBuilderCommon.toString());
            myWriter.write(strBuilderSummary.toString());
            myWriter.write(strBuilderDetails.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (myWriter != null)
                myWriter.close();
        }


    }

    private static void updateDataMap(JSONObject automationSessionObj, BStackBuildSummary buildSummary) {

        switch (automationSessionObj.get("status").toString().toLowerCase()) {
            case "passed":
                buildSummary.setPassCount(buildSummary.getPassCount() + 1);
                break;
            case "failed":
                buildSummary.setFailCount(buildSummary.getFailCount() + 1);
                break;
            default:
                buildSummary.setUnmarkedCount(buildSummary.getUnmarkedCount() + 1);
        }
    }
}
