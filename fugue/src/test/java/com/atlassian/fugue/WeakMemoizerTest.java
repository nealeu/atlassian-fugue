package com.atlassian.fugue;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.function.Function;

import org.junit.Ignore;
import org.junit.Test;

import com.atlassian.fugue.WeakMemoizer.MappedReference;

public class WeakMemoizerTest {

  static Function<Integer, String> lock() {
    return Functions.fromSupplier(() -> "test");
  }

  @Test public void callingTwiceReturnsSame() throws Exception {
    final WeakMemoizer<Integer, String> memoizer = WeakMemoizer.weakMemoizer(lock());
    assertSame(memoizer.apply(1), memoizer.apply(1));
  }

  // TODO test fails due to the refrences being the same
  // todo-alex JDK8 compatibility
  @Ignore @Test public void callingDifferentMemoizersReturnsDifferent() throws Exception {
    assertNotSame(WeakMemoizer.weakMemoizer(lock()).apply(1), WeakMemoizer.weakMemoizer(lock()).apply(1));
  }

  @Test public void lockReferenceNotNull() throws Exception {
    final MappedReference<String, String> ref = new MappedReference<>("test", "value",
      new ReferenceQueue<>());
    assertNotNull(ref.getDescriptor());
    assertNotNull(ref.get());
  }

  @Test(expected = NullPointerException.class) public void referenceNullDescriptor() throws Exception {
    new MappedReference<String, String>(null, "value", new ReferenceQueue<>());
  }

  @Test(expected = NullPointerException.class) public void referenceNullValue() throws Exception {
    new MappedReference<String, String>("ref", null, new ReferenceQueue<>());
  }

  @Test public void many() throws Exception {
    final WeakMemoizer<Integer, String> memoizer = WeakMemoizer.weakMemoizer(lock());

    final int size = 10000;
    for (int i = 0; i < 10; i++) {
      System.gc();
      for (int j = 0; j < size; j++) {
        assertSame(memoizer.apply(j), memoizer.apply(j));
      }
    }
  }

  // TODO test fails to lose reference
  // todo-alex JDK8 compatibility
  @Ignore @Test public void losesReference() throws Exception {
    final WeakMemoizer<Integer, String> memoizer = WeakMemoizer.weakMemoizer(lock());

    final WeakReference<String> one = new WeakReference<>(memoizer.apply(1));
    for (int i = 0; i < 10; i++) {
      System.gc();
    }
    assertNotNull(memoizer.apply(1));
    assertNull(one.get());
  }
}
