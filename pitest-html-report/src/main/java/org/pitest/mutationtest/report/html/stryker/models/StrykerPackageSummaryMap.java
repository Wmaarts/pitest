package org.pitest.mutationtest.report.html.stryker.models;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class StrykerPackageSummaryMap {

  private final Map<String, StrykerPackageSummaryData> packageSummaryData = new TreeMap<>();

  private StrykerPackageSummaryData getPackageSummaryData(final String packageName) {
    StrykerPackageSummaryData psData;
    if (this.packageSummaryData.containsKey(packageName)) {
      psData = this.packageSummaryData.get(packageName);
    } else {
      psData = new StrykerPackageSummaryData(packageName);
      this.packageSummaryData.put(packageName, psData);
    }
    return psData;
  }

  public StrykerPackageSummaryData update(final String packageName,
      final StrykerMutationTestSummaryData data) {
    final StrykerPackageSummaryData psd = getPackageSummaryData(packageName);
    psd.addSummaryData(data);
    return psd;
  }

  public Collection<StrykerPackageSummaryData> values() {
    return this.packageSummaryData.values();
  }

}
