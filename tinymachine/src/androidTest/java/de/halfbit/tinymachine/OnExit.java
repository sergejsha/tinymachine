package de.halfbit.tinymachine;

public class OnExit {
    public int state;
    public OnExit(int state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof  OnExit) {
            return ((OnExit)o).state == state;
        }
        return false;
    }

    @Override
    public String toString() {
        return "OnExit{" +
                "state=" + state +
                '}';
    }
}
