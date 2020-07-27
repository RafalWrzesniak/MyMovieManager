package MoviesAndActors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ContentList<T extends ContentType<T>> {

    private static final Logger logger = LoggerFactory.getLogger(ContentList.class.getName());
    private final List<T> list = new ArrayList<>();
    private final String listName;

    public ContentList(String listName) {
        this.listName = Actor.checkForNullOrEmptyOrIllegalChar(listName, "listName");
    }

    public List<T> getList() {
        return new ArrayList<>(list);
    }

    public boolean contains(T obj) {
        return indexOf(obj) != -1;
    }

    public int indexOf(T obj) {
        return Collections.binarySearch(list, obj);
    }

    public T get(int index) {
        return list.get(index);
    }

    public void add(T obj) {
        if(contains(obj)) {
            logger.warn("\"{}\" is already on the {} list", obj.toString(), getListName());
        } else {
            list.add(obj);
            logger.debug("\"{}\" added to \"{}\"", obj.toString(), getListName());
        }
    }

    public void addAll(List<T> objList) {
        objList.forEach(this::add);
    }

    public String getListName() {
        return listName;
    }

    public int size() {
        return list.size();
    }

    public boolean remove(T obj) {
        logger.debug("\"{}\" removed from \"{}\"", obj.toString(), getListName());
        return list.remove(obj);
    }

    public boolean remove(int index) {
        if(index < list.size()) {
            logger.debug("\"{}\" removed from \"{}\"", list.get(index), getListName());
            list.remove(index);
            return true;
        } else {
            return false;
        }
    }

    public void clear() {
        list.clear();
        logger.debug("\"{}\" cleared", getListName());
    }


    public List<T> find(String strToFind) {
        if (strToFind == null || strToFind.isEmpty() || list.size() == 0) return null;
        List<T> results = new ArrayList<>();
        for(T obj : list) {
            if(obj.searchFor(strToFind)) {
                results.add(obj);
            }
        }
        if(results.size() == 0) return null;
        return results;
    }



}
