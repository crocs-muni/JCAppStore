package cz.muni.crocs.appletstore.util;

/**
 * A tuple
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class Tuple<V1, V2> {
    public final V1 first;
    public final V2 second;

    public Tuple(V1 first, V2 second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Tuple)) return false;
        Tuple<V1,V2> otherTuple = (Tuple<V1, V2>) other;
        return otherTuple.first.equals(this.first) && otherTuple.second.equals(this.second);
    }

    @Override
    public int hashCode() {
        int hash = 13 * ((first == null) ? 0 : first.hashCode());
        hash = 13 * hash + ((second == null) ? 0 : second.hashCode());
        return hash;
    }
}
