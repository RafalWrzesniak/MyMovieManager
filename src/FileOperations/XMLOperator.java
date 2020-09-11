package FileOperations;

import Internet.Connection;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import MoviesAndActors.ContentType;
import MoviesAndActors.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public final class XMLOperator {

    private static String SAVE_PATH;
    private static String SAVE_PATH_MOVIE;
    private static String SAVE_PATH_ACTOR;
    private static final Logger logger = LoggerFactory.getLogger(XMLOperator.class.getName());

    private XMLOperator() {}

    static {
        File cfg = new File("resources\\config.cfg");
        if(cfg.exists() && !cfg.isDirectory()) {
            Document doc = createDocToRead(cfg);
            if(doc != null) {
                Element root = doc.getDocumentElement();
                NodeList element = root.getElementsByTagName("SAVE_PATH");
                SAVE_PATH = element.item(0).getChildNodes().item(0).getTextContent();
            }
        } else {
            Document doc = createDoc();
            if(doc != null) {
                Element rootElement = doc.createElement("config");
                doc.appendChild(rootElement);
                Element element = doc.createElement("SAVE_PATH");
                rootElement.appendChild(element);
                makeSimpleSave(doc, cfg);
            }
            updateParamInCfg("SAVE_PATH", System.getProperty("user.dir").concat("\\savedData"));
            boolean made1 = new File(System.getProperty("user.dir").concat("\\savedData")).mkdir();
            SAVE_PATH = System.getProperty("user.dir").concat("\\savedData");
        }
        boolean made2 = new File(SAVE_PATH).mkdirs();
        updateRelativePaths();

    }

    public static void changeSavePath(File newDirectory) {
        if(newDirectory != null && newDirectory.mkdir()) {
            try {
                Files.move(Paths.get(SAVE_PATH), newDirectory.toPath(), REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            updateParamInCfg("SAVE_PATH", newDirectory.toString());
            SAVE_PATH = newDirectory.getPath();
            updateRelativePaths();
        } else {
            logger.warn("Couldn't change SAVE_PATH to \"{}\". SAVE_PATH is still \"{}\"", newDirectory, SAVE_PATH);
        }
    }

    public static String getSavePath() {
        return SAVE_PATH;
    }
    public static String getSavePathActor() {
        return SAVE_PATH_ACTOR;
    }
    public static String getSavePathMovie() {
        return SAVE_PATH_MOVIE;
    }

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
        logger.debug("Content \"{}\" properly saved in \"{}\"", content.toString(), targetFile);
    }





    private static void updateRelativePaths() {
        boolean made1 = new File(SAVE_PATH + "\\" + Movie.class.getSimpleName()).mkdir();
        boolean made2 = new File(SAVE_PATH + "\\" + Actor.class.getSimpleName()).mkdir();
        SAVE_PATH_MOVIE = SAVE_PATH + "\\" + Movie.class.getSimpleName();
        SAVE_PATH_ACTOR = SAVE_PATH + "\\" + Actor.class.getSimpleName();

    }

    private static void updateParamInCfg(String parameter, String value) {
        File cfg = new File("resources\\config.cfg");
        Document doc = createDocToRead(cfg);
        if(doc == null) return;
        NodeList savePath = doc.getElementsByTagName(parameter);
        savePath.item(0).setTextContent(value);
        makeSimpleSave(doc, cfg);
        logger.info("Parameter \"{}\" changed to \"{}\" in config.cfg", parameter, value);
    }

    private static void makeSimpleSave(Document doc, File toFile) {
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

    private static Document createDoc() {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            return dBuilder.newDocument();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Document createDocToRead(File inputFile) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return null;
    }


    private static <E extends ContentType<E>> void createXmlStructure(E content, Document doc) {
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

    private static Element createRootElementFromXml(File inputFile) {
        if(inputFile == null) {
            logger.warn("Null as input for XMLOperator passed");
//            throw new IllegalArgumentException("Input file cannot be null!");
            return null;
        } else if(!inputFile.toString().endsWith(".xml")) {
            logger.warn("Wrong input file extension for XMLOperator passed. Should be: .xml, found: \"{}\"", inputFile.toString());
//            throw new IllegalArgumentException("Wrong input file extension! Should be: .xml, found: " + inputFile.toString());
            return null;
        }
        Document doc = createDocToRead(inputFile);
        if(doc == null) return null;
        return doc.getDocumentElement();
    }


    public static <E extends ContentType<E>> boolean createListFile(ContentList<E> list) {
        File savedFile;
        if(list == null || list.size() == 0) throw new IllegalArgumentException("List argument cannot be null or empty!");
        String type;
        if(list.get(0) instanceof Actor) {
            savedFile = new File(SAVE_PATH_ACTOR.concat("\\").concat(list.getListName()).concat(".xml"));
            type = Actor.class.getSimpleName();
        }
        else if(list.get(0) instanceof Movie) {
            savedFile = new File(SAVE_PATH_MOVIE.concat("\\").concat(list.getListName()).concat(".xml"));
            type = Movie.class.getSimpleName();
        }
        else throw new IllegalArgumentException("Wrong list type!");
        if(savedFile.exists()) return false;
        Document doc = createDoc();
        if(doc == null) return false;
        Element rootElement = doc.createElement(ContentList.class.getSimpleName());
        doc.appendChild(rootElement);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        Element listName = doc.createElement("listName");
        listName.appendChild(doc.createTextNode(list.getListName()));
        rootElement.appendChild(listName);
        rootElement.appendChild(doc.createTextNode("\n\t"));
        Element typeElement = doc.createElement("type");
        typeElement.appendChild(doc.createTextNode(type));
        rootElement.appendChild(typeElement);
        rootElement.appendChild(doc.createTextNode("\n"));
        makeSimpleSave(doc, savedFile);
        return true;
    }

    public static <E extends ContentType<E>> void updateSavedContentListWith(ContentList<E> list, E content) {
        File savedFile;
        if(list == null || list.size() == 0) return;
        if(list.get(0) instanceof Actor) savedFile = new File(SAVE_PATH_ACTOR.concat("\\").concat(list.getListName()).concat(".xml"));
        else if(list.get(0) instanceof Movie) savedFile = new File(SAVE_PATH_MOVIE.concat("\\").concat(list.getListName()).concat(".xml"));
        else return;

        Document doc = createDocToRead(savedFile);
        if(doc == null) return;
        Element rootElement = doc.getDocumentElement();
        rootElement.appendChild(doc.createTextNode("\t"));
        Element element = doc.createElement(content.getClass().getSimpleName().toLowerCase());
        element.appendChild(doc.createTextNode(String.valueOf(content.getId())));
        rootElement.appendChild(element);
        rootElement.appendChild(doc.createTextNode("\n"));
        makeSimpleSave(doc, savedFile);
    }







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
            allMovies = ContentList.getContentListFromListByName(allMoviesLists, ContentList.ALL_MOVIES_DEFAULT);
            logger.info("All data read from files");
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
            for(File file : Objects.requireNonNull(IO.listDirectory(new File(SAVE_PATH_ACTOR)))) {
                if(file.getName().endsWith(".xml") && !file.getName().matches(ContentList.ALL_ACTORS_DEFAULT.concat(".xml"))) {
                    allActorLists.add(createActorContentList(file, defaultAllActors));
                    logger.info("ContentList<Actor> \"{}\" successfully read from file \"{}\"", file.getName().replaceAll("\\.xml$", "") ,file);
                }
            }
            return allActorLists;
        }

        private static List<ContentList<Movie>> createAllMoviesContentListsFromXml(List<ContentList<Actor>> allActorsContentLists) {
            List<ContentList<Movie>> allMovieLists = new ArrayList<>();
            ContentList<Movie> defaultAllMovies = createDefaultMovieContentList(allActorsContentLists);
            allMovieLists.add(defaultAllMovies);
            for(File file : Objects.requireNonNull(IO.listDirectory(new File(SAVE_PATH_MOVIE)))) {
                if(file.toString().endsWith(".xml") && !file.getName().matches(ContentList.ALL_MOVIES_DEFAULT.concat(".xml"))) {
                    allMovieLists.add(createMovieContentList(file, defaultAllMovies));
                    logger.info("ContentList<Movie> \"{}\" successfully read from file \"{}\"", file.getName().replaceAll("\\.xml$", "") ,file);
                }
            }
            return allMovieLists;
        }

        private static ContentList<Actor> createActorContentList(File inputFile, ContentList<Actor> defaultAllActors) {
            Element root = createRootElementFromXml(inputFile);
            if(root == null) {
                logger.warn("Couldn't create ContentList from file \"{}\" - internal XML file issue", inputFile);
                return null;
            }

            NodeList nodes = root.getElementsByTagName(Actor.class.getSimpleName().toLowerCase());
            ContentList<Actor> contentList = new ContentList<>(root.getElementsByTagName("listName").item(0).getTextContent());

            for(int i = 0; i < nodes.getLength(); i++) {
                String id = nodes.item(i).getTextContent();
                assert defaultAllActors != null;
                contentList.addFromXml(defaultAllActors.getById(Integer.parseInt(id)));
            }
            return contentList;
        }

        private static ContentList<Movie> createMovieContentList(File inputFile, ContentList<Movie> defaultAllMovies) {
            Element root = createRootElementFromXml(inputFile);
            if(root == null) {
                logger.warn("Couldn't create ContentList from file \"{}\" - internal XML file issue", inputFile);
                return null;
            }

            NodeList nodes = root.getElementsByTagName(Movie.class.getSimpleName().toLowerCase());
            ContentList<Movie> contentList = new ContentList<>(root.getElementsByTagName("listName").item(0).getTextContent());

            for(int i = 0; i < nodes.getLength(); i++) {
                String id = nodes.item(i).getTextContent();
                if(defaultAllMovies == null) return null;
                contentList.addFromXml(defaultAllMovies.getById(Integer.parseInt(id)));
            }
            return contentList;
        }


        private static ContentList<Actor> createDefaultActorContentList() {
            File inputFile = new File(SAVE_PATH_ACTOR.concat("\\").concat(ContentList.ALL_ACTORS_DEFAULT.concat(".xml")));
            Element root = createRootElementFromXml(inputFile);
            if(root == null) {
                logger.warn("Couldn't create ContentList from file \"{}\" - internal XML file issue", inputFile);
                return null;
            }
            NodeList nodes = root.getElementsByTagName(Actor.class.getSimpleName().toLowerCase());
            ContentList<Actor> contentList = new ContentList<>(root.getElementsByTagName("listName").item(0).getTextContent());
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
                        contentList.addFromXml(createActorFromXml(new File(SAVE_PATH_ACTOR.concat("\\").concat("actor").concat(id))));
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

        private static ContentList<Movie> createDefaultMovieContentList(List<ContentList<Actor>> allActorsContentLists) {
            File inputFile = new File(SAVE_PATH_MOVIE.concat("\\").concat(ContentList.ALL_MOVIES_DEFAULT.concat(".xml")));
            Element root = createRootElementFromXml(inputFile);
            if(root == null) {
                logger.warn("Couldn't create ContentList from file \"{}\" - internal XML file issue", inputFile);
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
                logger.warn("Couldn't create ContentList from file \"{}\" - no correct default actor list", inputFile);
                return null;
            }
            ContentList<Movie> contentList = new ContentList<>(root.getElementsByTagName("listName").item(0).getTextContent());
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
                        contentList.addFromXml(createMovieFromXml(new File(SAVE_PATH_MOVIE.concat("\\").concat("movie").concat(id)), finalDefaultActors));
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
                logger.warn("Couldn't create actor from file \"{}\" - internal XML file issue", inputFile);
                return null;
            } else if(!root.getTagName().equals(Actor.class.getSimpleName())) {
                logger.warn("Couldn't create actor from file \"{}\" - different structure type", inputFile);
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
            logger.info("New actor created successfully from file \"{}\"", inputFile);
            Actor actor = new Actor(map);
            AutoSave.NEW_OBJECTS.remove(actor);
            return actor;
        }

        private static Movie createMovieFromXml(File inputDir, ContentList<Actor> allActors) {
            File inputFile = IO.getXmlFileFromDir(inputDir);
            Element root = createRootElementFromXml(inputFile);
            if(root == null) {
                logger.warn("Couldn't create actor from file \"{}\" - internal XML file issue", inputFile);
                return null;
            } else if(!root.getTagName().equals(Movie.class.getSimpleName())) {
                logger.warn("Couldn't create actor from file \"{}\" - different structure type", inputFile);
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
            logger.info("New movie created successfully from file \"{}\"", inputFile);
            Movie movie = new Movie(map, allActors);
            AutoSave.NEW_OBJECTS.remove(movie);
            Function<List<Actor>, Boolean> removeActorsFromListToSave = list -> {
                for(Actor actor : list) {
                    AutoSave.NEW_OBJECTS.remove(actor);
                }
                return true;
            };

            removeActorsFromListToSave.apply(movie.getCast());
            removeActorsFromListToSave.apply(movie.getDirectors());
            removeActorsFromListToSave.apply(movie.getWriters());
            return movie;
        }

    }


    public static class ExportAll extends Thread {

        private File exportFile;

        public ExportAll(File exportFile) {
            this.exportFile = exportFile;
        }

        public ExportAll() {
        }

        @Override
        public void run() {
            setName("Export");
            if(exportFile != null) {
                exportAll(exportFile);
            } else {
                exportAll(new File(
                        System.getProperty("user.dir")
                        .concat("\\MyMovieManager_exported_data_")
                        .concat(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                        .replaceAll("\\..*$", "")
                        .replaceAll(":", "_")
                        .concat(".xml"))));
            }
        }


        private static void exportAll(File exportFile) {
            Document doc = createDoc();
            assert doc != null;
            Element rootElement = doc.createElement("exported");
            doc.appendChild(rootElement);

            Function<String, Boolean> copyNodesFromPath = path -> {
                File xml;
                for(File dir : Objects.requireNonNull(IO.listDirectory(new File(path)))) {
                    if(dir.isDirectory()) {
                        xml = IO.getXmlFileFromDir(dir);
                    } else if(dir.toString().endsWith(".xml")) {
                        xml = dir;
                    } else return false;
                    Element localRoot = createRootElementFromXml(xml);
                    assert localRoot != null;
                    rootElement.appendChild(doc.createTextNode("\n"));
                    rootElement.appendChild(doc.adoptNode(localRoot.cloneNode(true)));
                }
                return true;
            };
            copyNodesFromPath.apply(SAVE_PATH_ACTOR);
            copyNodesFromPath.apply(SAVE_PATH_MOVIE);
            rootElement.appendChild(doc.createTextNode("\n"));
            makeSimpleSave(doc, exportFile);
            logger.info("All data successfully exported to file \"{}\"", exportFile);
        }

    }

    public static class ImportData extends ReadAllDataFromFiles {

        private final File importFile;

        public ImportData(File importFile) {
            this.importFile = importFile;
        }

        @Override
        public void run() {
            setName("ImportData");
            logger.info("Import data started from file \"{}\"", importFile.toString());
            convertImportFileToDirs(importFile);
            super.run();
            setName("ImportData");
            Thread downloadMovieImages = new Thread(() -> {
                for(Movie movie : getAllMovies().getList()) {
                    try {
                        Connection connection = new Connection(new URL(movie.getFilmweb()));
                        Connection.downloadImage(connection.getImageUrl(true), movie.getImagePath());
                    } catch (IOException ignored) { }
                }
            });

            Thread downloadActorImages = new Thread(() -> {
                for(Actor actor : getAllActors().getList()) {
                    try {
                        Connection connection = new Connection(new URL(actor.getFilmweb()));
                        Connection.downloadImage(connection.getImageUrl(false), actor.getImagePath());
                    } catch (IOException ignored) { }
                }
            });

            downloadActorImages.start();
            downloadMovieImages.start();

            try {
                downloadActorImages.join();
                downloadMovieImages.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            logger.info("Import data finished from file \"{}\"", importFile.toString());
        }

        public static void convertImportFileToDirs(File importFile) {
            Element rootElement = createRootElementFromXml(importFile);
            if(rootElement == null || !rootElement.getTagName().equals("exported")) {
                logger.warn("Failed to import file \"{}\" - wrong XML", importFile);
                return;
            }
            Function<String, Boolean> saveAllContentsToFiles = content -> {
                NodeList singleContent = rootElement.getElementsByTagName(content);
                for(int i = 0; i < singleContent.getLength(); i++) {
                    Document doc = createDoc();
                    assert doc != null;
                    doc.appendChild(doc.adoptNode(singleContent.item(i).cloneNode(true)));
                    Element localRoot = doc.getDocumentElement();

                    String reprName = "crashed";
                    File outDir = new File(SAVE_PATH);
                    if(content.equals(Actor.class.getSimpleName())) {
                        String contentId = localRoot.getElementsByTagName(ContentType.ID).item(0).getTextContent();
                        reprName = localRoot.getElementsByTagName(Actor.NAME).item(0).getTextContent().concat("_")
                                .concat(localRoot.getElementsByTagName(Actor.SURNAME).item(0).getTextContent());
                        outDir = new File(SAVE_PATH_ACTOR + "\\actor" + contentId);

                    } else if(content.equals(Movie.class.getSimpleName())) {
                        String contentId = localRoot.getElementsByTagName(ContentType.ID).item(0).getTextContent();
                        reprName = localRoot.getElementsByTagName(Movie.TITLE).item(0).getTextContent();
                        outDir = new File(SAVE_PATH_MOVIE + "\\movie" + contentId);

                    } else if(content.equals(ContentList.class.getSimpleName())) {
                        reprName = localRoot.getElementsByTagName("listName").item(0).getTextContent();
                        String type = localRoot.getElementsByTagName("type").item(0).getTextContent();
                        if(type.equals(Actor.class.getSimpleName())) outDir = new File(SAVE_PATH_ACTOR);
                        else if(type.equals(Movie.class.getSimpleName())) outDir = new File(SAVE_PATH_MOVIE);
                    }

                    boolean made = outDir.mkdir();
                    File targetFile = new File(outDir.toString().concat("\\").concat(reprName).concat(".xml"));
                    makeSimpleSave(doc, targetFile);
                }
                return true;
            };

            if(IO.deleteDirectoryRecursively(new File(SAVE_PATH))) {
                if(new File(SAVE_PATH).mkdir()) updateRelativePaths();
                if(saveAllContentsToFiles.apply(Movie.class.getSimpleName()) &&
                        saveAllContentsToFiles.apply(Actor.class.getSimpleName()) &&
                        saveAllContentsToFiles.apply(ContentList.class.getSimpleName())
                ) {
                    logger.info("Successfully imported data from file \"{}\"", importFile);
                } else {
                    logger.warn("Some data could not be read while import from \"{}\"", importFile);
                }
            }
        }

    }



}






