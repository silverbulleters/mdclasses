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
