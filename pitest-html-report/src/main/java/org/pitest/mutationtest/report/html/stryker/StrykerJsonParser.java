package org.pitest.mutationtest.report.html.stryker;

import org.pitest.classinfo.ClassInfo;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.SourceLocator;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

public class StrykerJsonParser {
    private String exampleJson = "{\"schemaVersion\":\"1\",\"thresholds\":{\"high\":80,\"low\":60},\"files\":{\"core/src/main/scala/stryker4s/mutants/ Mutator.scala\":{\"source\":\"package stryker4s.mutants\\n\\nimport better.files.File\\nimport grizzled.slf4j.Logging\\nimport stryker4s.model.{MutatedFile, MutationsInSource, SourceTransformations}\\nimport stryker4s.mutants.applymutants.{MatchBuilder, StatementTransformer}\\nimport stryker4s.mutants.findmutants.MutantFinder\\n\\nimport scala.meta.Tree\\n\\nclass Mutator(mutantFinder: MutantFinder, transformer: StatementTransformer, matchBuilder: MatchBuilder)\\n    extends Logging {\\n\\n  def mutate(files: Iterable[File]): Iterable[MutatedFile] = {\\n    val mutatedFiles = files\\n      .map { file =>\\n        val mutationsInSource = findMutants(file)\\n        val transformed = transformStatements(mutationsInSource)\\n        val builtTree = buildMatches(transformed)\\n\\n        MutatedFile(file, builtTree, mutationsInSource.mutants, mutationsInSource.excluded)\\n      }\\n      .filterNot(mutatedFile => mutatedFile.mutants.isEmpty && mutatedFile.excludedMutants == 0)\\n\\n    logMutationResult(mutatedFiles, files.size)\\n\\n    mutatedFiles\\n  }\\n\\n  /** Step 1: Find mutants in the found files\\n    */\\n  private def findMutants(file: File): MutationsInSource = mutantFinder.mutantsInFile(file)\\n\\n  /** Step 2: transform the statements of the found mutants (preparation of building pattern matches)\\n    */\\n  private def transformStatements(mutants: MutationsInSource): SourceTransformations =\\n    transformer.transformSource(mutants.source, mutants.mutants)\\n\\n  /** Step 3: Build pattern matches from transformed trees\\n    */\\n  private def buildMatches(transformedMutantsInSource: SourceTransformations): Tree =\\n    matchBuilder.buildNewSource(transformedMutantsInSource)\\n\\n  private def logMutationResult(mutatedFiles: Iterable[MutatedFile], totalAmountOfFiles: Int): Unit = {\\n    val includedMutants = mutatedFiles.flatMap(_.mutants).size\\n    val excludedMutants = mutatedFiles.map(_.excludedMutants).sum\\n    val totalMutants = includedMutants + excludedMutants\\n\\n    info(s\\\"Found ${mutatedFiles.size} of $totalAmountOfFiles file(s) to be mutated.\\\")\\n    info(s\\\"$totalMutants Mutant(s) generated.${if (excludedMutants > 0)\\n      s\\\" Of which $excludedMutants Mutant(s) are excluded.\\\"\\n    else \\\"\\\"}\\\")\\n\\n    if (totalAmountOfFiles == 0) {\\n      warn(s\\\"No files marked to be mutated. ${dryRunText(\\\"mutate\\\")}\\\")\\n    } else if (includedMutants == 0 && excludedMutants > 0) {\\n      warn(s\\\"All found mutations are excluded. ${dryRunText(\\\"excluded-mutations\\\")}\\\")\\n    } else if (totalMutants == 0) {\\n      info(\\\"Files to be mutated are found, but no mutations were found in those files.\\\")\\n      info(\\\"If this is not intended, please check your configuration and try again.\\\")\\n    }\\n\\n    def dryRunText(configProperty: String): String =\\n      s\\\"\\\"\\\"Stryker4s will perform a dry-run without actually mutating anything.\\n         |You can configure the `$configProperty` property in your configuration\\\"\\\"\\\".stripMargin\\n  }\\n\\n}\\n\",\"mutants\":[{\"id\":\"0\",\"mutatorName\":\"LogicalOperator\",\"replacement\":\"||\",\"location\":{\"start\":{\"line\":23,\"column\":61},\"end\":{\"line\":23,\"column\":63}},\"status\":\"Killed\"},{\"id\":\"1\",\"mutatorName\":\"LogicalOperator\",\"replacement\":\"||\",\"location\":{\"start\":{\"line\":56,\"column\":37},\"end\":{\"line\":56,\"column\":39}},\"status\":\"Killed\"}],\"language\":\"scala\"}}}";

    private final Collection<SourceLocator> sourceRoots;
    private final CoverageDatabase          coverage;

    public StrykerJsonParser(final Collection<SourceLocator> sourceRoots, final CoverageDatabase coverage){
        this.coverage = coverage;
        this.sourceRoots = sourceRoots;
    }

    private List<ClassMutationResults> mutationResults = new ArrayList<>();

    public String getJson() throws IOException {
        // Step 1: Map mutations by file
        final Map<String, List<ClassMutationResults>> mappedByName = mutationResults.stream()
                .collect(Collectors.groupingBy(ClassMutationResults::getFileName));

        final ArrayList<StrykerFile> strykerFiles = new ArrayList<>();

        for(String key: mappedByName.keySet()){
            final List<ClassMutationResults> mutationResults = mappedByName.get(key);
            final ClassMutationResults result = mutationResults.get(0);
            final Collection<ClassInfo> classes = this.coverage.getClassesForFile(key, result.getPackageName());
            final String source = this.getSource(this.classInfoToNames(classes), result.getFileName());
            if(!source.isEmpty()){
                strykerFiles.add(new StrykerFile(key, source, new ArrayList<>()));
            }
        }

        String beginJson = "{" +
                "    \"schemaVersion\": \"1\"," +
                "    \"thresholds\": {" +
                "        \"high\": 80," +
                "        \"low\": 60" +
                "    }," +
                "    \"files\": {";
        String endJson = "} }";
        StringBuilder builder = new StringBuilder();
        builder.append(beginJson);
        for(int i = 0; i < strykerFiles.size() - 2; i++){
            StrykerFile file = strykerFiles.get(i);
            builder.append(file.toJson());
            builder.append(",");
        }
        StrykerFile lastFile = strykerFiles.get(strykerFiles.size() - 1);
        builder.append(lastFile.toJson());
        builder.append(endJson);
        return builder.toString();
    }

    public void addMutationResult(final ClassMutationResults mutationResult){
        mutationResults.add(mutationResult);
    }

    private String getSource(final Collection<String> classes,
                             final String fileName) throws IOException{
        for (final SourceLocator each : this.sourceRoots) {
            final Optional<Reader> maybe = each.locate(classes, fileName);
            if (maybe.isPresent()) {
                return readerToString(maybe.get());
            }
        }
//        throw new IOException("File " + fileName + " not found");
        return "";
    }

    private Collection<String> classInfoToNames(
            final Collection<ClassInfo> classes) {
        return FCollection.map(classes, a -> a.getName().asJavaName());
    }

    private String readerToString(final Reader reader) throws java.io.IOException {
        final StringBuilder fileData = new StringBuilder(1000);
        char[] buf = new char[1024];
        int numRead = 0;

        while ((numRead = reader.read(buf)) != -1) {
            final String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        return fileData.toString();
    }
}
