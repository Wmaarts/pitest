package org.pitest.mutationtest.report.html.stryker;

import org.pitest.coverage.CoverageDatabase;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.SourceLocator;
import org.pitest.mutationtest.report.html.stryker.models.StrykerMutationTestSummaryData;
import org.pitest.mutationtest.report.html.stryker.models.StrykerPackageSummaryMap;
import org.pitest.mutationtest.report.html.stryker.utils.StrykerJsonParser;
import org.pitest.util.FileUtil;
import org.pitest.util.Log;
import org.pitest.util.ResultOutputStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;

public class MutationStrykerReportListener implements MutationResultListener {

  private final ResultOutputStrategy outputStrategy;

  private final StrykerJsonParser strykerJsonParser;

  private final CoverageDatabase         coverage;
  private final StrykerPackageSummaryMap packageSummaryData = new StrykerPackageSummaryMap();

  public MutationStrykerReportListener(final CoverageDatabase coverage,
      final ResultOutputStrategy outputStrategy, final SourceLocator... locators) {
    this.coverage = coverage;
    this.outputStrategy = outputStrategy;
    this.strykerJsonParser = new StrykerJsonParser(
        new HashSet<>(Arrays.asList(locators)));
  }

  private String loadStrykerHtml() {
    final String startHtml = "<!DOCTYPE html>\n" + "<html>\n" + "<body>\n"
        + "  <mutation-test-report-app title-postfix=\"Stryker4s report\">\n"
        + "    Your browser doesn't support <a href=\"https://caniuse.com/#search=custom%20elements\">custom elements</a>.\n"
        + "    Please use a latest version of an evergreen browser (Firefox, Chrome, Safari, Opera, etc).\n"
        + "  </mutation-test-report-app>\n"
        + "  <script src=\"report.js\"></script>\n" + "  <script>";
    final String endHtml = "  </script>\n" + "</body>\n" + "</html>";
    try {
      final String reportVersion = "1.0.2";
      final String htmlReportResource =
          "META-INF/resources/webjars/mutation-testing-elements/" + reportVersion + "/dist/mutation-test-elements.js";
      final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(htmlReportResource);
      return startHtml + FileUtil.readToString(inputStream) + endHtml;
    } catch (final IOException e) {
      Log.getLogger().log(Level.SEVERE, "Error while loading css", e);
    }
    return "";
  }

  private void createStrykerHtml() {
    final String content = this.loadStrykerHtml();
    final Writer strykerWriter = this.outputStrategy
        .createWriterForFile("index.html");
    try {
      strykerWriter.write(content);
      strykerWriter.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private void createStrykerJs(final String json) {
    final String content =
        "document.querySelector('mutation-test-report-app').report = " + json;
    final Writer strykerWriter = this.outputStrategy
        .createWriterForFile("report.js");
    try {
      strykerWriter.write(content);
      strykerWriter.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private StrykerMutationTestSummaryData createSummaryData(
      final CoverageDatabase coverage, final ClassMutationResults data) {
    return new StrykerMutationTestSummaryData(data.getFileName(),
        data.getMutations(), coverage.getClassInfo(Collections.singleton(data.getMutatedClass())));
  }

  private void updatePackageSummary(
      final ClassMutationResults mutationMetaData) {
    final String packageName = mutationMetaData.getPackageName();

    this.packageSummaryData.update(packageName,
        createSummaryData(this.coverage, mutationMetaData));
  }

  @Override
  public void runStart() {
    // Nothing to do
  }

  @Override
  public void handleMutationResult(ClassMutationResults metaData) {
    updatePackageSummary(metaData);
  }

  @Override
  public void runEnd() {
    try {
      String json = strykerJsonParser.toJson(this.packageSummaryData);
      createStrykerHtml();
      createStrykerJs(json);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
