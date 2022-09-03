package me.basiqueevangelist.gadget.path;

import org.jetbrains.annotations.NotNull;

public record ObjectPath(PathStep[] steps) implements Comparable<ObjectPath> {
    public static ObjectPath EMPTY = new ObjectPath(new PathStep[0]);

    public Object follow(Object o) {
        for (PathStep step : steps) {
            o = step.follow(o);
        }

        return o;
    }

    public String name() {
        return steps[steps.length - 1].toString();
    }

    public ObjectPath parent() {
        PathStep[] newSteps = new PathStep[steps.length - 1];

        System.arraycopy(steps, 0, newSteps, 0, steps.length - 1);

        return new ObjectPath(newSteps);
    }

    public ObjectPath then(PathStep step) {
        PathStep[] newSteps = new PathStep[steps.length + 1];

        System.arraycopy(steps, 0, newSteps, 0, steps.length);

        newSteps[newSteps.length - 1] = step;

        return new ObjectPath(newSteps);
    }

    @Override
    public int compareTo(@NotNull ObjectPath o) {
        for (int i = 0; i < o.steps.length && i < steps.length; i++) {
            int compared = steps[i].toString().compareTo(o.steps[i].toString());

            if (compared != 0)
                return compared;
        }

        return steps.length - o.steps.length;
    }
}
