package io.wispforest.gadget.client.nbt;

import net.minecraft.nbt.*;

import java.util.Arrays;

public record NbtPath(String[] steps) {
    public NbtElement follow(NbtElement start) {
        for (String element : steps) {
            if (start instanceof NbtCompound compound)
                start = compound.get(element);
            else if (start instanceof AbstractNbtList<?> list)
                start = list.get(Integer.parseInt(element));
        }

        return start;
    }

    @SuppressWarnings("unchecked")
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
            ((AbstractNbtList<NbtElement>) list).set(Integer.parseInt(steps[steps.length - 1]), to);
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
