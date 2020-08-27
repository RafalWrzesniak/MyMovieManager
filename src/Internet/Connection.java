package Internet;

import FileOperations.IO;
import MoviesAndActors.Actor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Connection {

    private URL websiteUrl;
    private URL defaultWebsiteUrl;
    private static String FILMWEB = "https://www.filmweb.pl";
    private static final Logger logger = LoggerFactory.getLogger(IO.class.getName());

    public Connection(String websiteUrl) throws MalformedURLException {
        this.websiteUrl = new URL(websiteUrl);
        this.defaultWebsiteUrl = this.websiteUrl;
    }

    public File downloadWebsite() throws IOException {
        String tmpFileName = "\\tmp_".concat(websiteUrl.getHost().replaceAll("(^.{3}\\.)|(\\..+)", ""))
                .concat("_").concat(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                        .replaceAll("\\..*$", "").replaceAll(":", "_")).concat(".html");
        ReadableByteChannel rbc = Channels.newChannel(websiteUrl.openStream());
        FileOutputStream fos = new FileOutputStream(IO.TMP_FILES.concat(tmpFileName));
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        return new File(IO.TMP_FILES.concat(tmpFileName));
    }


    public String grepLineFromWebsite(String find) throws IOException {
        URLConnection con;
        BufferedReader bufferedReader;
        con = websiteUrl.openConnection();
        InputStream inputStream = con.getInputStream();
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if(line.contains(find)) {
                return line;
            }
        }
        return null;
    }

    public String extractItemPropFromFilmWebLine(String itemToExtract, String line) {
        Pattern pattern = Pattern.compile(
                "<.+? itemprop=\"" + itemToExtract + "\"" +
                "( content=\"(\\d{4}-\\d{2}-\\d{2})\")?+" +
                "( src=\"(.+?)\")?+( .+?=\".+?\")??>" +
                "((.+?)( [vViI]+)??" +
                "(</.+?>))?+");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            // content - birthday
            if (matcher.group(2) != null) {
                return matcher.group(2);
            }
            // src - image
            if (matcher.group(4) != null) {
                return matcher.group(4);
            }
            return replaceAcutesHTML(matcher.group(7));
        }
        return null;
    }

    public String extractDeathDateFromFilmwebLine(String line) {
        Pattern pattern = Pattern.compile("dateToCalc=new Date\\((.+?)\\)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return LocalDate.parse(matcher.group(1), DateTimeFormatter.ofPattern("yyyy,MM,dd")).toString();
        }
        return null;
    }

    public void downloadImage(String imageUrl, String fileName) throws IOException {
        try (InputStream inputStream = new URL(imageUrl).openStream()) {
            Files.copy(inputStream, Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
        }
    }


    public void changeMovieUrlToCast() throws MalformedURLException {
        this.websiteUrl = new URL(defaultWebsiteUrl.toString().concat("/cast/actors"));
    }
    public void changeMovieUrlToCrew() throws MalformedURLException {
        this.websiteUrl = new URL(defaultWebsiteUrl.toString().concat("/cast/crew"));
    }


    public Map<String, String> grabActorDataFromFilmweb() throws IOException {
        String foundLine = grepLineFromWebsite("itemprop=\"birthDate\"");
        String fullName = extractItemPropFromFilmWebLine("name", foundLine);
        String birthday = extractItemPropFromFilmWebLine("birthDate", foundLine);
        String[] birthPlace = extractItemPropFromFilmWebLine("birthPlace", foundLine).replaceAll("\\(.+?\\)", "").split(", ");
        String imageUrl = extractItemPropFromFilmWebLine("image", foundLine);
        String deathDay = extractDeathDateFromFilmwebLine(foundLine);
        Map<String, String> actorData = new HashMap<>();
        actorData.put(Actor.NAME, fullName.substring(0, fullName.lastIndexOf(" ")));
        actorData.put(Actor.SURNAME, fullName.substring(fullName.lastIndexOf(" ") + 1));
        actorData.put(Actor.BIRTHDAY, birthday);
        actorData.put(Actor.NATIONALITY, birthPlace[birthPlace.length - 1]);
        actorData.put(Actor.FILMWEB, websiteUrl.toString());
        actorData.put(Actor.DEATH_DAY, deathDay);
        String imagePath;
        if (imageUrl != null) {
            imagePath = IO.SAVED_IMAGES.concat("\\").concat(fullName.replaceAll(" ", "_")).concat("_").concat(birthday.concat(".jpg"));
            try {
                downloadImage(imageUrl, imagePath);
            } catch (IOException e) {
                logger.warn("Couldn't download image of \"{}\" from \"{}\"", fullName, websiteUrl);
                imagePath = IO.NO_IMAGE;
            }
        } else {
            imagePath = IO.NO_IMAGE;
        }
        actorData.put(Actor.IMAGE_PATH, imagePath);
        logger.info("\"{}\" data properly grabbed from \"{}\"", fullName, websiteUrl);
        return actorData;
    }


    public String extractItemFromFilmwebLine(String itemToExtract, String line) {
        Pattern pattern = Pattern.compile(itemToExtract + "(=|\":)?+[\">]+(.+?)[\"<]");
        Matcher matcher = pattern.matcher(line);
        if(matcher.find()) {
            return replaceAcutesHTML(matcher.group(2));
        }
        return null;
    }

    public List<String> extractListOfItemsFromFilmwebLine(String itemToExtract, String line) {
        Pattern pattern = Pattern.compile(itemToExtract + "=\\d+\">(.+?)</a>");
        Matcher matcher = pattern.matcher(line);
        List<String> listOfItems = new ArrayList<>();
        while(matcher.find()) {
            listOfItems.add(replaceAcutesHTML(matcher.group(1)));
        }
        return listOfItems;
    }

    public List<String> extractCastLinksFromFilmwebLink(String itemToExtract, String line) {
        List<String> listOfItems = new ArrayList<>();
        Pattern pattern = Pattern.compile("data-profession=\"" + itemToExtract + "\"(.+?)<a href=\"(.+?)\">");
        Matcher matcher = pattern.matcher(line);
        int numberOfMatcher = 0;
        while(matcher.find() && numberOfMatcher < 10) {
            listOfItems.add(FILMWEB.concat(replaceAcutesHTML(matcher.group(2))));
            numberOfMatcher++;
        }
        return listOfItems;
    }





    public static String replaceAcutesHTML(String str) {
        str = str.replaceAll("&aacute;", "á");
        str = str.replaceAll("&eacute;", "é");
        str = str.replaceAll("&iacute;", "í");
        str = str.replaceAll("&oacute;", "ó");
        str = str.replaceAll("&uacute;", "ú");
        str = str.replaceAll("&Aacute;", "Á");
        str = str.replaceAll("&Eacute;", "É");
        str = str.replaceAll("&Iacute;", "Í");
        str = str.replaceAll("&Oacute;", "Ó");
        str = str.replaceAll("&Uacute;", "Ú");
        str = str.replaceAll("&ntilde;", "ñ");
        str = str.replaceAll("&Ntilde;", "Ñ");
        str = str.replaceAll("&egrave;", "è");
        str = str.replaceAll("&ucirc;", "û");
        str = str.replaceAll("&ocirc;", "ô");
        str = str.replaceAll("&quot;", "\"");
        str = str.replaceAll("&ouml;", "ö");
        str = str.replaceAll("&nbsp;", " ");
        str = str.replaceAll("&ndash; ", "- ");
        str = str.replaceAll("%C5%84", "ń");
        str = str.replaceAll("u0142", "ł");
        return str;
    }
}
