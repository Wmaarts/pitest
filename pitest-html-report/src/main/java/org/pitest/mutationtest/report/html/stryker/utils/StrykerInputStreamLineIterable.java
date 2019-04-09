package org.pitest.mutationtest.report.html.stryker.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

public class StrykerInputStreamLineIterable implements Iterable<String> {

  private final BufferedReader reader;
  private       String         next;

  public StrykerInputStreamLineIterable(final Reader reader) {
    this.reader = new BufferedReader(reader);
    advance();
  }

  private void advance() {
    try {
      this.next = this.reader.readLine();
    } catch (final IOException e) {
      this.next = null;
    }
  }

  public String next() {
    final String t = this.next;
    advance();
    return t;
  }

  @Override
  public Iterator<String> iterator() {
    return new Iterator<String>() {

      @Override
      public boolean hasNext() {
        return StrykerInputStreamLineIterable.this.next != null;
      }

      @Override
      public String next() {
        return StrykerInputStreamLineIterable.this.next();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }

}