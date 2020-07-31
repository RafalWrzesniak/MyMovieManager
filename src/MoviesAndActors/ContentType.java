package MoviesAndActors;

import java.util.Map;

public interface ContentType<T> extends Comparable<T> {
    boolean searchFor(String strToFind);
    int getId();
    Map<String, String> getAllFieldsAsStrings();
    String getReprName();
    void saveMe();
}
