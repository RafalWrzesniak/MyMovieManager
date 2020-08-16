package FileOperations;

import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import MoviesAndActors.ContentType;
import MoviesAndActors.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.print.Doc;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public final class XMLOperator {

    private static String SAVE_PATH;
    private static String SAVE_PATH_MOVIE;
    private static String SAVE_PATH_ACTOR;
    private static final Logger logger = LoggerFactory.getLogger(XMLOperator.class.getName());
    public static final List<ContentType> OBJECTS_TO_SAVE = new ArrayList<>();

    private XMLOperator(){}

    static {
        new File(System.getProperty("user.dir").concat("\\savedData")).mkdir();
        SAVE_PATH = System.getProperty("user.dir").concat("\\savedData");
        updateRelativePaths();
    }

    private static void updateRelativePaths() {
        new File(SAVE_PATH + "\\" + Movie.class.getSimpleName()).mkdir();
        new File(SAVE_PATH + "\\" + Actor.class.getSimpleName()).mkdir();
        SAVE_PATH_MOVIE = SAVE_PATH + "\\" + Movie.class.getSimpleName();
        SAVE_PATH_ACTOR = SAVE_PATH + "\\" + Actor.class.getSimpleName();
    }

    public static void changeSavePath(File newDirectory) {
        if(newDirectory != null && newDirectory.mkdir()) {
            try {
                Files.move(Paths.get(SAVE_PATH), newDirectory.toPath(), REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            SAVE_PATH = newDirectory.getPath();
            updateRelativePaths();
            logger.info("SAVE_PATH changed to \"{}\"", newDirectory);
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
        if(!XMLOperator.OBJECTS_TO_SAVE.contains(content)) return;
        Document doc = createDoc();
        assert doc != null;
        createXmlStructure(content, doc);
        saveIntoXML(content, doc);
        OBJECTS_TO_SAVE.remove(content);
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
            e.printStackTrace();
        }
        return null;
    }


    private static <E extends ContentType<E>> void saveIntoXML(E content, Document doc) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        File contentDir = createContentDirectory(content);
        if(contentDir == null) {
            logger.warn("Directory for \"{}\" doesn't exist, could't create XML", content.toString());
            return;
        }
        File targetFile = new File(contentDir.toString().concat("\\").concat(content.getReprName()).concat(".xml"));
        try {
            transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(targetFile);
            transformer.transform(source, result);
            logger.debug("Content \"{}\" properly saved in \"{}\"", content.toString(), targetFile);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private static <E extends ContentType<E>> void createXmlStructure(E content, Document doc) {
        String tagName;
        if(content instanceof Actor) tagName = Actor.class.getSimpleName();
        else if(content instanceof Movie) tagName = Movie.class.getSimpleName();
        else return;

        Element rootElement = doc.createElement(tagName);
        doc.appendChild(rootElement);
        Attr attrType = doc.createAttribute(ContentType.ID);
        attrType.setValue(String.valueOf(content.getId()));
        rootElement.setAttributeNode(attrType);

        Map<String, String> contentDetails = content.getAllFieldsAsStrings();
        for(String detail : contentDetails.keySet()) {
            Element element = doc.createElement(detail);
            if(contentDetails.get(detail) == null) continue;
            String[] listCheckSemicolon = contentDetails.get(detail).split(";");
            if(listCheckSemicolon.length == 1) {
                element.appendChild(doc.createTextNode(contentDetails.get(detail)));
            } else {
                for(int i = 0; i < listCheckSemicolon.length; i++) {
                    Element childElement = doc.createElement(detail);
                    childElement.appendChild(doc.createTextNode(Arrays.asList(listCheckSemicolon).get(i) + "\t"));
                    element.appendChild(childElement);
                }
            }
            rootElement.appendChild(element);
        }
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

    private static File getFileFromDir(File inputDir) {
        if(inputDir == null || !inputDir.isDirectory()) {
            logger.warn("Couldn't create actor from directory \"{}\" - no such directory ir is not a directory", inputDir);
            return null;
        }
        List<File> fileList = IO.listDirectory(inputDir);
        if(fileList == null ||fileList.size() == 0) {
            logger.warn("Couldn't create actor from directory \"{}\" - directory is empty or does not exist", inputDir);
            return null;
        }
        for(File file : fileList) {
            if(file.toString().endsWith(".xml")) {
                return file;
            }
        }
        logger.warn("There is no .xml file in directory \"{}\". Content wasn't created", inputDir);
        return null;

    }


    public static Actor createActorFromXml(File inputDir) {

        File inputFile = getFileFromDir(inputDir);
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

        Actor actor = new Actor(map.get(Actor.NAME), map.get(Actor.SURNAME), map.get(Actor.NATIONALITY),
                map.get(Actor.BIRTHDAY), map.get(Actor.IMAGE_PATH), map.get(Actor.ID));
        logger.info("New actor \"{}\" created successfully from file \"{}\"", actor, inputFile);
        XMLOperator.OBJECTS_TO_SAVE.remove(actor);
        return actor;
    }

    public static Movie createMovieFromXml(File inputDir, ContentList<Actor> allActors) {
        File inputFile = getFileFromDir(inputDir);
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
        Movie movie = new Movie(map, allActors);
        logger.info("New movie \"{}\" created successfully from file \"{}\"", movie, inputFile);
        XMLOperator.OBJECTS_TO_SAVE.remove(movie);
        Function<List<Actor>, Boolean> removeActorsFromListToSave = list -> {
            for(Actor actor : list) {
                XMLOperator.OBJECTS_TO_SAVE.remove(actor);
            }
            return true;
        };
        removeActorsFromListToSave.apply(movie.getCast());
        removeActorsFromListToSave.apply(movie.getDirectors());
        removeActorsFromListToSave.apply(movie.getWriters());
        return movie;
    }

    public static <E extends ContentType<E>> File createContentDirectory(E content) {
        File outDir;
        if (content instanceof Actor) {
            outDir = new File(SAVE_PATH_ACTOR + "\\actor" + content.getId());
        } else if(content instanceof Movie) {
            outDir = new File(SAVE_PATH_MOVIE + "\\movie" + content.getId());
        } else {
            logger.warn("Wrong input argument - \"{}\". Directory didn't created", content);
            return null;
        }
        if (outDir.mkdir()) {
            logger.info("New directory \"{}\" created", outDir);
        }
        return outDir;
    }

    public static List<Actor> readAllActorsFromDisk() {
        List<Actor> list = new ArrayList<>();
        List<File> actorDir = IO.listDirectory(new File(SAVE_PATH_ACTOR));
        if(actorDir == null || actorDir.isEmpty()) {
            logger.warn("Directory \"{}\" is empty or does not exist. Couldn't read data", SAVE_PATH_ACTOR);
            return list;
        }
        for(File dir : actorDir) {
            if(!dir.isDirectory()) continue;
            Actor actor = createActorFromXml(dir);
            if(actor != null) list.add(actor);
        }
        return list;
    }

    public static List<Movie> readAllMoviesFromDisk(ContentList<Actor> allActors) {
        List<Movie> list = new ArrayList<>();
        List<File> movieDir = IO.listDirectory(new File(SAVE_PATH_MOVIE));
        if(movieDir == null || movieDir.isEmpty()) {
            logger.warn("Directory \"{}\" is empty or does not exist. Couldn't read data", SAVE_PATH_MOVIE);
            return list;
        }
        if(allActors == null || allActors.isEmpty()) {
            logger.warn("To read movies is it required to provide not null and not empty list of all actors read former. Couldn't read data");
            return list;
        }
        for (File dir : movieDir) {
            if(!dir.isDirectory()) continue;
            Movie movie = createMovieFromXml(dir, allActors);
            if(movie != null) list.add(movie);
        }
        return list;
    }

    public static <E extends ContentType<E>> void createListFile(ContentList<E> list) {
        File savedFile;
        if(list == null || list.size() == 0) return;
        if(list.get(0) instanceof Actor) savedFile = new File(SAVE_PATH_ACTOR.concat("\\").concat(list.getListName()).concat(".xml"));
        else if(list.get(0) instanceof Movie) savedFile = new File(SAVE_PATH_MOVIE.concat("\\").concat(list.getListName()).concat(".xml"));
        else return;
        Document doc = createDoc();
        if(doc == null) return;
        Element rootElement = doc.createElement(list.getListName());
        doc.appendChild(rootElement);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(savedFile);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }

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

        Element element = doc.createElement(content.getClass().getSimpleName().concat(String.valueOf(content.getId())));
        element.appendChild(doc.createTextNode(String.valueOf(content.getId())));
        rootElement.appendChild(element);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(savedFile);
            transformer.transform(source, result);
//            logger.debug("ContentList file \"{}\" properly appended by \"{}\"", list.getListName(), content);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public static <E extends ContentType<E>> ContentList<E> createContentListFromXML(File inputFile) {
        Element root = createRootElementFromXml(inputFile);
        if(root == null) {
            logger.warn("Couldn't create ContentList from file \"{}\" - internal XML file issue", inputFile);
            return null;
        }

        NodeList nodes = root.getChildNodes();
        if(root.getFirstChild().getNodeName().matches("^" + Actor.class.getSimpleName() + "\\d+$")) {
            ContentList contentList = new ContentList<>(root.getNodeName());
            for(int i = 0; i < nodes.getLength(); i++) {
                String id = nodes.item(i).getTextContent();
//                contentList.add(createActorFromXml(new File(SAVE_PATH_ACTOR.concat("\\").concat("actor").concat(id).concat(".xml"))));
            }
            return contentList;

        } else if(root.getFirstChild().getNodeName().matches("^" + Movie.class.getSimpleName() + "\\d+$")) {
            ContentList<Movie> contentList = new ContentList<>(root.getNodeName());


        }
        return null;
    }


}

