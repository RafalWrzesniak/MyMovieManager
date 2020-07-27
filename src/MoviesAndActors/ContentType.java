package MoviesAndActors;

public interface ContentType<T> extends Comparable<T> {
    boolean searchFor(String strToFind);
}
