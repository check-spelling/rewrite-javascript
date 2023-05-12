/*
 * Copyright 2023 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.javascript.tree;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ExpectedToFail;

@SuppressWarnings({"JSUnusedLocalSymbols", "JSUnresolvedVariable"})
public class VariableDeclarationTest extends ParserTest {

    @Test
    void let() {
        rewriteRun(
          javascript(
            """
              let hello = "World" ;
              """
          )
        );
    }

    @ExpectedToFail("Added support for union type expressions")
    @Test
    void multiTypeLet() {
        rewriteRun(
          javascript(
            """
              let stringWord : string | null ;
              """
          )
        );
    }

    @Test
    void constant() {
        rewriteRun(
          javascript(
            """
              const hello = "World" ;
              """
          )
        );
    }

    @Test
    void var() {
        rewriteRun(
          javascript(
            """
              var hello = "World" ;
              """
          )
        );
    }

    @ExpectedToFail
    @Test
    void multiTypeVariableDeclaration() {
        rewriteRun(
          javascript(
            """
              let x: number, y: string;
              """
          )
        );
    }

    @Test
    void generic() {
        rewriteRun(
          javascript(
            """
              var v : Array < string > = [ 'foo' , 'bar', 'buz' ] ;
              """
          )
        );
    }
}
