package org.pitest.mutationtest.report.html.stryker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.pitest.classinfo.ClassInfo;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.SourceLocator;
import org.pitest.mutationtest.report.html.stryker.models.StrykerLine;
import org.pitest.mutationtest.report.html.stryker.models.StrykerMutationTestSummaryData;
import org.pitest.mutationtest.report.html.stryker.models.StrykerPackageSummaryData;
import org.pitest.mutationtest.report.html.stryker.models.StrykerPackageSummaryMap;
import org.pitest.mutationtest.report.html.stryker.models.json.*;
import org.pitest.mutationtest.report.html.stryker.utils.StrykerLineFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class StrykerJsonParser {
  private final Collection<SourceLocator> sourceRoots;

  public StrykerJsonParser(final Collection<SourceLocator> sourceRoots) {
    this.sourceRoots = sourceRoots;
  }

  private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

  public String toJson(final StrykerPackageSummaryMap packageSummaryMap)
      throws IOException {
    final Map<String, StrykerFile> collectedStrykerFiles = new HashMap<>();

    for (StrykerPackageSummaryData data : packageSummaryMap.values()) {
      this.addToStrykerFiles(collectedStrykerFiles, data);
    }

    StrykerReport report = new StrykerReport(collectedStrykerFiles);
    return gson.toJson(report, StrykerReport.class);
  }
  int i = 0;

  private void addToStrykerFiles(
      final Map<String, StrykerFile> collectedStrykerFiles,
      final StrykerPackageSummaryData packageSummaryData) throws IOException {
    for (StrykerMutationTestSummaryData data : packageSummaryData
        .getSummaryData()) {
      // Step 1: Map mutations to Stryker mutations
      final List<StrykerMutant> strykerMutants = new ArrayList<>();
      final List<StrykerLine> lines = getLines(data);
      if (lines.isEmpty()) {
        i++;
        continue;
      }
      for (final StrykerLine line : lines) {
        for (MutationResult mutationResult : line.getMutations()) {
          strykerMutants.add(this.mapPiMutantToStryker(mutationResult, line));
        }
      }
      // Step 2: Check if we can find the source
      final String source = this.parseLinesToString(lines);
      if (!source.isEmpty()) {
        // Step 3: Add mutations to file
        final String fullPath =
            data.getPackageName() + "/" + data.getFileName();
        if (collectedStrykerFiles.get(fullPath) == null) {
          collectedStrykerFiles.put(fullPath, new StrykerFile());
        }
        final StrykerFile file = collectedStrykerFiles.get(fullPath);
        file.addMutants(strykerMutants);
        file.addSource(source);
      }
    }
  }

  private String parseLinesToString(final List<StrykerLine> lines) {
    if (lines.isEmpty()) {
      return "";
    }
    StringBuilder builder = new StringBuilder();
    for (final StrykerLine line : lines) {
      builder.append(line.getText());
      builder.append("\n");
    }
    return builder.toString();
  }

  private List<StrykerLine> getLines(
      final StrykerMutationTestSummaryData summaryData) throws IOException {
    final String fileName = summaryData.getFileName();
    final Collection<ClassInfo> classes = summaryData.getClasses();
    final Optional<Reader> reader = findReaderForSource(classes, fileName);
    if (reader.isPresent()) {
      final StrykerLineFactory lineFactory = new StrykerLineFactory(
          summaryData.getResults());
      return lineFactory.convert(reader.get());
    }
    return Collections.emptyList();
  }

  private Optional<Reader> findReaderForSource(
      final Collection<ClassInfo> classes, final String fileName) {
    for (final SourceLocator each : this.sourceRoots) {
      final Optional<Reader> maybe = each
          .locate(this.classInfoToNames(classes), fileName);
      if (maybe.isPresent())
        return maybe;
    }
    return Optional.empty();
  }

  private Collection<String> classInfoToNames(
      final Collection<ClassInfo> classes) {
    return FCollection.map(classes, a -> a.getName().asJavaName());
  }

  private StrykerMutant mapPiMutantToStryker(MutationResult mutation,
      StrykerLine line) {
    final String mutatorName = mutation.getDetails().getMutator();
    final StrykerMutantStatus status = StrykerMutantStatus
        .fromPitestStatus(mutation.getStatus());
    final StrykerLocation location = StrykerLocation.ofLine(line);
    return new StrykerMutant(-1, // Will be set later
        mutatorName.substring(mutatorName.lastIndexOf(".") + 1),
        mutation.getDetails().getDescription(), location, status);
  }
}
