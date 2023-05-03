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
package org.openrewrite.javascript.internal.tsc;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.primitive.V8ValueBoolean;
import com.caoccao.javet.values.primitive.V8ValueString;
import com.caoccao.javet.values.primitive.V8ValueUndefined;
import com.caoccao.javet.values.reference.V8ValueArray;
import com.caoccao.javet.values.reference.V8ValueObject;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.javascript.internal.tsc.generated.TSCSyntaxKind;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class TSCNode implements TSCV8Backed {
    private final TSCProgramContext programContext;
    public final V8ValueObject nodeV8;

    public TSCNode(TSCProgramContext programContext, V8ValueObject nodeV8) {
        this.programContext = programContext;
        this.nodeV8 = nodeV8;
    }

    public int syntaxKindCode() {
        try {
            return nodeV8.getInteger("kind");
        } catch (JavetException e) {
            throw new RuntimeException(e);
        }
    }
    public TSCSyntaxKind syntaxKind() {
        return TSCSyntaxKind.fromCode(this.syntaxKindCode());
    }

    @Nullable
    public TSCType getTypeForNode() {
        try {
            V8Value type = this.programContext.getTypeChecker().invoke("getTypeAtLocation", this.nodeV8);
            if (type.isNullOrUndefined()) {
                return null;
            } else {
                return this.programContext.tscType((V8ValueObject) type);
            }
        } catch (JavetException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public TSCSymbol getSymbolForNode() {
        try {
            V8Value type = this.programContext.getTypeChecker().invoke("getSymbolAtLocation", this.nodeV8);
            if (type.isNullOrUndefined()) {
                return null;
            } else {
                return this.programContext.tscSymbol((V8ValueObject) type);
            }
        } catch (JavetException e) {
            throw new RuntimeException(e);
        }
    }

    public int getStart() {
        try {
            return this.nodeV8.getPropertyInteger("pos");
        } catch (JavetException e) {
            throw new RuntimeException(e);
        }
    }

    public int getEnd() {
        try {
            return this.nodeV8.getPropertyInteger("end");
        } catch (JavetException e) {
            throw new RuntimeException(e);
        }
    }

    public int getChildCount() {
        try {
            return this.nodeV8.invokeInteger("getChildCount");
        } catch (JavetException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public TSCNode getChildNode(String name) {
        try {
            V8Value child = this.nodeV8.getProperty(name);
            if (child.isNullOrUndefined()) {
                return null;
            }
            return programContext.tscNode((V8ValueObject) child);
        } catch (JavetException e) {
            throw new RuntimeException(e);
        }
    }

    public TSCNode getChildNodeRequired(String name) {
        TSCNode child = this.getChildNode(name);
        if (child == null) {
            throw new IllegalArgumentException("property " + name + " is not required");
        }
        return child;
    }

    public List<TSCNode> getChildNodes(String name) {
        try (V8ValueArray children = this.nodeV8.getProperty(name)) {
            final int childCount = children.getLength();
            List<TSCNode> result = new ArrayList<>(childCount);
            for (int i = 0; i < childCount; i++) {
                result.add(programContext.tscNode(children.get(i)));
            }
            return result;
        } catch (JavetException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean getBooleanPropertyValue(String propertyName) {
        V8Value val;
        try {
            val = this.nodeV8.getProperty(propertyName);
        } catch (JavetException ignored) {
            throw new IllegalStateException(String.format("Property <%s> does not exist on syntaxKind %s", propertyName, this.syntaxKind()));
        }

        boolean propertyValue;
        if (val instanceof V8ValueBoolean) {
            propertyValue = ((V8ValueBoolean) val).getValue();
        } else {
            throw new IllegalStateException(String.format("Property <%s> is not a boolean type.", propertyName));
        }
        return propertyValue;
    }

    public String getStringPropertyValue(String propertyName) {
        V8Value val;
        try {
            val = this.nodeV8.getProperty(propertyName);
        } catch (JavetException ignored) {
            throw new IllegalStateException(String.format("Property <%s> does not exist on syntaxKind %s", propertyName, this.syntaxKind()));
        }

        String propertyValue = "";
        if (val instanceof V8ValueString) {
            propertyValue = ((V8ValueString) val).getValue();
        } else {
            throw new IllegalStateException(String.format("Property <%s> is not a boolean type.", propertyName));
        }
        return propertyValue;
    }

    public boolean hasProperty(String propertyName) {
        boolean isFound = false;
        try {
            isFound = !(this.nodeV8.getProperty(propertyName) instanceof V8ValueUndefined);
        } catch (JavetException ignored) {
        }
        return isFound;
    }

    public String getText() {
        try {
            return this.nodeV8.invokeString("getText");
        } catch (JavetException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> collectChildNodes(String name, Function<TSCNode, @Nullable T> fn) {
        List<T> results = new ArrayList<>();
        for (TSCNode child : this.getChildNodes(name)) {
            @Nullable T result = fn.apply(child);
            if (result != null) {
                results.add(result);
            }
        }
        return results;
    }

    public <T> List<T> mapChildNodes(String name, Function<TSCNode, @Nullable T> fn) {
        List<T> results = new ArrayList<>();
        for (TSCNode child : this.getChildNodes(name)) {
            results.add(fn.apply(child));
        }
        return results;
    }

    public void forEachChild(Consumer<TSCNode> callback) {
        Consumer<V8Value> v8Callback = v8Value -> callback.accept(programContext.tscNode((V8ValueObject) v8Value));
        try (V8Value v8Function = programContext.asJSFunction(v8Callback)) {
            this.nodeV8.invoke("forEachChild", v8Function);
        } catch (JavetException e) {
            throw new RuntimeException(e);
        }
    }

    public void printTree(PrintStream ps) {
        printTree(ps, "");
    }

    // FIXME: Remove. Temporary method to view object
    public V8ValueObject getObject() {
        return nodeV8;
    }


    // FIXME: Remove. Temporary method to view context
    public TSCProgramContext getContext() {
        return this.programContext;
    }

    public List<String> getOwnPropertyNames() {
        try {
            return this.nodeV8.getOwnPropertyNames().getOwnPropertyNameStrings();
        } catch (JavetException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<String> getPropertyNames() {
        try {
            return this.nodeV8.getPropertyNames().getOwnPropertyNameStrings();
        } catch (JavetException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void printTree(PrintStream ps, String indent) {
        ps.print(indent);
        ps.print(syntaxKind());
        if (syntaxKind().name().contains("Literal")) {
            ps.print(" (" + this.getText() + ")");
        }
        ps.println();

        String childIndent = indent + "  ";
        forEachChild(child -> {
            child.printTree(ps, childIndent);
        });

    }

    @Override
    public V8ValueObject getBackingV8Object() {
        return this.nodeV8;
    }
}