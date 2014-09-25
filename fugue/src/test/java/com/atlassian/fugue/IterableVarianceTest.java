/*
   Copyright 2011 Atlassian

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.atlassian.fugue;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.List;

import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

public class IterableVarianceTest {

  @Test public void flatMap() {
    final Iterable<String> result = Iterables.flatMap(asList("123", "ABC"), new Function<CharSequence, List<String>>() {
      public List<String> apply(final CharSequence from) {
        return ImmutableList.copyOf(new IterablesTest.CharSplitter(from));
      }
    });
    assertThat(result, contains("1", "2", "3", "A", "B", "C"));
  }
}
