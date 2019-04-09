package org.pitest.mutationtest.report.html.stryker.utils;

import org.pitest.mutationtest.report.html.stryker.models.StrykerMutationTestSummaryData;

import java.io.Serializable;
import java.util.Comparator;

public class StrykerMutationTestSummaryDataFileNameComparator implements
Comparator<StrykerMutationTestSummaryData>, Serializable {

  private static final long serialVersionUID = 1L;

  @Override
  public int compare(final StrykerMutationTestSummaryData arg0,
      final StrykerMutationTestSummaryData arg1) {
    return arg0.getFileName().compareTo(arg1.getFileName());
  }

}
