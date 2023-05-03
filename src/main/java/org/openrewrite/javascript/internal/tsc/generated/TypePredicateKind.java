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
package org.openrewrite.javascript.internal.tsc.generated;

//
// THIS FILE IS GENERATED. Do not modify it by hand.
// See `js/README.md` for instructions to regenerate this file.
//

public enum TypePredicateKind {
    This(0),
    Identifier(1),
    AssertsThis(2),
    AssertsIdentifier(3);


    public final int code;

    TypePredicateKind(int code) {
        this.code = code;
    }

    public static TypePredicateKind fromCode(int code) {
        switch (code) {
            case 0:
                return TypePredicateKind.This;
            case 1:
                return TypePredicateKind.Identifier;
            case 2:
                return TypePredicateKind.AssertsThis;
            case 3:
                return TypePredicateKind.AssertsIdentifier;
            default:
                throw new IllegalArgumentException("unknown TypePredicateKind code: " + code);
        }
    }
}
