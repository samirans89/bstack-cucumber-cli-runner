# bstack-cucumber-cli-runner
Cucumber CLI runner based test suite parallelization across multiple platforms.

How to use this repository?

1. Clone the repository on your machine.
2. Build this repository using the below Maven command
```shell
mvn install
```
3. Copy the `.jar` file generated in the target directory into your Cucumber test project directory within a `lib` folder.  
 
 Filename: `lib/<artifact-id-from-pom>-<artifact-version-from-pom>.jar`

 Example: `lib/bstack-cucumber-cli-runner-0.0.1.jar`

4. Add the WebDriver Framework Core and the Cucumber CLI runner `.jar` dependency in your test project's Maven POM file.

```xml

    <dependencies>
        <dependency>
            <groupId>com.browserstack</groupId>
            <artifactId>webdriver-framework-core</artifactId>
            <version>${wedriver.core.version}</version>
        </dependency>
        <dependency>
            <groupId>com.browserstack</groupId>
            <artifactId>bstack-cucumber-cli-runner</artifactId>
            <version>&lt;artifact-version&gt;</version>
            <scope>system</scope>
            <systemPath>${project.basedir}/libs/com/browserstack/runner/bstack-cucumber-cli-runner-0.0.1.jar</systemPath>
        </dependency>
    </dependencies>

```
5. Create a Junit4 runner file in your test project.

``` java

package <your-test-project-package-name>;
import org.junit.runner.RunWith;

@RunWith(CucumberCLIRunner.class)
public class RunCucumberTest {
}
```

5. Set the Cucumber JVM, WebDriver and other system properties, as required by your project

Example:
```shell
# Cucumber system properties 
-Dcucumber.features=<your-feature-files-parent-directory>
-Dcucumber.glue=<your-cucumber-step-definitions>
-Dcucumber.plugin=<your-cucumber-plugins>
-Dcucumber.tags=<your-feature-or-scenario-tags>
```

The below System properties are expected by the Cucumber CLI framework.
```shell
# This is for identifying the WebDriver Config file
# Note: This is the path of .yml file relative to source root (/src directory)
# e.g. resources/<folder-path-to-conf>/<config-file-name.yml>

-Dcapabilities.config=<webdriver-capabilities-config-file-path>
```

The below Cucumber properties are recommended by the framework.
```shell
# Used to display the Cucumber version in Custom HTML reporting.
-Dcucumber.version=<cucumber-version> 

# Creates multiple Cucumber threads which enables 
# Cucumber scenarios or Scenario Outline examples
# to run in parallel.
-Dcucumber.threads=<cucumber-threads-max-count> 
```