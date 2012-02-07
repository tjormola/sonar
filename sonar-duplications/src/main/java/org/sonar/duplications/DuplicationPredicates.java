/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2012 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.duplications;

import com.google.common.annotations.Beta;
import com.google.common.base.Predicate;
import org.sonar.duplications.index.CloneGroup;

@Beta
public final class DuplicationPredicates {

  private DuplicationPredicates() {
  }

  public static Predicate<CloneGroup> numberOfUnitsNotLessThan(int min) {
    return new NumberOfUnitsNotLessThan(min);
  }

  private static class NumberOfUnitsNotLessThan implements Predicate<CloneGroup> {
    private final int min;

    public NumberOfUnitsNotLessThan(int min) {
      this.min = min;
    }

    public boolean apply(CloneGroup input) {
      return input.getLengthInUnits() >= min;
    }
  }

}