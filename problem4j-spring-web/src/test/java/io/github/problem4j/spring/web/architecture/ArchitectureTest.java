/*
 * Copyright 2025-present the original author or authors.
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

package io.github.problem4j.spring.web.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import io.github.problem4j.spring.web.CachingProblemResolverStore;
import io.github.problem4j.spring.web.DefaultProblemResolverStore;
import io.github.problem4j.spring.web.ProblemResolverStore;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

  private static final String BASE_PACKAGE = "io.github.problem4j.spring.web";

  private static final JavaClasses CLASSES =
      new ClassFileImporter()
          .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
          .importPackages(BASE_PACKAGE);

  private static final JavaClasses TEST_CLASSES =
      new ClassFileImporter().importPackages(BASE_PACKAGE);

  @Test
  void givenPackageStructure_whenCheckingSlices_thenNoCyclesExist() {
    ArchRule rule = slices().matching(BASE_PACKAGE + ".(*)..").should().beFreeOfCycles();
    rule.check(CLASSES);
  }

  @Test
  void givenPackageStructure_whenCheckingAutoconfigure_thenNoOtherPackageDependsOnIt() {
    ArchRule rule =
        noClasses()
            .that()
            .resideOutsideOfPackage(BASE_PACKAGE + ".autoconfigure..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage(BASE_PACKAGE + ".autoconfigure..")
            .because("only the autoconfigure package may wire up autoconfiguration classes");
    rule.check(CLASSES);
  }

  /**
   * {@link ProblemResolverStore}, {@link DefaultProblemResolverStore} and {@link
   * CachingProblemResolverStore} are exempt because of backwards compatibility. New root-package
   * classes must still satisfy the rule.
   */
  @Test
  void givenPackageStructure_whenCheckingRootPackage_thenRootDoesNotDependOnSubpackages() {
    ArchRule rule =
        noClasses()
            .that(resideInAPackage(BASE_PACKAGE).and(not(isBackwardsCompatProblemResolverStore())))
            .should()
            .dependOnClassesThat()
            .resideInAPackage(BASE_PACKAGE + ".*..")
            .because("subpackages may depend on the root package, not the other way around");
    rule.check(CLASSES);
  }

  private static DescribedPredicate<JavaClass> isBackwardsCompatProblemResolverStore() {
    return equivalentTo(ProblemResolverStore.class)
        .or(equivalentTo(DefaultProblemResolverStore.class))
        .or(equivalentTo(CachingProblemResolverStore.class));
  }

  @Test
  void givenPackageStructure_whenCheckingPackages_thenEachPackageIsNullMarked() {
    ArchRule rule = classes().should(resideInANullMarkedPackage());
    rule.check(CLASSES);
  }

  private static ArchCondition<JavaClass> resideInANullMarkedPackage() {
    return new ArchCondition<>("reside in a package annotated with @NullMarked") {
      @Override
      public void check(JavaClass item, ConditionEvents events) {
        boolean nullMarked = item.getPackage().isAnnotatedWith(NullMarked.class);
        String message =
            String.format("Package %s is not annotated with @NullMarked", item.getPackageName());
        events.add(new SimpleConditionEvent(item, nullMarked, message));
      }
    };
  }

  @Test
  void givenTestSources_whenClassHasTestMethod_thenClassNameEndsWithTest() {
    ArchRule rule =
        classes()
            .that(haveAMethodAnnotatedWithTest())
            .should()
            .haveSimpleNameEndingWith("Test")
            .because("test classes must be named *Test (singular), not *Tests");
    rule.check(TEST_CLASSES);
  }

  private static DescribedPredicate<JavaClass> haveAMethodAnnotatedWithTest() {
    return new DescribedPredicate<>("be a top-level class with a method annotated with @Test") {
      @Override
      public boolean test(JavaClass javaClass) {
        return javaClass.getEnclosingClass().isEmpty()
            && javaClass.getMethods().stream()
                .anyMatch(method -> method.isAnnotatedWith(Test.class));
      }
    };
  }
}
