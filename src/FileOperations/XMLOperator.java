package FileOperations;

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
import java.util.*;
import java.util.function.Function;

public class XMLOperator {

    private Document doc;
    private static String SAVE_PATH;
    private static String SAVE_PATH_MOVIE;
    private static String SAVE_PATH_ACTOR;
    private static final Logger logger = LoggerFactory.getLogger(XMLOperator.class.getName());
    public static final List<ContentType> OBJECTS_TO_SAVE = new ArrayList<>();

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

    public void changeSavePath(File newDirectory) {
        if(newDirectory != null && newDirectory.mkdir()) {
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

    public <E extends ContentType<E>> void saveContentToXML(E content) {
        this.doc = createDoc();
        assert doc != null;
        createXmlStructure(content);
        saveIntoXML(content);
    }

    private Document createDoc() {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            return dBuilder.newDocument();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Document createDocToRead(File inputFile) {
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


    private <E extends ContentType<E>> void saveIntoXML(E content) {
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
            OBJECTS_TO_SAVE.remove(content);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private <E extends ContentType<E>> void createXmlStructure(E content) {
        String tagName;
        if(content instanceof Actor) tagName = Actor.class.getSimpleName();
        else if(content instanceof Movie) tagName = Movie.class.getSimpleName();
        else return;

        Element rootElement = doc.createElement(tagName);
        doc.appendChild(rootElement);
        Attr attrType = doc.createAttribute("id");
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
                    childElement.appendChild(doc.createTextNode(Arrays.asList(listCheckSemicolon).get(i)));
                    element.appendChild(childElement);
                }
            }
            rootElement.appendChild(element);
        }
    }

    private Element createRootElementFromXml(File inputFile) {
        if(inputFile == null) {
            logger.warn("Null as input for XMLOperator passed");
            throw new IllegalArgumentException("Input file cannot be null!");
        } else if(!inputFile.toString().endsWith(".xml")) {
            logger.warn("Wrong input file extension for XMLOperator passed");
            throw new IllegalArgumentException("Wrong input file extension! Should be: .xml, found: " + inputFile.toString());
        }
        this.doc = createDocToRead(inputFile);
        if(doc == null) return null;
        return doc.getDocumentElement();
    }

    private File getFileFromDir(File inputDir) {
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
        logger.warn("There is no .xml file in directory \"{}\". Actor didn't created", inputDir);
        return null;

    }


    public Actor createActorFromXml(File inputDir) {

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
        String id = map.get(Actor.FIELD_NAMES.get(0));
        String name = map.get(Actor.FIELD_NAMES.get(1));
        String surname = map.get(Actor.FIELD_NAMES.get(2));
        String nationality = map.get(Actor.FIELD_NAMES.get(3));
        String birthday = map.get(Actor.FIELD_NAMES.get(4));
        String imagePath = map.get(Actor.FIELD_NAMES.get(5));
        Actor actor = new Actor(name, surname, nationality, birthday, imagePath, id);
        logger.info("New actor \"{}\" created successfully from file \"{}\"", actor, inputFile);
        if(XMLOperator.OBJECTS_TO_SAVE.contains(actor)) {
            XMLOperator.OBJECTS_TO_SAVE.remove(actor);
            logger.debug("Actor \"{}\" is saved", actor);
        }
        return actor;
    }

    public Movie createMovieFromXml(File inputDir, ContentList<Actor> allActors) {
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
        logger.debug("Movie \"{}\" is saved", movie);
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

    public List<Actor> readAllActorsFromDisk() {
        List<Actor> list = new ArrayList<>();
        List<File> actorDir = IO.listDirectory(new File(SAVE_PATH_ACTOR));
        if(actorDir == null || actorDir.isEmpty()) {
            logger.warn("Directory \"{}\" is empty or does not exist. Couldn't read data", SAVE_PATH_ACTOR);
            return null;
        }
        for(File dir : actorDir) {
            list.add(createActorFromXml(dir));
        }
        return list;
    }

    public List<Movie> readAllMoviesFromDisk(ContentList<Actor> allActors) {
        List<Movie> list = new ArrayList<>();
        List<File> movieDir = IO.listDirectory(new File(SAVE_PATH_MOVIE));
        if(movieDir == null || movieDir.isEmpty()) {
            logger.warn("Directory \"{}\" is empty or does not exist. Couldn't read data", SAVE_PATH_MOVIE);
            return null;
        }
        if(allActors == null || allActors.isEmpty()) {
            logger.warn("To read movies is it required to provide not null and not empty list of all actors read former. Couldn't read data");
            return null;
        }
        for(File dir : movieDir) {
            list.add(createMovieFromXml(dir, allActors));
        }
        return list;
    }


}

