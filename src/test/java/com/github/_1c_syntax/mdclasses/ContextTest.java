/*
 * This file is a part of MDClasses.
 *
 * Copyright © 2019 - 2020
 * Tymko Oleg <olegtymko@yandex.ru>, Maximov Valery <maximovvalery@gmail.com> and contributors
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * MDClasses is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * MDClasses is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with MDClasses.
 */
package com.github._1c_syntax.mdclasses;

import com.github._1c_syntax.mdclasses.metadata.Configuration;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class ContextTest {

  @Test
  void test_basic() {

    var configuration = Configuration.create();
    assertThat(configuration.getGlobalMethod("ЗаполнитьЗначенияСвойств")).isPresent();
    assertThat(configuration.getGlobalMethod("заполнитьзначениясвойств")).isPresent();
    assertThat(configuration.getGlobalMethod("FillPropertyValues")).isPresent();
    assertThat(configuration.getGlobalMethod("НеГлобальныйМетод")).isEmpty();

    // только с версией 8.3.15
    assertThat(configuration.getGlobalMethod("ПолучитьРазмерДанныхБазыДанных")).isEmpty();
  }

  @Test
  void test_version8_3_15() {
    var srcPath = new File("src/test/resources/metadata/versions/version8_3_15");
    var configuration = Configuration.create(srcPath.toPath());

    assertThat(configuration.getGlobalMethod("ПолучитьРазмерДанныхБазыДанных")).isPresent();
  }

}
