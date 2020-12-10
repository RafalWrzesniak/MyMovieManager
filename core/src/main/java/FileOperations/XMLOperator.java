package FileOperations;

import Configuration.Config;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import MoviesAndActors.ContentType;
import MoviesAndActors.Movie;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class XMLOperator {

//    == public static methods ==

    public static <E extends ContentType<E>> void saveContentToXML(E content) {
        if(!AutoSave.NEW_OBJECTS.contains(content)) return;
        Document doc = createDoc();
        assert doc != null;
        createXmlStructure(content, doc);
        File contentDir = IO.createContentDirectory(content);
        File targetFile = new File(
                contentDir.toString()
                .concat("\\")
                .concat(content.getReprName().replaceAll("[]\\[*./:;|,\"]", ""))
                .concat(".xml"));
        makeSimpleSave(doc, targetFile);
        log.debug("Content \"{}\" properly saved in \"{}\"", content.toString(), targetFile);
    }

    public static <E extends ContentType<E>> void createListFile(ContentList<E> list) {
        File savedFile;
        if(list == null || list.size() == 0) throw new IllegalArgumentException("List argument cannot be null or empty!");
        String type;
        if(list.get(0) instanceof Actor) {
            savedFile = Paths.get(Config.getSAVE_PATH_ACTOR().toString(), list.getListName().concat(".xml")).toFile();
            type = Actor.class.getSimpleName();
        }
        else if(list.get(0) instanceof Movie) {
            savedFile = Paths.get(Config.getSAVE_PATH_MOVIE().toString(), list.getListName().concat(".xml")).toFile();
            type = Movie.class.getSimpleName();
        }
        else throw new IllegalArgumentException("Wrong list type!");
        if(savedFile.exists()) return;
        Document doc = createDoc();
        if(doc == null) return;
        Element rootElement = doc.createElement(ContentList.class.getSimpleName());
        doc.appendChild(rootElement);

        rootElement.appendChild(doc.createTextNode("\n\t"));
        Element listName = doc.createElement("listName");
        listName.appendChild(doc.createTextNode(list.getListName()));
        rootElement.appendChild(listName);

        rootElement.appendChild(doc.createTextNode("\n\t"));
        Element displayName = doc.createElement("displayName");
        displayName.appendChild(doc.createTextNode(list.getDisplayName()));
        rootElement.appendChild(displayName);

        rootElement.appendChild(doc.createTextNode("\n\t"));
        Element typeElement = doc.createElement("type");
        typeElement.appendChild(doc.createTextNode(type));
        rootElement.appendChild(typeElement);

        rootElement.appendChild(doc.createTextNode("\n"));
        makeSimpleSave(doc, savedFile);
        System.out.println(list.getListName());
        System.out.println(list.getDisplayName());
        log.info("ContentList \"{}\" properly created and saved in \"{}\"", list, savedFile);
    }

    public static <E extends ContentType<E>> void updateSavedContentListWith(ContentList<E> list, E content) {
        File savedFile;
        if(list == null || list.size() == 0) return;
        if(list.get(0) instanceof Actor) savedFile = Paths.get(Config.getSAVE_PATH_ACTOR().toString(), list.getListName().concat(".xml")).toFile();
        else if(list.get(0) instanceof Movie) savedFile = Paths.get(Config.getSAVE_PATH_MOVIE().toString(), list.getListName().concat(".xml")).toFile();
        else return;

        Document doc = createDocToRead(savedFile);
        if(doc == null) return;
        Element rootElement = doc.getDocumentElement();
        if(rootElement.getTextContent().contains(String.valueOf(content.getId()))) {
            log.warn("Content \"{}\" is already saved in file \"{}\"", content, list);
            return;
        }
        rootElement.appendChild(doc.createTextNode("\t"));
        Element element = doc.createElement(content.getClass().getSimpleName().toLowerCase());
        element.appendChild(doc.createTextNode(String.valueOf(content.getId())));
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n"));
        makeSimpleSave(doc, savedFile);
    }

    public static <E extends ContentType<E>> void removeFromContentList(ContentList<E> list, int idToRemove) {
        File savedFile;
        if(list == null || list.size() == 0) return;
        String tagName;
        if(list.get(0) instanceof Actor) {
            savedFile = Paths.get(Config.getSAVE_PATH_ACTOR().toString(), list.getListName().concat(".xml")).toFile();
            tagName = Actor.class.getSimpleName().toLowerCase();
        }
        else if(list.get(0) instanceof Movie) {
            savedFile = Paths.get(Config.getSAVE_PATH_MOVIE().toString(), list.getListName().concat(".xml")).toFile();
            tagName = Movie.class.getSimpleName().toLowerCase();
        }
        else return;

        Document doc = createDocToRead(savedFile);
        if(doc == null) return;
        Element rootElement = doc.getDocumentElement();
        NodeList nodeList = rootElement.getElementsByTagName(tagName);
        for(int i = 0; i < nodeList.getLength(); i++) {
            if(nodeList.item(i).getTextContent().equals(String.valueOf(idToRemove))) {
                rootElement.removeChild(nodeList.item(i));
                log.debug("Id \"{}\" successfully removed from \"{}\"", idToRemove, list.getListName());
                makeSimpleSave(doc, savedFile);
                return;
            }
        }
        log.warn("Failed to remove id \"{}\" from \"{}\"", idToRemove, list.getListName());
    }


    public static <E extends ContentType<E>> void removeContentList(ContentList<E> list) {
        File savedFile;
        if(list == null || list.size() == 0) return;
        if(list.get(0) instanceof Actor) savedFile = Paths.get(Config.getSAVE_PATH_ACTOR().toString(), list.getListName().concat(".xml")).toFile();
        else if(list.get(0) instanceof Movie) savedFile = Paths.get(Config.getSAVE_PATH_MOVIE().toString(), list.getListName().concat(".xml")).toFile();
        else return;
        log.info("Attempt to remove list \"{}\" ends with status: \"{}\"", savedFile.getName(), savedFile.delete());
    }

    public static <E extends ContentType<E>> void renameDisplayNameList(ContentList<E> list, String newName) {
        File savedFile;
        if(list == null || list.size() == 0) return;
        if(list.get(0) instanceof Actor) savedFile = Paths.get(Config.getSAVE_PATH_ACTOR().toString(), list.getListName().concat(".xml")).toFile();
        else if(list.get(0) instanceof Movie) savedFile = Paths.get(Config.getSAVE_PATH_MOVIE().toString(), list.getListName().concat(".xml")).toFile();
        else return;

        Document doc = createDocToRead(savedFile);
        if(doc == null) return;
        Element rootElement = doc.getDocumentElement();
        Node listName = rootElement.getElementsByTagName("displayName").item(0);
        listName.setTextContent(newName);
        makeSimpleSave(doc, savedFile);
        log.info("Display name of ContentList \"{}\" is now \"{}\"", list, newName);
    }

    public static void makeSimpleSave(Document doc, File toFile) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(toFile);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public static Document createDoc() {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            return dBuilder.newDocument();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Document createDocToRead(File inputFile) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return null;
    }

//    == package-private static methods ==

    static <E extends ContentType<E>> void createXmlStructure(E content, Document doc) {
        String tagName;
        if(content instanceof Actor) tagName = Actor.class.getSimpleName();
        else if(content instanceof Movie) tagName = Movie.class.getSimpleName();
        else return;

        Element rootElement = doc.createElement(tagName);
        doc.appendChild(rootElement);
        Map<String, String> contentDetails = content.getAllFieldsAsStrings();
        for(String detail : contentDetails.keySet()) {
            if(contentDetails.get(detail) == null) continue;
            String[] listCheckSemicolon = contentDetails.get(detail).split(";");
            Element element;
            if(listCheckSemicolon.length == 1) {
                rootElement.appendChild(doc.createTextNode("\n\t"));
                element = doc.createElement(detail);
                element.appendChild(doc.createTextNode(contentDetails.get(detail)));
            } else {
                rootElement.appendChild(doc.createTextNode("\n\t"));
                element =  doc.createElement(detail.substring(0,1).toUpperCase().concat(detail.substring(1)));
                for(int i = 0; i < listCheckSemicolon.length; i++) {
                    element.appendChild(doc.createTextNode("\n\t\t"));
                    Element childElement = doc.createElement(detail);
                    childElement.appendChild(doc.createTextNode(Arrays.asList(listCheckSemicolon).get(i)));
                    element.appendChild(childElement);
                }
                element.appendChild(doc.createTextNode("\n\t"));
            }
            rootElement.appendChild(element);
        }
        rootElement.appendChild(doc.createTextNode("\n"));
    }

    static Element createRootElementFromXml(File inputFile) {
        if(inputFile == null) {
            log.warn("Null as input for XMLOperator passed");
            return null;
        } else if(!inputFile.toString().endsWith(".xml")) {
            log.warn("Wrong input file extension for XMLOperator passed. Should be: .xml, found: \"{}\"", inputFile.toString());
            return null;
        }
        Document doc = createDocToRead(inputFile);
        if(doc == null) return null;
        return doc.getDocumentElement();
    }


//    == public static inner classes ==

    public static class ReadAllDataFromFiles extends Thread {
        private List<ContentList<Actor>> allActorsLists;
        private List<ContentList<Movie>> allMoviesLists;
        private ContentList<Actor> allActors;
        private ContentList<Movie> allMovies;


        @Override
        public void run() {
            setName("ReadAll");
            allActorsLists = createAllActorsContentListsFromXml();
            allMoviesLists = createAllMoviesContentListsFromXml(allActorsLists);

            allActors = ContentList.getContentListFromListByName(allActorsLists, ContentList.ALL_ACTORS_DEFAULT);
            if(allActors == null) {
                allActors = new ContentList<>(ContentList.ALL_ACTORS_DEFAULT);
            }
            allMovies = ContentList.getContentListFromListByName(allMoviesLists, ContentList.ALL_MOVIES_DEFAULT);
            if(allMovies == null) {
                allMovies = new ContentList<>(ContentList.ALL_MOVIES_DEFAULT);
            }

            log.info("All data read from files");
        }


        public ContentList<Actor> getAllActors() {
            return allActors;
        }

        public ContentList<Movie> getAllMovies() {
            return allMovies;
        }

        public List<ContentList<Actor>> getAllActorsLists() {
            return allActorsLists;
        }

        public List<ContentList<Movie>> getAllMoviesLists() {
            return allMoviesLists;
        }


        private static List<ContentList<Actor>> createAllActorsContentListsFromXml() {
            List<ContentList<Actor>> allActorLists = new ArrayList<>();
            ContentList<Actor> defaultAllActors = createDefaultActorContentList();
            allActorLists.add(defaultAllActors);
            for(File file : Objects.requireNonNull(IO.listDirectory(Config.getSAVE_PATH_ACTOR().toFile()))) {
                if(file.getName().endsWith(".xml") && !file.getName().matches(ContentList.ALL_ACTORS_DEFAULT.concat(".xml"))) {
                    allActorLists.add(createActorContentList(file, defaultAllActors));
                    log.info("ContentList<Actor> \"{}\" successfully read from file \"{}\"", file.getName().replaceAll("\\.xml$", "") ,file);
                }
            }
            return allActorLists;
        }

        private static List<ContentList<Movie>> createAllMoviesContentListsFromXml(List<ContentList<Actor>> allActorsContentLists) {
            List<ContentList<Movie>> allMovieLists = new ArrayList<>();
            ContentList<Movie> defaultAllMovies = createDefaultMovieContentList(allActorsContentLists);
            allMovieLists.add(defaultAllMovies);
            for(File file : Objects.requireNonNull(IO.listDirectory(Config.getSAVE_PATH_MOVIE().toFile()))) {
                if(file.toString().endsWith(".xml") && !file.getName().matches(ContentList.ALL_MOVIES_DEFAULT.concat(".xml")) && !file.getName().matches(ContentList.MOVIES_TO_WATCH.concat(".xml"))) {
                    allMovieLists.add(createMovieContentList(file, defaultAllMovies));
                    log.info("ContentList<Movie> \"{}\" successfully read from file \"{}\"", file.getName().replaceAll("\\.xml$", "") ,file);
                }
            }
            return allMovieLists;
        }

        @SneakyThrows
        private static ContentList<Actor> createActorContentList(File inputFile, ContentList<Actor> defaultAllActors) {
            Element root = createRootElementFromXml(inputFile);
            if(root == null) {
                log.warn("Couldn't create ContentList from file \"{}\" - internal XML file issue", inputFile);
                return null;
            }

            NodeList nodes = root.getElementsByTagName(Actor.class.getSimpleName().toLowerCase());
            ContentList<Actor> contentList = new ContentList<>(root.getElementsByTagName("listName").item(0).getTextContent());
            contentList.setDisplayName(root.getElementsByTagName("displayName").item(0).getTextContent());

            for(int i = 0; i < nodes.getLength(); i++) {
                String id = nodes.item(i).getTextContent();
                assert defaultAllActors != null;
                contentList.addFromXml(defaultAllActors.getById(Integer.parseInt(id)));
            }
            return contentList;
        }

        @SneakyThrows
        private static ContentList<Movie> createMovieContentList(File inputFile, ContentList<Movie> defaultAllMovies) {
            Element root = createRootElementFromXml(inputFile);
            if(root == null) {
                log.warn("Couldn't create ContentList from file \"{}\" - internal XML file issue", inputFile);
                return null;
            }

            NodeList nodes = root.getElementsByTagName(Movie.class.getSimpleName().toLowerCase());
            ContentList<Movie> contentList = new ContentList<>(root.getElementsByTagName("listName").item(0).getTextContent());
            contentList.setDisplayName(root.getElementsByTagName("displayName").item(0).getTextContent());

            for(int i = 0; i < nodes.getLength(); i++) {
                String id = nodes.item(i).getTextContent();
                if(defaultAllMovies == null) return null;
                contentList.addFromXml(defaultAllMovies.getById(Integer.parseInt(id)));
            }
            return contentList;
        }


        @SneakyThrows
        private static ContentList<Actor> createDefaultActorContentList() {
            File inputFile = Paths.get(Config.getSAVE_PATH_ACTOR().toString(), ContentList.ALL_ACTORS_DEFAULT.concat(".xml")).toFile();
            Element root = createRootElementFromXml(inputFile);
            if(root == null) {
                log.warn("Couldn't create ContentList from file \"{}\" - internal XML file issue", inputFile);
                return null;
            }
            NodeList nodes = root.getElementsByTagName(Actor.class.getSimpleName().toLowerCase());
            ContentList<Actor> contentList = new ContentList<>(root.getElementsByTagName("listName").item(0).getTextContent());
            contentList.setDisplayName(root.getElementsByTagName("displayName").item(0).getTextContent());
            List<String> actorsIds = new ArrayList<>();
            for(int i = 0; i < nodes.getLength(); i++) {
                actorsIds.add(nodes.item(i).getTextContent());
            }
            List<Thread> actorThreads = new ArrayList<>();
            int numberOfThreads = actorsIds.size()/50 + 1;
            for(int i = 0; i < numberOfThreads; i++) {
                Thread createActors = new Thread(() -> {
                    while(actorsIds.size() != 0) {
                        String id;
                        synchronized (actorsIds) {
                            id = actorsIds.get(0);
                            actorsIds.remove(0);
                        }
                        contentList.addFromXml(
                                createActorFromXml(
                                        Paths.get(Config.getSAVE_PATH_ACTOR().toString(), "actor".concat(id)).toFile()));
                    }
                });
                createActors.setName("newActor" + i);
                actorThreads.add(createActors);
                createActors.start();

                if(i == numberOfThreads-1) {
                    boolean iAmStillWorking = true;
                    while(iAmStillWorking) {
                        actorThreads.removeIf(thread -> thread.getState().equals(Thread.State.TERMINATED));
                        if(actorThreads.size() == 0) {
                            iAmStillWorking = false;
                        }
                    }
                }
            }

            return contentList;
        }

        @SneakyThrows
        private static ContentList<Movie> createDefaultMovieContentList(List<ContentList<Actor>> allActorsContentLists) {
            File inputFile = Paths.get(Config.getSAVE_PATH_MOVIE().toString(), ContentList.ALL_MOVIES_DEFAULT.concat(".xml")).toFile();
            Element root = createRootElementFromXml(inputFile);
            if(root == null) {
                log.warn("Couldn't create ContentList from file \"{}\" - internal XML file issue", inputFile);
                return null;
            }

            NodeList nodes = root.getElementsByTagName(Movie.class.getSimpleName().toLowerCase());
            ContentList<Actor> defaultActors = null;
            for(ContentList<Actor> contentList : allActorsContentLists) {
                if(contentList.getListName().equals(ContentList.ALL_ACTORS_DEFAULT)) {
                    defaultActors = contentList;
                }
            }
            if(defaultActors == null) {
                log.warn("Couldn't create ContentList from file \"{}\" - no correct default actor list", inputFile);
                return null;
            }
            ContentList<Movie> contentList = new ContentList<>(root.getElementsByTagName("listName").item(0).getTextContent());
            contentList.setDisplayName(root.getElementsByTagName("displayName").item(0).getTextContent());
            List<String> moviesIds = new ArrayList<>();
            for(int i = 0; i < nodes.getLength(); i++) {
                moviesIds.add(nodes.item(i).getTextContent());
            }
            List<Thread> movieThreads = new ArrayList<>();
            int numberOfThreads = moviesIds.size()/10 + 1;
            for(int i = 0; i < numberOfThreads; i++) {
                ContentList<Actor> finalDefaultActors = defaultActors;
                Thread createMovies = new Thread(() -> {
                    while(moviesIds.size() != 0) {
                        String id;
                        synchronized (moviesIds) {
                            id = moviesIds.get(0);
                            moviesIds.remove(0);
                        }
                        contentList.addFromXml(
                                createMovieFromXml(
                                        Paths.get(Config.getSAVE_PATH_MOVIE().toString(), "movie".concat(id)).toFile(),
                                        finalDefaultActors));
                    }
                });
                createMovies.setName("newMovie" + i);
                movieThreads.add(createMovies);
                createMovies.start();

                if(i == numberOfThreads-1) {
                    boolean iAmStillWorking = true;
                    while(iAmStillWorking) {
                        movieThreads.removeIf(thread -> thread.getState().equals(Thread.State.TERMINATED));
                        if(movieThreads.size() == 0) {
                            iAmStillWorking = false;
                        }
                    }
                }
            }
            return contentList;
        }

        private static Actor createActorFromXml(File inputDir) {
            File inputFile = IO.getXmlFileFromDir(inputDir);
            Element root = createRootElementFromXml(inputFile);
            if(root == null) {
                log.warn("Couldn't create actor from file \"{}\" - internal XML file issue", inputFile);
                return null;
            } else if(!root.getTagName().equals(Actor.class.getSimpleName())) {
                log.warn("Couldn't create actor from file \"{}\" - different structure type", inputFile);
                return null;
            }

            Map<String, String> map = new HashMap<>();
            for(String fieldName : Actor.FIELD_NAMES) {
                NodeList element = root.getElementsByTagName(fieldName);
                if(element.getLength() == 1) {
                    String value = element.item(0).getChildNodes().item(0).getNodeValue();
                    if(value != null) map.put(fieldName, value);
                }
            }
            log.info("New actor created successfully from file \"{}\"", inputFile);
            return new Actor(map);
        }

        private static Movie createMovieFromXml(File inputDir, ContentList<Actor> allActors) {
            File inputFile = IO.getXmlFileFromDir(inputDir);
            Element root = createRootElementFromXml(inputFile);
            if(root == null) {
                log.warn("Couldn't create actor from file \"{}\" - internal XML file issue", inputFile);
                return null;
            } else if(!root.getTagName().equals(Movie.class.getSimpleName())) {
                log.warn("Couldn't create actor from file \"{}\" - different structure type", inputFile);
                return null;
            }

            Map<String, List<String>> map = new LinkedHashMap<>();
            for(String fieldName : Movie.FIELD_NAMES) {
                NodeList element = root.getElementsByTagName(fieldName);
                List<String> param = new ArrayList<>();
                for(int i = 0; i < element.getLength(); i++) {
                    String value = element.item(i).getChildNodes().item(0).getNodeValue();
                    if(value != null) param.add(value);
                }
                map.put(fieldName, param);
            }
            log.info("New movie created successfully from file \"{}\"", inputFile);
            return new Movie(map, allActors);
        }

    }


}