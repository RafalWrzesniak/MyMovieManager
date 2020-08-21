package FileOperations;

import MoviesAndActors.Actor;
import MoviesAndActors.ContentType;
import MoviesAndActors.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

public final class IO {

    private static final Logger logger = LoggerFactory.getLogger(IO.class.getName());
    public static final String TMP_FILES = System.getProperty("user.dir").concat("\\tmp");
    public static final String SAVED_IMAGES = System.getProperty("user.dir").concat("\\savedImages");
    public static final String NO_IMAGE = "resources\\iHaveNoImage.jpg";

    static {
        new File(SAVED_IMAGES).mkdir();
        File tmpFiles = new File(TMP_FILES);
        if(!tmpFiles.mkdir()) {
            IO.deleteDirectory(tmpFiles);
            tmpFiles.mkdir();
        }
    }

    private IO() {}

    public static List<String> getFileNamesInDirectory(File directory) {
        List<File> files = listDirectory(directory);
        List<String> fileNames = new ArrayList<>();
        for(File file : files) {
            String formattedName = removeFileExtension(file.getName());
            if(formattedName != null) {
                fileNames.add(formattedName);
            }
        }
//        logger.debug("Found {} properly decoded files in \"{}\"", fileNames.size(), directory);
        return fileNames;
    }

    public static List<File> listDirectory(File directory) {
        try {
            return Arrays.asList(Objects.requireNonNull(directory.listFiles()));
        } catch (NullPointerException ignore) {
            logger.warn("Directory \"{}\" does not exist", directory);
            return new ArrayList<>();
        }
    }

    public static String removeFileExtension(String fileName) {
        if(fileName.matches("^.+\\.ini$") || fileName.equals("Thumbs.db")) return null;
        if(fileName.contains(".")) {
            return fileName.substring(0, fileName.indexOf('.'));
        }
        return fileName;
    }

    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public static <E extends ContentType<E>> File createContentDirectory(E content) {
        File outDir;
        if (content instanceof Actor) {
            outDir = new File(XMLOperator.getSavePathActor() + "\\actor" + content.getId());
        } else if(content instanceof Movie) {
            outDir = new File(XMLOperator.getSavePathMovie() + "\\movie" + content.getId());
        } else {
            logger.warn("Wrong input argument - \"{}\". Directory didn't created", content);
            return null;
        }
        if (outDir.mkdir()) {
            logger.info("New directory \"{}\" created", outDir);
        }
        return outDir;
    }

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



    public static void createSummaryImage(Movie movie, File pathName) {
        if(!pathName.isDirectory()) {
            logger.warn("Failed to save summaryImage of movie \"{}\" - no such directory \"{}\"", movie, pathName);
            return;
        } else if(movie == null) {
            logger.warn("Failed to save summaryImage - movie is null");
            return;
        }
        List<String> keys = Arrays.asList(
                "Tytuł: ", "Długość: ", "Premiera: ", "Gatunek: ",
                "Produkcja: ", "Reżyseria: ", "Obsada: ", "Opis: ");
        List<String> movieValues = new ArrayList<>(movie.getDataForSummary());

        Font font = new Font("Helvetica", Font.PLAIN, 14);
        BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int maxDescLen = 125;
        int width = 850;
        int height = movie.getDescription().length() < maxDescLen ? (fm.getHeight()+10)*8 + 10 : (fm.getHeight()+10)*9 + 10;
        g2d.dispose();

        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        g2d = bufferedImage.createGraphics();
        Map<?, ?> desktopHints = (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        g2d.setRenderingHints(desktopHints);
        g2d.setColor(new Color(43, 43, 43));
        g2d.fillRect(0, 0, width, height);

        for(int i = 0; i < keys.size(); i++) {
            g2d.setFont(font);
            g2d.setColor(new Color(70, 150, 200));
            g2d.drawString(keys.get(i), 10, (fm.getAscent() + 10)*(i+1));

            g2d.setColor(new Color(175, 175, 175));
            g2d.setFont(font);

            if(i != keys.size()-1) {
                g2d.drawString(movieValues.get(i), 10 + fm.stringWidth(keys.get(i)), (fm.getAscent() + 10)*(i+1));
            } else { // description positioning
                if(movie.getDescription().length() < maxDescLen) {
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
            File imageFile = new File(pathName.toString().concat("\\").concat(pathName.getName()).concat(".png"));
            ImageIO.write(bufferedImage, "png", imageFile);
            logger.info("SummaryImage for \"{}\" created in \"{}\"", movie, imageFile);
        } catch (IOException e) {
            logger.warn("Failed to save summaryImage of movie \"{}\"", movie);
        }
    }



}
