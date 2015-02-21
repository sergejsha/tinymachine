package de.halfbit.tinymachine;

class OnEntry {
    public int state;
    public OnEntry(int state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof OnEntry) {
            return ((OnEntry)o).state == state;
        }
        return false;
    }

    @Override
    public String toString() {
        return "OnEntry{" +
                "state=" + state +
                '}';
    }
}
