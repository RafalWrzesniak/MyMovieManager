package FileOperations;

import MoviesAndActors.Actor;
import MoviesAndActors.ContentType;
import MoviesAndActors.Movie;
import MyMovieManager.MovieMainFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Fully static and final class with private constructor to manage some I/O operations
 */
public final class IO {

    private static final Logger logger = LoggerFactory.getLogger(IO.class.getName());
    /**
     * Path for files that are needed only for some time
     */
    public static final Path TMP_FILES = Paths.get(System.getProperty("user.dir"), "tmp");
    /**
     * File to be used when there is no available image of something on the web
     */
    public static final Path NO_IMAGE = Paths.get("resources", "iHaveNoImage.jpg");
    /**
     * File that contains last saw files in {@link MovieMainFolder#getMainMovieFolder()}
     */
    public static final File LAST_RIDE = Paths.get("resources", "lastRide.xml").toFile();

    private static Path SAVE_PATH;
    private static Path SAVE_PATH_MOVIE;
    private static Path SAVE_PATH_ACTOR;

    static {
        initCfg();
        XMLOperator.setLocalPaths();

        File tmpFiles = TMP_FILES.toFile();
        if(!tmpFiles.mkdir()) {
            IO.deleteDirectoryRecursively(tmpFiles);
            tmpFiles.mkdir();
        }
    }

    private IO() {}

    private static void initCfg() {
        File cfg = Paths.get("resources", "config.cfg").toFile();
        if(cfg.exists() && !cfg.isDirectory()) {
            Document doc = XMLOperator.createDocToRead(cfg);
            if(doc != null) {
                Element root = doc.getDocumentElement();
                NodeList element = root.getElementsByTagName("SAVE_PATH");
                SAVE_PATH = Paths.get(element.item(0).getChildNodes().item(0).getTextContent());
                element = root.getElementsByTagName("MAIN_MOVIE_FOLDER");
                MovieMainFolder.setMainMovieFolder(new File(element.item(0).getChildNodes().item(0).getTextContent()));
            }
        } else {
            Document doc = XMLOperator.createDoc();
            if(doc != null) {
                Element rootElement = doc.createElement("config");
                rootElement.appendChild(doc.createTextNode("\n\t"));
                doc.appendChild(rootElement);
                Element element = doc.createElement("SAVE_PATH");
                rootElement.appendChild(element);
                rootElement.appendChild(doc.createTextNode("\n\t"));
                element = doc.createElement("MAIN_MOVIE_FOLDER");
                rootElement.appendChild(element);
                rootElement.appendChild(doc.createTextNode("\n"));
                XMLOperator.makeSimpleSave(doc, cfg);
            }
            updateParamInCfg("SAVE_PATH", System.getProperty("user.dir").concat("\\savedData"));
            updateParamInCfg("MAIN_MOVIE_FOLDER", Paths.get("E:", "Rafał", "Filmy").toString());
            boolean made1 = Paths.get(System.getProperty("user.dir"), "savedData").toFile().mkdir();
            SAVE_PATH = Paths.get(System.getProperty("user.dir"), "savedData");
        }
        boolean made2 =  SAVE_PATH.toFile().mkdirs();
        updateRelativePaths();
    }

