package Internet;

import FileOperations.IO;
import MoviesAndActors.Actor;
import MoviesAndActors.Movie;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Map.entry;

public class Connection {

    private URL websiteUrl;
    private final URL defaultWebsiteUrl;
    private static final String FILMWEB = "https://www.filmweb.pl";
    private static final Logger logger = LoggerFactory.getLogger(Connection.class.getName());
    public static final String MOVIE_LINE_KEY = "data-linkable=\"filmMain\"";
    public static final String CAST_LINE_KEY  = "data-linkable=\"filmFullCast\"";
    public static final String ACTOR_LINE_KEY = "personMainHeader";
    private static final Map<String, String> ACTOR_CLASS_FIELDS_MAP_FILMWEB_KEYS = Map.ofEntries(
            entry(Actor.NAME,        "name"),
            entry(Actor.NATIONALITY, "birthPlace"),
            entry(Actor.BIRTHDAY,    "itemprop=\"birthDate\" content"),
            entry(Actor.IMAGE_PATH,  "itemprop=\"image\" src")
    );
    private static final Map<String, String> MOVIE_CLASS_FIELDS_MAP_FILMWEB_KEYS = Map.ofEntries(
            entry(Movie.TITLE,      "data-title"),
            entry(Movie.TITLE_ORG,  "originalTitle"),
            entry(Movie.PREMIERE,   "releaseCountryPublicString"),
            entry(Movie.DURATION,   "duration"),
            entry(Movie.RATE,       "data-rate"),
            entry(Movie.RATE_COUNT, "dataRating-count"),
            entry(Movie.DESCRIPTION,"itemprop=\"description\""),
            entry(Movie.IMAGE_PATH, "itemprop=\"image\" content")
    );
    private static final Map<String, String> MOVIE_CLASS_LIST_FIELDS_MAP_FILMWEB_KEYS = Map.ofEntries(
            entry(Movie.GENRES,     "genres"),
            entry(Movie.PRODUCTION, "countries")
    );
    public static final Map<String, String> MOVIE_CLASS_CAST_FIELDS_MAP_FILMWEB_KEYS = Map.ofEntries(
            entry(Movie.CAST,     "actors"),
            entry(Movie.DIRECTORS,"director"),
            entry(Movie.WRITERS,  "screenwriter")
    );


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


    public Map<String, String> grabActorDataFromFilmwebAndCreateActorMap() throws IOException {
        Map<String, String> actorData = new HashMap<>();
        String foundLine = grepLineFromWebsite(ACTOR_LINE_KEY);

        String fullName = extractItemFromFilmwebLine(ACTOR_CLASS_FIELDS_MAP_FILMWEB_KEYS.get(Actor.NAME), foundLine);
        String birthday = extractItemFromFilmwebLine(ACTOR_CLASS_FIELDS_MAP_FILMWEB_KEYS.get(Actor.BIRTHDAY), foundLine);
        String[] birthPlace = extractItemFromFilmwebLine(ACTOR_CLASS_FIELDS_MAP_FILMWEB_KEYS.get(Actor.NATIONALITY), foundLine).replaceAll("\\(.+?\\)", "").split(", ");
        String imageUrl = extractItemFromFilmwebLine(ACTOR_CLASS_FIELDS_MAP_FILMWEB_KEYS.get(Actor.IMAGE_PATH), foundLine);
        String deathDay = extractDeathDateFromFilmwebLine(foundLine);

        actorData.put(Actor.NAME, fullName.substring(0, fullName.lastIndexOf(" ")));
        actorData.put(Actor.SURNAME, fullName.substring(fullName.lastIndexOf(" ") + 1));
        actorData.put(Actor.BIRTHDAY, birthday);
        actorData.put(Actor.NATIONALITY, birthPlace[birthPlace.length - 1]);
        actorData.put(Actor.FILMWEB, websiteUrl.toString());
        actorData.put(Actor.DEATH_DAY, deathDay);
        actorData.put(Actor.IMAGE_PATH, imageUrl);

        logger.info("Data properly grabbed from \"{}\"", websiteUrl);
        return actorData;
    }

    public Map<String, List<String>> grabBasicMovieDataFromFilmwebAndCreateMovieMap() throws IOException {
        Map<String, List<String>> movieData = new HashMap<>();
        String foundLine = grepLineFromWebsite(MOVIE_LINE_KEY);

        for (Map.Entry<String, String> pair : MOVIE_CLASS_FIELDS_MAP_FILMWEB_KEYS.entrySet()) {
            movieData.put(pair.getKey(), Collections.singletonList(extractItemFromFilmwebLine(pair.getValue(), foundLine)));
        }
        for(Map.Entry<String, String> pair : MOVIE_CLASS_LIST_FIELDS_MAP_FILMWEB_KEYS.entrySet()) {
            movieData.put(pair.getKey(), extractListOfItemsFromFilmwebLine(pair.getValue(), foundLine));
        }
        movieData.put(Movie.FILMWEB, Collections.singletonList(defaultWebsiteUrl.toString()));

        logger.info("Data properly grabbed from \"{}\"", websiteUrl);
        return movieData;
    }

    public List<String> grabCastOrCrewFromFilmweb(String castType) throws IOException {
        if(!castType.equals(MOVIE_CLASS_CAST_FIELDS_MAP_FILMWEB_KEYS.get(Movie.CAST)) &&
                !castType.equals(MOVIE_CLASS_CAST_FIELDS_MAP_FILMWEB_KEYS.get(Movie.DIRECTORS)) &&
                !castType.equals(MOVIE_CLASS_CAST_FIELDS_MAP_FILMWEB_KEYS.get(Movie.WRITERS))) {
            logger.warn("Wrong castType parameter, can't download cast data of \"{}\" from \"{}\"", castType, websiteUrl);
            throw new IllegalArgumentException("Wrong castType parameter");
        }

        return extractCastLinksFromFilmwebLink(castType, grepLineFromWebsite(CAST_LINE_KEY));
    }


//        if (imageUrl != null) {
//            imagePath = IO.SAVED_IMAGES.concat("\\").concat(fullName.replaceAll(" ", "_")).concat("_").concat(birthday.concat(".jpg"));
//            try {
//                downloadImage(imageUrl, imagePath);
//            } catch (IOException e) {
//                logger.warn("Couldn't download image of \"{}\" from \"{}\"", fullName, websiteUrl);
//                imagePath = IO.NO_IMAGE;
//            }
//        } else {
//            imagePath = IO.NO_IMAGE;
//        }

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
