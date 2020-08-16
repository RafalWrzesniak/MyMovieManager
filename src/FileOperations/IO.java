package FileOperations;

import MoviesAndActors.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public final class IO {

    private static final Logger logger = LoggerFactory.getLogger(IO.class.getName());

    private IO() {}

    public static List<String> getFileNamesInDirectory(File directory) {
        List<File> files = listDirectory(directory);
        List<String> fileNames = new ArrayList<>();
        if(files == null) return fileNames;
        for(File file : files) {
            String formattedName = removeFileExtension(file.getName());
            if(formattedName != null) {
                fileNames.add(formattedName);
            }
        }
        logger.debug("Found {} properly decoded files in \"{}\"", fileNames.size(), directory);
        return fileNames;
    }

    public static List<File> listDirectory(File directory) {
        try {
            return Arrays.asList(Objects.requireNonNull(directory.listFiles()));
        } catch (NullPointerException ignore) {
            logger.warn("Directory \"{}\" does not exist", directory);
            return null;
        }
    }

    public static String removeFileExtension(String fileName) {
        if(fileName.matches("^.+\\.ini$") || fileName.equals("Thumbs.db")) return null;
        if(fileName.contains(".")) {
            return fileName.substring(0, fileName.indexOf('.'));
        }
        return fileName;
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
