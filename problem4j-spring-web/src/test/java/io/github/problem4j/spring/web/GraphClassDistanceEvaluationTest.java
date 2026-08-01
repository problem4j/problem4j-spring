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

package io.github.problem4j.spring.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GraphClassDistanceEvaluationTest {

  static class BaseClass {}

  static class Level1Class extends BaseClass {}

  static class Level2Class extends Level1Class {}

  interface BaseInterface {}

  interface Level1Interface extends BaseInterface {}

  interface Level2Interface extends Level1Interface {}

  static class ImplementerClass extends Level2Class implements Level2Interface {}

  static class UnrelatedClass {}

  interface UnrelatedInterface {}

  @Nested
  class ClassToClassHierarchy {

    @Test
    void givenSameClass_whenDistance_thenZero() {
      ClassDistanceEvaluation evaluation = new GraphClassDistanceEvaluation();

      int distance = evaluation.calculate(Level2Class.class, Level2Class.class);

      assertEquals(0, distance);
    }

    @Test
    void givenChildToObject_whenDistance_thenCorrectValue() {
      ClassDistanceEvaluation evaluation = new GraphClassDistanceEvaluation();

      int distance = evaluation.calculate(Level2Class.class, Object.class);

      assertEquals(3, distance);
    }

    @Test
    void givenTwoArgCalculate_whenDistance_thenUsesDefaultMaxDepth() {
      ClassDistanceEvaluation evaluation = new GraphClassDistanceEvaluation();

      int withDefault = evaluation.calculate(Level2Class.class, Object.class);
      int withExplicit =
          evaluation.calculate(
              Level2Class.class, Object.class, ClassDistanceEvaluation.DEFAULT_MAX_DEPTH);

      assertEquals(withExplicit, withDefault);
    }

    @Test
    void givenLowMaxDepth_whenDistance_thenShortCircuitValue() {
      ClassDistanceEvaluation evaluation = new GraphClassDistanceEvaluation();
      ClassDistanceEvaluation limitedEvaluation = new GraphClassDistanceEvaluation(2);

      int distance = evaluation.calculate(ImplementerClass.class, Object.class);
      int limitedDistance = limitedEvaluation.calculate(ImplementerClass.class, Object.class);

      assertEquals(4, distance);
      assertEquals(3, limitedDistance);
    }

    @Test
    void givenChildToParent_whenDistance_thenCorrectValue() {
      ClassDistanceEvaluation evaluation = new GraphClassDistanceEvaluation();

      int distance = evaluation.calculate(Level2Class.class, BaseClass.class);

      assertEquals(2, distance);
    }

    @Test
    void givenChildToImmediateParent_whenDistance_thenOne() {
      ClassDistanceEvaluation evaluation = new GraphClassDistanceEvaluation();

      int distance = evaluation.calculate(Level2Class.class, Level1Class.class);

      assertEquals(1, distance);
    }

    @Test
    void givenClassToImmediateObject_whenDistance_thenOne() {
      ClassDistanceEvaluation evaluation = new GraphClassDistanceEvaluation();

      int distance = evaluation.calculate(BaseClass.class, Object.class);

      assertEquals(1, distance);
    }

    @Test
    void givenClassToItselfAsObject_whenDistance_thenZero() {
      ClassDistanceEvaluation evaluation = new GraphClassDistanceEvaluation();

      int distance = evaluation.calculate(Object.class, Object.class);

      assertEquals(0, distance);
    }
  }

  @Nested
  class InterfaceToInterfaceHierarchy {

    @Test
    void givenSameInterface_whenDistance_thenZero() {
      ClassDistanceEvaluation evaluation = new GraphClassDistanceEvaluation();

      int distance = evaluation.calculate(Level2Interface.class, Level2Interface.class);

      assertEquals(0, distance);
    }

    @Test
    void givenSubInterfaceToSuperInterface_whenDistance_thenCorrectValue() {
      ClassDistanceEvaluation evaluation = new GraphClassDistanceEvaluation();

      int distance = evaluation.calculate(Level2Interface.class, BaseInterface.class);

      assertEquals(2, distance);
    }

    @Test
    void givenSubInterfaceToImmediateSuperInterface_whenDistance_thenOne() {
      ClassDistanceEvaluation evaluation = new GraphClassDistanceEvaluation();

      int distance = evaluation.calculate(Level2Interface.class, Level1Interface.class);

      assertEquals(1, distance);
    }
  }

  @Nested
  class ClassToInterfaceHierarchy {

    @Test
    void givenImplementerToInterface_whenDistance_thenCorrectValue() {
      // ImplementerClass -> Level2Interface -> Level1Interface -> BaseInterface
      ClassDistanceEvaluation evaluation = new GraphClassDistanceEvaluation();

      int distance = evaluation.calculate(ImplementerClass.class, BaseInterface.class);

      assertEquals(3, distance);
    }

    @Test
    void givenImplementerToImmediateInterface_whenDistance_thenOne() {
      ClassDistanceEvaluation evaluation = new GraphClassDistanceEvaluation();

      int distance = evaluation.calculate(ImplementerClass.class, Level2Interface.class);

      assertEquals(1, distance);
    }

    @Test
    void givenClassImplementingInterfaceToInterface_whenDistance_thenCorrectValue() {
      ClassDistanceEvaluation evaluation = new GraphClassDistanceEvaluation();

      int distance = evaluation.calculate(Level2Class.class, Cloneable.class);

      assertEquals(Integer.MAX_VALUE, distance);
    }

    @Test
    void givenUnrelatedClassToClass_whenDistance_thenMaxValue() {
      ClassDistanceEvaluation evaluation = new GraphClassDistanceEvaluation();

      int distance = evaluation.calculate(Level2Class.class, UnrelatedClass.class);

      assertEquals(Integer.MAX_VALUE, distance);
    }

    @Test
    void givenParentToChildClass_whenDistance_thenMaxValue() {
      ClassDistanceEvaluation evaluation = new GraphClassDistanceEvaluation();

      int distance = evaluation.calculate(BaseClass.class, Level2Class.class);

      assertEquals(Integer.MAX_VALUE, distance);
    }

    @Test
    void givenUnrelatedClassToInterface_whenDistance_thenMaxValue() {
      ClassDistanceEvaluation evaluation = new GraphClassDistanceEvaluation();

      int distance = evaluation.calculate(Level2Class.class, UnrelatedInterface.class);

      assertEquals(Integer.MAX_VALUE, distance);
    }

    @Test
    void givenClassToUnrelatedInterface_whenDistance_thenMaxValue() {
      ClassDistanceEvaluation evaluation = new GraphClassDistanceEvaluation();

      int distance = evaluation.calculate(ImplementerClass.class, UnrelatedInterface.class);

      assertEquals(Integer.MAX_VALUE, distance);
    }

    @Test
    void givenInterfaceToImplementingClass_whenDistance_thenMaxValue() {
      ClassDistanceEvaluation evaluation = new GraphClassDistanceEvaluation();

      int distance = evaluation.calculate(BaseInterface.class, ImplementerClass.class);

      assertEquals(Integer.MAX_VALUE, distance);
    }
  }

  @Nested
  class ComplexAndMultiPathScenarios {

    interface I1 {}

    interface I2 {}

    interface I3 extends I1 {}

    static class C1 implements I3, I2 {}

    static class C2 extends C1 implements I1 {}

    @Test
    void givenClassToMultiExtendedInterface_whenDistance_thenShortestPath() {
      // C1 -> I3 -> I1 (Distance 2)
      int distance = new GraphClassDistanceEvaluation().calculate(C1.class, I1.class);
      assertEquals(2, distance);

      // C2 -> C1 -> I1 (Distance 2)
      distance = new GraphClassDistanceEvaluation().calculate(C2.class, I2.class);
      assertEquals(2, distance);

      // C1 -> I2 (Distance 1)
      distance = new GraphClassDistanceEvaluation().calculate(C1.class, I2.class);
      assertEquals(1, distance);
    }

    interface A {}

    interface B extends A {}

    interface C extends B {}

    static class D implements B, C {}

    @Test
    void givenDiamondStructure_whenDistance_thenShortestPath() {
      // D -> B -> A (Distance 2) OR D -> C -> B -> A (Distance 3)
      int distance = new GraphClassDistanceEvaluation().calculate(D.class, A.class);
      assertEquals(2, distance);
    }
  }

  @Nested
  class PrimitiveAndArrayTypes {

    @Test
    void givenPrimitiveToInt_whenDistance_thenMaxValue() {
      int distance = new GraphClassDistanceEvaluation().calculate(int.class, Integer.class);
      assertEquals(Integer.MAX_VALUE, distance);
    }

    @Test
    void givenArrayToObject_whenDistance_thenCorrectValue() {
      int distance = new GraphClassDistanceEvaluation().calculate(String[].class, Object.class);
      assertEquals(1, distance);
    }

    @Test
    void givenArrayToCloneable_whenDistance_thenCorrectValue() {
      int distance = new GraphClassDistanceEvaluation().calculate(String[].class, Cloneable.class);
      assertEquals(1, distance);
    }
  }

  @Nested
  class InclusionFlags {

    @Test
    void givenSuperclassExcluded_whenSearchingSuperclassHierarchy_thenMaxValue() {
      ClassDistanceEvaluation evaluation =
          new GraphClassDistanceEvaluation(HierarchyTraversalMode.INTERFACES);

      int distance = evaluation.calculate(Level2Class.class, BaseClass.class);

      assertEquals(Integer.MAX_VALUE, distance);
    }

    @Test
    void givenSuperclassExcluded_butInterfaceMatchExists_thenStillFindInterfacePath() {
      ClassDistanceEvaluation evaluation =
          new GraphClassDistanceEvaluation(HierarchyTraversalMode.INTERFACES);

      int distance = evaluation.calculate(ImplementerClass.class, BaseInterface.class);

      assertEquals(3, distance);
    }

    @Test
    void givenInterfacesExcluded_whenSearchingInterfaceHierarchy_thenMaxValue() {
      ClassDistanceEvaluation evaluation =
          new GraphClassDistanceEvaluation(HierarchyTraversalMode.SUPERCLASS);

      int distance = evaluation.calculate(ImplementerClass.class, BaseInterface.class);

      assertEquals(Integer.MAX_VALUE, distance);
    }

    @Test
    void givenInterfacesExcluded_butSuperclassMatchExists_thenStillFindSuperclassPath() {
      ClassDistanceEvaluation evaluation =
          new GraphClassDistanceEvaluation(HierarchyTraversalMode.SUPERCLASS);

      int distance = evaluation.calculate(Level2Class.class, Level1Class.class);

      assertEquals(1, distance);
    }
  }
}
