package org.pitest.mutationtest.report.html.stryker;

import com.google.gson.Gson;
import org.pitest.classinfo.ClassInfo;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.SourceLocator;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.report.html.AnnotatedLineFactory;
import org.pitest.mutationtest.report.html.Line;
import org.pitest.mutationtest.report.html.MutationTestSummaryData;
import org.pitest.mutationtest.report.html.PackageSummaryData;
import org.pitest.mutationtest.report.html.stryker.models.*;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class StrykerJsonParser {
  private final Collection<SourceLocator> sourceRoots;
  private final CoverageDatabase          coverage;

  private int id = 0;

  public StrykerJsonParser(final Collection<SourceLocator> sourceRoots,
      final CoverageDatabase coverage) {
    this.coverage = coverage;
    this.sourceRoots = sourceRoots;
  }

  //  private List<ClassMutationResults> mutationResults = new ArrayList<>();
  private final Map<String, StrykerFile> collectedStrykerFiles = new HashMap<>();
  private final Set<MutationIdentifier>  parsedMutations       = new HashSet<>();

  private final Gson gson = new Gson();

  public String getJson() throws IOException {
    StrykerReport report = new StrykerReport(collectedStrykerFiles);
    return gson.toJson(report);

    //    final String beginJson =
    //        "{" + "\"schemaVersion\": \"1\"," + "\"thresholds\": {"
    //            + "\"high\": 80," + "\"low\": 60" + "}," + "\"files\": {";
    //    final String endJson = "} }";
    //    StringBuilder builder = new StringBuilder();
    //    builder.append(beginJson);
    //    for (int i = 0; i < collectedStrykerFiles.size() - 2; i++) {
    //      StrykerFile file = collectedStrykerFiles.get(i);
    //      builder.append(file.toJson());
    //      builder.append(",");
    //    }
    //    StrykerFile lastFile = collectedStrykerFiles
    //        .get(collectedStrykerFiles.size() - 1);
    //    builder.append(lastFile.toJson());
    //    builder.append(endJson);
    //    return builder.toString();
  }

  public void addPackageSummaryData(final PackageSummaryData packageSummaryData)
      throws IOException {
    for (MutationTestSummaryData summaryData : packageSummaryData
        .getSummaryData()) {
      this.addToStrykerFiles(packageSummaryData);
    }
  }

  private void addToStrykerFiles(PackageSummaryData packageSummaryData)
      throws IOException {
    for (MutationTestSummaryData data : packageSummaryData.getSummaryData()) {
      // Step 1: Map mutations to Stryker mutations
      final List<MutationResult> mutationResults = data.getResults().list();
      final List<StrykerMutant> strykerMutants = new ArrayList<>();
      final List<Line> lines = getLines(data);
      for (Line line : lines) {
        for (MutationResult mutationResult : line.getMutations()) {
          if (this.parsedMutations.add(mutationResult.getDetails().getId())) {
            strykerMutants.add(this.mapPiMutantToStryker(mutationResult, line));
          }
        }
      }
      // Step 2: Check if we can find the source
      final String source = this.parseLinesToString(lines);
      if (!source.isEmpty()) {
        // Step 3: Add mutations to file
        if (this.collectedStrykerFiles.get(data.getFileName()) == null) {
          this.collectedStrykerFiles.put(data.getFileName(), new StrykerFile());
        }
        StrykerFile file = this.collectedStrykerFiles.get(data.getFileName());
        file.addMutants(strykerMutants);
        file.addSource(source);
      }
    }
  }

  private String parseLinesToString(final List<Line> lines) {
    if (lines.isEmpty()) {
      return "";
    }
    StringBuilder builder = new StringBuilder();
    for (Line line : lines) {
      builder.append(line.getText());
      builder.append("\n");
    }
    return builder.toString();
  }

  private List<Line> getLines(final MutationTestSummaryData summaryData)
      throws IOException {
    final String fileName = summaryData.getFileName();
    final Collection<ClassInfo> classes = summaryData.getClasses();
    final Optional<Reader> reader = findReaderForSource(classes, fileName);
    if (reader.isPresent()) {
      final AnnotatedLineFactory alf = new AnnotatedLineFactory(
          summaryData.getResults().list(), this.coverage, classes);
      return alf.convert(reader.get());
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
      Line line) {
    final String mutatorName = mutation.getDetails().getMutator();
    final StrykerMutantStatus status = this
        .mapPiStatusToStryker(mutation.getStatus());
    final StrykerLocation location = StrykerLocation.ofLine(line);
    return new StrykerMutant(-1, // Will be set later
        mutatorName.substring(mutatorName.lastIndexOf(".") + 1),
        mutation.getDetails().getDescription(), location, status);
  }

  private StrykerMutantStatus mapPiStatusToStryker(DetectionStatus status) {
    switch (status) {
    case KILLED:
      return StrykerMutantStatus.Killed;
    case MEMORY_ERROR:
      return StrykerMutantStatus.RuntimeError;
    case NO_COVERAGE:
      return StrykerMutantStatus.NoCoverage;
    case RUN_ERROR:
      return StrykerMutantStatus.RuntimeError;
    case SURVIVED:
      return StrykerMutantStatus.Survived;
    case TIMED_OUT:
      return StrykerMutantStatus.Timeout;
    // If there's an internal state at the end, something probably went wrong
    case NON_VIABLE:
    case STARTED:
    case NOT_STARTED:
    default:
      return StrykerMutantStatus.RuntimeError;
    }
  }
}
