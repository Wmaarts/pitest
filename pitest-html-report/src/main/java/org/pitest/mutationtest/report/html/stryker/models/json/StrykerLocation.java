package org.pitest.mutationtest.report.html.stryker.models.json;

import org.pitest.mutationtest.report.html.stryker.models.StrykerLine;

public class StrykerLocation {
  private LineAndColumn start, end;

  public StrykerLocation(final LineAndColumn start, final LineAndColumn end) {
    this.start = start;
    this.end = end;
  }

  public String toJson() {
    return " \"start\": {" + "     \"line\": " + this.start.line + ","
        + "     \"column\": " + this.start.column + " " + " }," + " \"end\": {"
        + "     \"line\": " + this.end.line + "," + "     \"column\": "
        + this.start.column + " " + " }";
  }

  public static StrykerLocation ofLine(StrykerLine line) {
    final LineAndColumn start = new LineAndColumn((int) line.getNumber(), 1);
    final LineAndColumn end = new LineAndColumn((int) line.getNumber(), line.getText().length());
    return new StrykerLocation(start, end);
  }
}

class LineAndColumn {
  int line;
  int column;

  public LineAndColumn(final int line, final int column) {
    this.line = line;
    this.column = column;
  }
}
