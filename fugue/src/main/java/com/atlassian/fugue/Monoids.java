/*
   Copyright 2015 Atlassian

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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.atlassian.fugue.Either.right;
import static com.atlassian.fugue.Iterables.flatten;
import static com.atlassian.fugue.Monoid.monoid;
import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.some;
import static com.atlassian.fugue.Semigroups.*;
import static java.util.Collections.emptyList;

/**
 * {@link Monoid} instances and factories.
 */
public final class Monoids {

  /**
   * A monoid that adds integers.
   */
  public static final Monoid<Integer> intAdditionMonoid = monoid(intAdditionSemigroup, 0);

  /**
   * A monoid that multiplies integers.
   */
  public static final Monoid<Integer> intMultiplicationMonoid = monoid(intMultiplicationSemigroup, 1);

  /**
   * A monoid that adds doubles.
   */
  public static final Monoid<Double> doubleAdditionMonoid = monoid(doubleAdditionSemigroup, 0.0);

  /**
   * A monoid that adds big integers.
   */
  public static final Monoid<BigInteger> bigintAdditionMonoid = monoid(bigintAdditionSemigroup, BigInteger.ZERO);

  /**
   * A monoid that multiplies big integers.
   */
  public static final Monoid<BigInteger> bigintMultiplicationMonoid = monoid(bigintMultiplicationSemigroup, BigInteger.ONE);

  /**
   * A monoid that adds longs.
   */
  public static final Monoid<Long> longAdditionMonoid = monoid(longAdditionSemigroup, 0L);

  /**
   * A monoid that multiplies longs.
   */
  public static final Monoid<Long> longMultiplicationMonoid = monoid(longMultiplicationSemigroup, 1L);

  /**
   * A monoid that ORs booleans.
   */
  public static final Monoid<Boolean> disjunctionMonoid = monoid(disjunctionSemigroup, false);

  /**
   * A monoid that XORs booleans.
   */
  public static final Monoid<Boolean> exclusiveDisjunctionMonoid = monoid(exclusiveDisjunctionSemiGroup, false);

  /**
   * A monoid that ANDs booleans.
   */
  public static final Monoid<Boolean> conjunctionMonoid = monoid(conjunctionSemigroup, true);

  /**
   * A monoid that appends strings.
   */
  public static final Monoid<String> stringMonoid = new Monoid<String>() {
    @Override public String empty() {
      return "";
    }

    @Override public String append(String a1, String a2) {
      return stringSemigroup.append(a1, a2);
    }

    @Override public String join(Iterable<String> strings) {
      StringBuilder sb = new StringBuilder();
      for (String s : strings) {
        sb.append(s);
      }
      return sb.toString();
    }
  };

  /**
   * A monoid for the Unit value.
   */
  public static final Monoid<Unit> unitMonoid = monoid(unitSemigroup, Unit.VALUE);

  private Monoids() {
  }

  /**
   * A monoid for functions.
   *
   * @param mb The monoid for the function codomain.
   * @return A monoid for functions.
   */
  public static <A, B> Monoid<Function<A, B>> functionMonoid(final Monoid<B> mb) {
    return monoid(functionSemigroup(mb), f -> mb.empty());
  }

  /**
   * A monoid for lists.
   *
   * @return A monoid for lists.
   */
  public static <A> Monoid<List<A>> listMonoid() {
    return new Monoid<List<A>>() {
      @Override public List<A> append(final List<A> a1, final List<A> a2) {
        return Semigroups.<A>listSemigroup().append(a1, a2);
      }

      @Override public List<A> empty() {
        return emptyList();
      }

      @Override public List<A> join(final Stream<List<A>> ll) {
        return ll.flatMap(l -> l.stream()).collect(Collectors.toList());
      }

      @Override public List<A> join(final Iterable<List<A>> ll) {
        final List<A> r = new ArrayList<>();
        for (final List<A> l : ll) {
          r.addAll(l);
        }
        return r;
      }
    };
  }

  /**
   * A monoid for iterables.
   *
   * @return A monoid for iterables.
   */
  public static <A> Monoid<Iterable<A>> iterableMonoid() {
    return new Monoid<Iterable<A>>() {
      @Override public Iterable<A> empty() {
        return emptyList();
      }

      @Override public Iterable<A> append(Iterable<A> l1, Iterable<A> l2) {
        return Semigroups.<A>iterableSemigroup().append(l1, l2);
      }

      @Override public Iterable<A> join(Iterable<Iterable<A>> iterables) {
        return flatten(iterables);
      }
    };
  }

  /**
   * A monoid for options (that take the first available value).
   *
   * @return A monoid for options (that take the first available value).
   */
  public static <A> Monoid<Option<A>> firstOptionMonoid() {
    return monoid(firstOptionSemigroup(), none());
  }

  /**
   * A monoid for options that take the last available value.
   *
   * @return A monoid for options that take the last available value.
   */
  public static <A> Monoid<Option<A>> lastOptionMonoid() {
    return monoid(Semigroups.lastOptionSemigroup(), none());
  }

  /**
   * A monoid for options that combine inner value with a semigroup.
   *
   * @return A monoid for options that combine inner value with a semigroup.
   */
  public static <A> Monoid<Option<A>> optionMonoid(Semigroup<A> semigroup) {
    return monoid((o1, o2) -> o1.fold(() -> o2, a1 -> o2.fold(() -> o1, a2 -> some(semigroup.append(a1, a2)))), none());
  }

  /**
   * A monoid Sums up values inside either {@see Semigroups#eitherSemigroup}.
   * Monoid of right values provide the identity element of the resulting monoid.
   *
   * @param lS semigroup for left values
   * @param rM monoid for right values.
   * @return A monoid Sums up values inside either.
   */
  public static <L, R> Monoid<Either<L, R>> eitherMonoid(Semigroup<L> lS, Monoid<R> rM) {
    return monoid(eitherSemigroup(lS, rM), right(rM.empty()));
  }

}