    public static void changeSavePath(File newDirectory) {
        System.out.println(newDirectory);
        if(newDirectory != null) {
            newDirectory.mkdir();
            try {
                Files.move(SAVE_PATH, newDirectory.toPath(), REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            updateParamInCfg("SAVE_PATH", newDirectory.toString());
            SAVE_PATH = newDirectory.toPath();
            updateRelativePaths();
        } else {
            logger.warn("Couldn't change SAVE_PATH to \"{}\". SAVE_PATH is still \"{}\"", newDirectory, SAVE_PATH);
        }
    }

    static void updateRelativePaths() {
        SAVE_PATH_MOVIE = Paths.get(SAVE_PATH.toString(), Movie.class.getSimpleName());
        SAVE_PATH_ACTOR = Paths.get(SAVE_PATH.toString(), Actor.class.getSimpleName());
        boolean made1 = SAVE_PATH_MOVIE.toFile().mkdir();
        boolean made2 = SAVE_PATH_ACTOR.toFile().mkdir();

    }

    public static void updateParamInCfg(String parameter, String value) {
        File cfg = Paths.get("resources", "config.cfg").toFile();
        Document doc = XMLOperator.createDocToRead(cfg);
        if(doc == null) return;
        NodeList element = doc.getElementsByTagName(parameter);
        if(!element.item(0).getTextContent().equals(value)) {
            element.item(0).setTextContent(value);
            XMLOperator.makeSimpleSave(doc, cfg);
            logger.info("Parameter \"{}\" changed to \"{}\" in config.cfg", parameter, value);
        }
    }

    public static Path getSavePath() {
        return SAVE_PATH;
    }
    public static Path getSavePathActor() {
        return SAVE_PATH_ACTOR;
    }
    public static Path getSavePathMovie() {
        return SAVE_PATH_MOVIE;
    }
    /** Lists directory to ArrayList
     * @param directory
     * File to list
     * @return
     * Arraylist of files in directory
     */
    public static List<File> listDirectory(File directory) {
        try {
            List<File> files = new ArrayList<>(Arrays.asList(directory.listFiles()));
            files.remove(new File(directory + "\\00DONE"));
            files.remove(new File(directory + "\\Thumbs.db"));
            files.removeIf(file -> file.getName().matches("^.+\\.ini$"));
            return files;
        } catch (NullPointerException ignore) {
            logger.warn("Directory \"{}\" does not exist", directory);
            return new ArrayList<>();
        }
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
        if(fileName.contains(".")) {
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
     * Empty {@link File} (directory). {@link #getSavePathActor()} or {@link #getSavePathMovie()} ()}
     * and then \\actor or \\movie and in the end content id. For example: C:\ProjectPath\savedData\actor42
     */
    public static <E extends ContentType<E>> File createContentDirectory(E content) {
        File outDir = null;
        if (content instanceof Actor) {
            outDir = new File(getSavePathActor() + "\\actor" + content.getId());
        } else if(content instanceof Movie) {
            outDir = new File(getSavePathMovie() + "\\movie" + content.getId());
        } else return null;
        if (outDir.mkdir()) {
            logger.info("New directory \"{}\" created", outDir);
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
            logger.warn("Couldn't create content from directory \"{}\" - no such directory or is not a directory", inputDir);
            return null;
        }
        List<File> fileList = IO.listDirectory(inputDir);
        if(fileList.size() == 0) {
            logger.warn("Couldn't create content from directory \"{}\" - directory is empty or does not exist", inputDir);
            return null;
        }
        for(File file : fileList) {
            if(file.toString().endsWith(".xml")) {
                return file;
            }
        }
        logger.warn("There is no .xml file in directory \"{}\".", inputDir);
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
                return line;
            }
        }
        return null;
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
            logger.warn("Failed to save summaryImage of movie \"{}\" - no such directory \"{}\"", movie, folderToSaveIn);
            return;
        } else if(movie == null) {
            logger.warn("Failed to save summaryImage - movie is null");
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
            logger.info("SummaryImage for \"{}\" created in \"{}\"", movie, imageFile);
        } catch (IOException e) {
            logger.warn("Failed to save summaryImage of movie \"{}\"", movie);
        }
    }

    public static class ExportAll extends Thread {

        File outPutFile;

        public ExportAll(File outPutFile) {
            if(outPutFile != null) {
                if(!outPutFile.getName().endsWith(".zip")) {
                    outPutFile = new File(outPutFile.getName().concat(".zip"));
                }
            }
            this.outPutFile = outPutFile;
        }

        public ExportAll() {
        }

        @Override
        public void run() {
            setName("ExportAll");
            if(outPutFile == null) {
                outPutFile = new File(
                        System.getProperty("user.dir")
                        .concat("\\MyMovieManager_exported_data_")
                        .concat(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                        .replaceAll("\\..*$", "")
                        .replaceAll(":", "_")
                        .concat(".zip")));
            }
            try {
                zipFile(getSavePath().toFile(), outPutFile);
                logger.info("All data was successfully zipped end exported to \"{}\"", outPutFile);
            } catch (IOException e) {
                logger.warn("Failed to ExportAll and ZIP file");
            }

        }

        private void zipFile(File fileToZip, File zipOut) throws IOException {
            ZipOutputStream zipOutStream = new ZipOutputStream(new FileOutputStream(zipOut.toString()));
            zipFile(fileToZip, "exported", zipOutStream);
            zipOutStream.close();
        }

        private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
            if(fileToZip == null) return;
            List<File> files = IO.listDirectory(fileToZip);
            if(files.size() == 0) files.add(fileToZip);
            for(File file : files) {
                if(file.isDirectory()) {
                    zipFile(file, fileName.concat("\\").concat(file.getName()), zipOut);
                } else {
                    FileInputStream fis = new FileInputStream(file);
                    ZipEntry zipEntry = new ZipEntry(fileName.concat("\\").concat(file.getName()));
                    zipOut.putNextEntry(zipEntry);
                    byte[] bytes = new byte[1024];
                    int length;
                    while((length = fis.read(bytes)) >= 0) {
                        zipOut.write(bytes, 0, length);
                    }
                    zipOut.closeEntry();
                    fis.close();
                }
            }
        }



    }

    public static class ImportAll extends Thread {

        File inputFile;

        public ImportAll(File inputFile) {
            this.inputFile = inputFile;
        }

        @Override
        public void run() {
            setName("ImportAll");
            try {
                deleteDirectoryRecursively(getSavePath().toFile());
                unZipFile();
                logger.info("Successfully imported data from \"{}\"", inputFile);
            } catch (IOException e) {
                e.printStackTrace();
                logger.warn("Failed to import file \"{}\"", inputFile);
            }
        }

        private void unZipFile() throws IOException{
            File destDir = getSavePath().toFile();
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(inputFile));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String zipName;
                if(zipEntry.getName().startsWith("exported")) {
                    zipName = zipEntry.getName().substring(zipEntry.getName().indexOf('\\'));
                } else throw new IOException("Wrong ZIP file, can't import data");
                String relPath = zipName.substring(0, zipName.lastIndexOf('\\'));
                new File(destDir, relPath).mkdirs();
                File newFile = new File(destDir, zipName);
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
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
            XMLOperator.makeSimpleSave(doc, LAST_RIDE);
            logger.info("Saved \"{}\" files from MainMovieFolder to lastly saw file", map.size());
        } else {
            logger.warn("Couldn't save state of MainMovieFolder");
        }

    }

    public static Map<File, Integer> readLastStateOfMainMovieFolder() {
        Map<File, Integer> lastState = new HashMap<>();
        Document doc = XMLOperator.createDocToRead(LAST_RIDE);
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
            logger.warn("Couldn't read data from \"{}\"", MovieMainFolder.getMainMovieFolder());
        }
        return lastState;
    }



}
