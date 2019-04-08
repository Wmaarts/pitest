package org.pitest.mutationtest.report.html.stryker;

public class StrykerMutant {
    private String id;
    private String mutatorName;
    private String replacement;
    private StrykerLocation location;
    private String status;

    public StrykerMutant(String id, String mutatorName, String replacement, StrykerLocation location, String status) {
        this.id = id;
        this.mutatorName = mutatorName;
        this.replacement = replacement;
        this.location = location;
        this.status = status;
    }
    
    public String toJson(){
        return "{" +
                " \"id\": \"" + this.id + "\"," +
                " \"mutatorName\": \"" + this.mutatorName + "\"," +
                " \"replacement\": \"||\"," +
                " \"location\": {" + location.toJson() +
                " }," +
                " \"status\": \"" + this.status + "\"" +
                "}";
    }
}
