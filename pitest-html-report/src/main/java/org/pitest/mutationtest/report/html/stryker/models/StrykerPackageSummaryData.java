package org.pitest.mutationtest.report.html.stryker.models;

import org.pitest.mutationtest.report.html.stryker.utils.StrykerMutationTestSummaryDataFileNameComparator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StrykerPackageSummaryData
    implements Comparable<StrykerPackageSummaryData> {

  private final String                                      packageName;
  private final Map<String, StrykerMutationTestSummaryData> fileNameToSummaryData = new HashMap<>();

  public StrykerPackageSummaryData(final String packageName) {
    this.packageName = packageName;
  }

  public void addSummaryData(final StrykerMutationTestSummaryData data) {
    final StrykerMutationTestSummaryData existing = this.fileNameToSummaryData
        .get(data.getFileName());
    if (existing == null) {
      this.fileNameToSummaryData.put(data.getFileName(), data);
    } else {
      existing.add(data);
    }
  }

  public String getPackageName() {
    return this.packageName;
  }

  public List<StrykerMutationTestSummaryData> getSummaryData() {
    final ArrayList<StrykerMutationTestSummaryData> values = new ArrayList<>(
        this.fileNameToSummaryData.values());
    values.sort(new StrykerMutationTestSummaryDataFileNameComparator());
    return values;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((this.packageName == null) ?
        0 :
        this.packageName.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final StrykerPackageSummaryData other = (StrykerPackageSummaryData) obj;
    if (this.packageName == null) {
      if (other.getPackageName() != null) {
        return false;
      }
    } else if (!this.packageName.equals(other.packageName)) {
      return false;
    }
    return true;
  }

  @Override
  public int compareTo(final StrykerPackageSummaryData arg0) {
    return this.packageName.compareTo(arg0.packageName);
  }
}