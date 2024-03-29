package FileOperations;

import Configuration.Config;
import Configuration.Files;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentType;
import MoviesAndActors.Movie;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.*;

/**
 * Fully static and final class with private constructor to manage some I/O operations
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IO {

//    == public static methods ==
    /** Lists directory to ArrayList
     * @param directory
     * File to list
     * @return
     * Arraylist of files in directory
     */
    public static List<File> listDirectory(File directory) {
        try {
            if(directory == null) return new ArrayList<>();
            File[] listedFiles = directory.listFiles();
            if(listedFiles == null) return new ArrayList<>();
            List<File> files = new ArrayList<>(Arrays.asList(listedFiles));
            files.remove(new File(directory + "\\00DONE"));
            files.remove(new File(directory + "\\Thumbs.db"));
            files.removeIf(file -> file.getName().matches("^.+\\.ini$"));
            return files;
        } catch (NullPointerException ignore) {
            log.warn("Directory \"{}\" does not exist", directory);
            return new ArrayList<>();
        }
    }

    /**
     * Lists provided directory recursively and returns all found directories inside
     * @param directory
     * Directory to list recursively
     * @return
     * List of found directories
     */
    public static List<File> listDirectoryRecursively(File directory) {
        if(directory == null) return null;
        List<File> list = new ArrayList<>();
        for (File file : Objects.requireNonNull(listDirectory(directory))) {
            list.add(file);
            if(file.isDirectory()) {
                list.addAll(listDirectoryRecursively(file));
            }
        }
        return list;
    }


    /**
     * Creates list of files in directory and removes file extensions if present - see {@link #removeFileExtension}
     * @param directory
     * Directory to list
     * @return
     * List of directories and files without extensions
     * @see IO#removeFileExtension
     */
    public static List<String> getFileNamesInDirectory(File directory) {
        List<File> files = listDirectory(directory);
        List<String> fileNames = new ArrayList<>();
        for(File file : files) {
            String formattedName;
            if(!file.isDirectory() || file.getName().equals("00DONE")) {
                formattedName = removeFileExtension(file.getName());
            } else {
                formattedName = file.getName();
            }
            if(formattedName != null) {
                fileNames.add(formattedName);
            }
        }
        return fileNames;
    }


    /** Removes regular extension from file
     * @param fileName
     * Name of file to remove its extension
     * @return
     * null if fileName is null .
     * File name without extension - substring from the beginning to the last found '.'
     */
    public static String removeFileExtension(String fileName) {
        if(fileName == null) return null;
        if(fileName.matches("^.+\\.\\w.+$")) {
            return fileName.substring(0, fileName.lastIndexOf('.'));
        }
        return fileName;
    }

    /** Removes directory and all its subdirectories
     * @param directoryToBeDeleted
     * Directory that has to been removed
     * @return
     * true if everything was deleted, false in other case
     */
    public static boolean deleteDirectoryRecursively(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectoryRecursively(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    /** Creates folder in path that is connected with object type. Path is taken from {@link XMLOperator}
     * @param content
     * Object of type {@link E}
     * @param <E>
     *     Object implementing {@link ContentType} interface - {@link Actor} or {@link Movie}
     *
     * @return
     * Empty {@link File} (directory). {@link Config#getSAVE_PATH_ACTOR()} ()} or {@link Config#getSAVE_PATH_MOVIE()} ()}
     * and then \\actor or \\movie and in the end content id. For example: C:\ProjectPath\savedData\actor42
     */
    public static <E extends ContentType> File createContentDirectory(E content) {
        if (content instanceof Actor) {
            return createContentDirectory(Actor.class, content.getId());
        } else if(content instanceof Movie) {
            return createContentDirectory(Movie.class, content.getId());
        } else return null;
    }

    public static <E extends ContentType> File createContentDirectory(Class<E> type, int id) {
        File outDir;
        if (type.equals(Actor.class)) {
            outDir = new File(Config.getSAVE_PATH_ACTOR() + "\\actor" + id);
        } else if(type.equals(Movie.class)) {
            outDir = new File(Config.getSAVE_PATH_MOVIE() + "\\movie" + id);
        } else return null;
        if (outDir.mkdir()) {
            log.info("New directory \"{}\" created", outDir);
        }
        return outDir;
    }

    /** Extracts XML file from directory
     * @param inputDir
     * Directory from which it is needed to get only XML file
     * @return
     * null if inputDir is null or empty or is not a directory or in the directory there is no XML file.
     * First file with '.xml' extension when found
     */
    public static File getXmlFileFromDir(File inputDir) {
        if(inputDir == null || !inputDir.isDirectory()) {
            log.warn("Couldn't create content from directory \"{}\" - no such directory or is not a directory", inputDir);
            return null;
        }
        List<File> fileList = IO.listDirectory(inputDir);
        if(fileList.size() == 0) {
            log.warn("Couldn't create content from directory \"{}\" - directory is empty or does not exist", inputDir);
            return null;
        }
        for(File file : fileList) {
            if(file.toString().endsWith(".xml")) {
                return file;
            }
        }
        log.debug("There is no .xml file in directory \"{}\".", inputDir);
        return null;
    }

    /** Method is trying to find line in file that contains desired string
     * @param file
     * File to search in
     * @param textToFind
     * String that is being looked for
     * @return
     * Line containing desired string or null if not found
     * @throws IOException
     * if file does not exist
     */
    public static String findInFile(File file, String textToFind) throws IOException {
        FileReader fileIn = new FileReader(file);
        BufferedReader reader = new BufferedReader(fileIn);
        String line;
        while((line = reader.readLine()) != null) {
            if ((line.contains(textToFind))) {
                reader.close();
                return line;
            }
        }
        reader.close();
        return null;
    }

    public static void moveFiles(Path source, Path target) {
        IO.listDirectoryRecursively(source.toFile()).forEach(file -> {
            if(!file.isDirectory() && file.exists()) {
                Path targetPath = target.resolve(file.toPath().subpath(source.getNameCount(), file.toPath().getNameCount()));
                try {
                    targetPath.getParent().toFile().mkdirs();
                    java.nio.file.Files.move(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                    java.nio.file.Files.delete(file.toPath().getParent());
                } catch (IOException ignored) { }
            }
        });
        try {
            java.nio.file.Files.delete(source.getParent());
        } catch (IOException ignored) { }
    }

    /** Method created a .png file with basic information about movie. It uses {@link Movie#getDataForSummary()} to get
     * all need movie data. It will be saved in pathName\pathName.{@link File#getName()}.png
     * @param movie
     * Movie to create its summary image
     * @param folderToSaveIn
     * Path of directory that contains movie file
     */
    public static void createSummaryImage(Movie movie, File folderToSaveIn) {
        if(!folderToSaveIn.isDirectory()) {
            log.warn("Failed to save summaryImage of movie \"{}\" - no such directory \"{}\"", movie, folderToSaveIn);
            return;
        } else if(movie == null) {
            log.warn("Failed to save summaryImage - movie is null");
            return;
        }

        Font font = new Font("Helvetica", Font.PLAIN, 14);
        BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int maxDescLen = 120;
        int width = 850;
        int movieDescLen;
        if(movie.getDescription() != null) {
            movieDescLen = movie.getDescription().length();
        } else {
            movieDescLen = 100;
        }
        int height = movieDescLen < maxDescLen ? (fm.getHeight()+10)*8 + 10 : (fm.getHeight()+10)*9 + 10;
        g2d.dispose();

        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        g2d = bufferedImage.createGraphics();
        Map<?, ?> desktopHints = (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        g2d.setRenderingHints(desktopHints);
        g2d.setColor(new Color(43, 43, 43));
        g2d.fillRect(0, 0, width, height);

        List<String> keys = Arrays.asList(
                "Tytuł: ", "Długość: ", "Premiera: ", "Gatunek: ",
                "Produkcja: ", "Reżyseria: ", "Obsada: ", "Opis: ");
        List<String> movieValues = new ArrayList<>(movie.getDataForSummary());
        for(int i = 0; i < keys.size(); i++) {
            g2d.setFont(font);
            g2d.setColor(new Color(70, 150, 200));
            g2d.drawString(keys.get(i), 10, (fm.getAscent() + 10)*(i+1));

            g2d.setColor(new Color(175, 175, 175));
            g2d.setFont(font);

            if(i != keys.size()-1) {
//                g2d.drawString(movieValues.get(i), 10 + fm.stringWidth(keys.get(i)), (fm.getAscent() + 10)*(i+1));
                g2d.drawString(movieValues.get(i), 10 + 70, (fm.getAscent() + 10)*(i+1));
            } else { // description positioning
                if(movieDescLen < maxDescLen) {
                    g2d.drawString(movieValues.get(i), 10, (fm.getAscent() + 10)*(i+2));
                } else {
                    int spaceInTheMiddle = movie.getDescription().substring(0, maxDescLen).lastIndexOf(' ');
                    g2d.drawString(movie.getDescription().substring(0, spaceInTheMiddle), 10, (fm.getAscent() + 10)*(i+2));
                    g2d.drawString(movie.getDescription().substring(spaceInTheMiddle+1), 10, (fm.getAscent() + 10)*(i+3));
                }
            }
        }
        g2d.dispose();

        try {
            File imageFile = Paths.get(folderToSaveIn.toString(), folderToSaveIn.getName().concat(".png")).toFile();
            ImageIO.write(bufferedImage, "png", imageFile);
            log.info("SummaryImage for \"{}\" created in \"{}\"", movie, imageFile);
        } catch (IOException e) {
            log.warn("Failed to save summaryImage of movie \"{}\"", movie);
        }
    }

    public static void writeLastRideFile(Map<File, Integer> map) {
        Document doc = XMLOperator.createDoc();
        if(doc != null) {
            Element rootElement = doc.createElement("LastRide");
            doc.appendChild(rootElement);

            for(Map.Entry<File, Integer> entry : map.entrySet()) {
                rootElement.appendChild(doc.createTextNode("\n\t"));
                Element element = doc.createElement("folder");
                element.appendChild(doc.createTextNode("\n\t\t"));
                Element name = doc.createElement("name");
                name.setTextContent(entry.getKey().toString());
                element.appendChild(name);

                element.appendChild(doc.createTextNode("\n\t\t"));
                Element id = doc.createElement("id");
                id.setTextContent(String.valueOf(entry.getValue()));
                element.appendChild(id);
                element.appendChild(doc.createTextNode("\n\t"));
                rootElement.appendChild(element);
            }
            rootElement.appendChild(doc.createTextNode("\n"));
            XMLOperator.makeSimpleSave(doc, Files.LAST_RIDE);
            log.info("Saved \"{}\" files from MainMovieFolder to lastly saw file", map.size());
        } else {
            log.warn("Couldn't save state of MainMovieFolder");
        }

    }

    public static Map<File, Integer> readLastStateOfMainMovieFolder() {
        Map<File, Integer> lastState = new HashMap<>();
        Document doc = XMLOperator.createDocToRead(Files.LAST_RIDE);
        if(doc != null) {
            Element root = doc.getDocumentElement();
            NodeList elements = root.getElementsByTagName("folder");
            for(int i = 0; i < elements.getLength(); i++) {
                NodeList child = elements.item(i).getChildNodes();
                File file = null;
                int id = -1;
                for(int k = 0; k < child.getLength(); k++) {
                    if(child.item(k).getNodeName().equals("name")) {
                        file = new File(child.item(k).getTextContent());
                    }
                    if(child.item(k).getNodeName().equals("id")) {
                        id = Integer.parseInt(child.item(k).getTextContent());
                    }
                }
                if(file != null && id != -1) {
                    lastState.putIfAbsent(file, id);
                }
            }
        } else {
            log.warn("Couldn't read data from \"{}\"", Config.getMAIN_MOVIE_FOLDER());
        }
        return lastState;
    }


}
