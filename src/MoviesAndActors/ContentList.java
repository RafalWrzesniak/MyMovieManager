package MoviesAndActors;

import FileOperations.AutoSave;
import FileOperations.XMLOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class ContentList<T extends ContentType<T>> {

    private static final Logger logger = LoggerFactory.getLogger(ContentList.class.getName());
    public static final String ALL_ACTORS_DEFAULT = "allActors";
    public static final String ALL_MOVIES_DEFAULT = "allMovies";
    public static final String MOVIES_TO_WATCH = "moviesToWatch";
    private final List<T> list = new ArrayList<>();
    private final String listName;

    public ContentList(String listName) {
        this.listName = ContentType.checkForNullOrEmptyOrIllegalChar(listName, "listName");
    }

    public List<T> getList() {
        return new ArrayList<>(list);
    }

    public boolean contains(T obj) {
        return indexOf(obj) >= 0;
    }

    public int indexOf(T obj) {
//        Collections.sort(list);
//        return Collections.binarySearch(list, obj);
        return list.indexOf(obj);
    }

    public T get(int index) {
        return list.get(index);
    }

    public T get(T object) {
        if(contains(object)) {
            return list.get(indexOf(object));
        }
        return null;
    }

    public synchronized boolean add(T obj) {
        if(addFromXml(obj)) {
            if(list.size() == 1) {
                XMLOperator.createListFile(this);
            }
            XMLOperator.updateSavedContentListWith(this, obj);
            return true;
        }
        return false;
    }

    public boolean addFromXml(T obj) {
        if(obj == null) {
            logger.warn("Null object will not be added to the list \"{}\"!", getListName());
            return false;
        }
        if(contains(obj)) {
            synchronized (AutoSave.NEW_OBJECTS) {
                logger.warn("\"{}\" is already on the {} list", get(obj).toString(), getListName());
                AutoSave.NEW_OBJECTS.remove(obj);
                return false;
            }
        }
        list.add(obj);
        logger.debug("\"{}\" added to \"{}\"", obj.toString(), getListName());
        return true;
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
        XMLOperator.removeContentList(this);
        list.clear();
        logger.debug("\"{}\" cleared", getListName());
    }


    public List<T> find(String strToFind) {
        List<T> results = new ArrayList<>();
        if (strToFind == null || strToFind.isEmpty()) return null;
        for(T obj : list) {
            if(obj.searchFor(strToFind)) {
                results.add(obj);
            }
        }
        return results;
    }

    public T getById(int id) {
        for (T obj : list) {
            if (obj.getId() == id) {
                return obj;
            }
        }
        return null;
    }

    public List<T> convertStrIdsToObjects(List<String> strList) {
        List<T> targetList = new ArrayList<>();
        for(String str : strList) {
            try {
                targetList.add(getById(Integer.parseInt(str)));
            } catch (NumberFormatException e) {
                logger.warn("Couldn't convert \"{}\" to int", str);
            }

        }
        return targetList;
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public static <E extends ContentType<E>> ContentList<E> getContentListFromListByName(List<ContentList<E>> contentLists, String listName) {
        if(contentLists == null) return null;
        ContentList<E> desiredContentList = null;
        for(ContentList<E> contentList : contentLists) {
            if (contentList != null && contentList.getListName().equals(listName)) {
                desiredContentList = contentList;
            }
        }
        return desiredContentList;
    }

    @Override
    public String toString() {
        return list.toString();
    }
}
