package org.pitest.mutationtest.report.html.stryker.models;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StrykerFile {
  private String              source  = "";
  private List<StrykerMutant> mutants = new ArrayList<>();

  @Expose(serialize = false, deserialize = false)
  private int idCounter = 0;

  public StrykerFile() {
  }

  public void addMutants(final Collection<StrykerMutant> mutants) {
    mutants.forEach(this::addMutant);
  }

  public void addMutant(final StrykerMutant mutant) {
    mutant.setId(idCounter++);
    this.mutants.add(mutant);
  }

  public void addSource(String source) {
    if (this.source.isEmpty()) {
      this.source = source;
    }
  }

  //    public String toJson(){
  //        String startJson = " \"" + this.name + "\": {" +
  //            "     \"source\": \"" + this.source + "\"," +
  //            "     \"mutants\": [";
  //        String endJson = "]," +
  //            "     \"language\": \"java\"" +
  //            " }";
  //        StringBuilder builder = new StringBuilder();
  //        builder.append(startJson);
  //        for (int i = 0; i < this.mutants.size() - 2; i++) {
  //            StrykerMutant mutant = this.mutants.get(i);
  //            builder.append(mutant.toJson());
  //            builder.append(",");
  //        }
  //        StrykerMutant lastMutant = this.mutants.get(this.mutants.size() - 1);
  //        builder.append(lastMutant.toJson());
  //        builder.append(endJson);
  //        return builder.toString();
  //    }
}
