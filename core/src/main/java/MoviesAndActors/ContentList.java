package MoviesAndActors;

import Configuration.Config;
import FileOperations.AutoSave;
import FileOperations.XMLOperator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ContentList<T extends ContentType> {

//    == constants ==
    public static final String ALL_ACTORS_DEFAULT = "allActors";
    public static final String ALL_MOVIES_DEFAULT = "allMovies";
    public static final String MOVIES_TO_WATCH = "moviesToWatch";
    public static final String RECENTLY_WATCHED = "recentlyWatched";

//    == fields ==
    private static final List<String> NAMES = new ArrayList<>();
    @Getter private final List<T> list = new ArrayList<>();
    @Getter private  String listName;
    @Getter private String displayName;


//    == constructors ==
    public ContentList(String listName) {
        setName(listName);
        log.info("New ContentList object created: \"{}\"", listName);
    }


//    == static methods ==
    public static <E extends ContentType> ContentList<E> getContentListFromListByName(List<ContentList<E>> contentLists, String listName) {
        if(contentLists == null) return null;
        ContentList<E> desiredContentList = null;
        for(ContentList<E> contentList : contentLists) {
            if (contentList != null && contentList.getListName().equals(listName)) {
                desiredContentList = contentList;
            }
        }
        return desiredContentList;
    }


//    == methods ==
    private void setName(String listName) {
        if(NAMES.contains(listName)) {
            throw new NullPointerException(listName + " is already defined in the scope, change name of the list!");
        }

        String tmpListName = null;
        try {
            tmpListName = ContentType.checkForNullOrEmptyOrIllegalChar(listName, "listName");
        } catch (Config.ArgumentIssue argumentIssue) {
            tmpListName = "MyContentList#" + NAMES.size();
            log.warn("Couldn't create ContentList with name \"{}\". New listName is \"{}\"", listName, tmpListName);
        } finally {
            try {
                setDisplayName(tmpListName);
            }  catch (Config.ArgumentIssue e) {
                log.warn("Couldn't set list name \"{}\"", tmpListName);
            }
        }
        if(this.listName != null) NAMES.remove(this.listName);
        this.listName = tmpListName;
        NAMES.add(tmpListName);
    }

    public void setDisplayName(String newName) throws Config.ArgumentIssue {
        this.displayName = ContentType.checkForNullOrEmptyOrIllegalChar(newName, "displayName");
        XMLOperator.renameDisplayNameList(this, displayName);
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

    public synchronized boolean addFromXml(T obj) {
        if(obj == null) {
            log.warn("Null object will not be added to the list \"{}\"!", getListName());
            return false;
        }
        if(contains(obj)) {
            synchronized (AutoSave.NEW_OBJECTS) {
                log.warn("\"{}\" is already on the {} list", get(obj).toString(), getListName());
                AutoSave.NEW_OBJECTS.remove(obj);
                return false;
            }
        }
        list.add(obj);
        log.debug("\"{}\" added to \"{}\"", obj.toString(), getListName());
        return true;
    }

    public void addAll(List<T> objList) {
        objList.forEach(this::add);
    }


    public int size() {
        return list.size();
    }

    public void remove(T obj) {
        XMLOperator.removeFromContentList(this, obj.getId());
        if(list.remove(obj)) {
            log.debug("\"{}\" removed from \"{}\"", obj.toString(), getListName());
            return;
        }
        log.warn("\"{}\" didn't removed from \"{}\"", obj.toString(), getListName());
    }

    public void remove(int index) {
        if(index < list.size()) {
            log.debug("\"{}\" removed from \"{}\"", list.get(index), getListName());
            XMLOperator.removeFromContentList(this, list.remove(index).getId());
        }
        log.warn("Index out of range - provided \"{}\", a list has only \"{}\" positions", index, getListName());
    }

    public void clear() {
        XMLOperator.removeContentList(this);
        list.clear();
        log.debug("\"{}\" cleared", getListName());
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
            if(str == null) continue;
            try {
                targetList.add(getById(Integer.parseInt(str)));
            } catch (NumberFormatException e) {
                log.warn("Couldn't convert \"{}\" to int", str);
            }

        }
        return targetList;
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }



    public T getObjByUrlIfExists(URL link) {
        for(T obj : list) {
            if(obj.getFilmweb().equals(link)) {
                return obj;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        String bold = "\033[1m";
        String end = "\033[0m";
        return "ContentList{" +
                "name='" + bold + listName + end + "', " +
                "size='" + bold + list.size() + end + "'" +
                '}';
    }

    public static void clearNames() {
        NAMES.clear();
    }

}
