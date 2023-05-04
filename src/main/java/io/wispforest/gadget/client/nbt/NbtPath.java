package io.wispforest.gadget.client.nbt;

import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.Arrays;

public record NbtPath(String[] steps) {
    public static final NbtPath EMPTY = new NbtPath(new String[0]);

    public NbtElement follow(NbtElement start) {
        for (String element : steps) {
            if (start instanceof NbtCompound compound)
                start = compound.get(element);
            else if (start instanceof AbstractNbtList<?> list)
                start = list.get(Integer.parseInt(element));
        }

        return start;
    }

    public void set(NbtElement start, NbtElement to) {
        for (int i = 0; i < steps.length - 1; i++) {
            if (start instanceof NbtCompound compound)
                start = compound.get(steps[i]);
            else if (start instanceof AbstractNbtList<?> list)
                start = list.get(Integer.parseInt(steps[i]));
        }

        if (start instanceof NbtCompound compound)
            compound.put(steps[steps.length - 1], to);
        else if (start instanceof AbstractNbtList<?> list)
            list.setElement(Integer.parseInt(steps[steps.length - 1]), to);
    }

    public void add(NbtElement start, NbtElement to) {
        for (int i = 0; i < steps.length - 1; i++) {
            if (start instanceof NbtCompound compound)
                start = compound.get(steps[i]);
            else if (start instanceof AbstractNbtList<?> list)
                start = list.get(Integer.parseInt(steps[i]));
        }

        if (start instanceof NbtCompound compound)
            compound.put(steps[steps.length - 1], to);
        else if (start instanceof AbstractNbtList<?> list)
            list.addElement(Integer.parseInt(steps[steps.length - 1]), to);
    }

    public void remove(NbtElement start) {
        for (int i = 0; i < steps.length - 1; i++) {
            if (start instanceof NbtCompound compound)
                start = compound.get(steps[i]);
            else if (start instanceof AbstractNbtList<?> list)
                start = list.get(Integer.parseInt(steps[i]));
        }

        if (start instanceof NbtCompound compound)
            compound.remove(steps[steps.length - 1]);
        else if (start instanceof AbstractNbtList<?> list)
            list.remove(Integer.parseInt(steps[steps.length - 1]));
    }

    public NbtPath parent() {
        String[] newSteps = new String[steps.length - 1];

        System.arraycopy(steps, 0, newSteps, 0, steps.length - 1);

        return new NbtPath(newSteps);
    }

    public NbtPath then(String step) {
        String[] newSteps = new String[steps.length + 1];

        System.arraycopy(steps, 0, newSteps, 0, steps.length);

        newSteps[newSteps.length - 1] = step;

        return new NbtPath(newSteps);
    }

    public String name() {
        return steps[steps.length - 1];
    }

    public boolean startsWith(NbtPath path) {
        if (steps.length < path.steps.length) return false;

        for (int i = 0; i < steps.length && i < path.steps.length; i++) {
            if (!path.steps[i].equals(steps[i]))
                return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return String.join(".", steps);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NbtPath nbtPath = (NbtPath) o;

        return Arrays.equals(steps, nbtPath.steps);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(steps);
    }
}
