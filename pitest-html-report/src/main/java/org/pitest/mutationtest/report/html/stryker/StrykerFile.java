package org.pitest.mutationtest.report.html.stryker;

import java.util.List;

public class StrykerFile {
    private String name;
    private String source;
    private List<StrykerMutant> mutants;

    public StrykerFile(String name, String source, List<StrykerMutant> mutants) {
        this.name = name;
        this.source = source.replace("\"", "\\\"").replace("\t", " ").replace("\n", "\\n");
        this.mutants = mutants;
    }

    public String toJson(){
        return " \"" + this.name + "\": {" +
                "     \"source\": \"" + this.source + "\"," +
                "     \"mutants\": []," +
                "     \"language\": \"scala\"" +
                " }";
    }
}
