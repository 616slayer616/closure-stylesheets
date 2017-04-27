/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.css;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * {@link IdentitySubstitutionMapTest} is a unit test for {@link IdentitySubstitutionMap}.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
@RunWith(JUnit4.class)
public class IdentitySubstitutionMapTest {

  @Test
  public void testNull() {
    IdentitySubstitutionMap map = new IdentitySubstitutionMap();
    try {
      map.get(null);
      Assert.fail();
    } catch (NullPointerException e) {
      // OK.
    }
  }

  @Test
  public void testGet() {
    IdentitySubstitutionMap map = new IdentitySubstitutionMap();
    assertThat(map.get("")).isEmpty();
    assertThat(map.get("a")).isEqualTo("a");
    assertThat(map.get("foo")).isEqualTo("foo");
  }
}
