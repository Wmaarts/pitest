package org.pitest.mutationtest.report.html.stryker.models;

public enum StrykerMutantStatus {
    Killed,
    Survived,
    NoCoverage,
    CompileError,
    RuntimeError,
    Timeout
}
