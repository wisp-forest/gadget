// Portions of this code are copied from NEC.
//
// Copyright (c) 2021 Fudge and NEC contributors
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package io.wispforest.gadget.mappings;

import io.wispforest.gadget.util.ProgressToast;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class LoadingMappings implements Mappings {
    private volatile Map<String, String> intermediaryToFieldMap = Collections.emptyMap();
    private volatile Map<String, String> intermediaryToClassMap = Collections.emptyMap();
    private volatile Map<String, String> intermediaryFromClassMap = Collections.emptyMap();
    private volatile Map<String, String> fieldIdToIntermediaryMap = Collections.emptyMap();

    public LoadingMappings() {
        ProgressToast toast = ProgressToast.create(Text.translatable("message.gadget.loading_mappings"));
        toast.follow(CompletableFuture.runAsync(() -> {
            MappingTree tree = new MemoryMappingTree();
            load(toast, (MappingVisitor) tree);

            var classMap = new HashMap<String, String>();
            var classUnmap = new HashMap<String, String>();
            var fieldMap = new HashMap<String, String>();

            for (var def : tree.getClasses()) {
                String intermediary = def.getName("intermediary");
                String named = def.getName("named");

                if (intermediary != null && named != null) {
                    classMap.put(intermediary, named);
                    classUnmap.put(named, intermediary);
                }

                for (var field : def.getFields()) {
                    fieldMap.put(field.getName("intermediary"), field.getName("named"));
                }
            }

            intermediaryToFieldMap = fieldMap;
            intermediaryToClassMap = classMap;
            intermediaryFromClassMap = classUnmap;
            fieldIdToIntermediaryMap = MappingUtils.createFieldIdUnmap(tree, "named");
        }), false);
    }

    protected abstract void load(ProgressToast toast, MappingVisitor visitor);

    @Override
    public String mapClass(String src) {
        src = src.replace('.', '/');

        return intermediaryToClassMap.getOrDefault(src, src).replace('/', '.');
    }

    @Override
    public String mapField(String src) {
        return intermediaryToFieldMap.getOrDefault(src, src);
    }

    @Override
    public String unmapClass(String dst) {
        dst = dst.replace('.', '/');

        return intermediaryFromClassMap.getOrDefault(dst, dst).replace('/', '.');
    }

    @Override
    public String unmapFieldId(String dst) {
        return fieldIdToIntermediaryMap.getOrDefault(dst, dst);
    }

    @Override
    public void load(MappingVisitor visitor) {
        load(new ProgressToast.Dummy(), visitor);
    }
}
