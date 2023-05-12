/*
 * Copyright 2022 the original author or authors.
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

@SuppressWarnings("JSUnusedLocalSymbols")
class CommentTest extends ParserTest {

    @Test
    void singleLineComment() {
        rewriteRun(
          javascript(
            """
              class Test {
                  // C1
              }
              """
          )
        );
    }

    @Test
    void multilineNestedInsideSingleLine() {
        rewriteRun(
          javascript(
            """
              class Test { // /*
              }
              """
          )
        );
    }

    @Test
    void multilineComment() {
        rewriteRun(
          javascript(
            """
              class Test {
                  /*
                    C1
                   */
              }
              """
          )
        );
    }

    @Test
    void javaScriptDoc() {
        rewriteRun(
          javascript(
            """
              var s ;
              
              /**
               * @param {string} p1
               * @param {string} p2
               */
              function f ( p1 , p2 ) {
              }
              """
          )
        );
    }
}
