package Internet;

import FileOperations.IO;
import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
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
    private URL mainMoviePage;
    private static final String FILMWEB = "https://www.filmweb.pl";
    private static final Logger logger = LoggerFactory.getLogger(Connection.class.getName());
    private static final String MOVIE_LINE_KEY  = "data-linkable=\"filmMain\"";
    private static final String MOVIE_LINE_KEY2 = "data-source=\"linksData\"";
    private static final String ACTOR_LINE_KEY  = "personMainHeader";
    private static final String CAST_LINE_KEY   = "data-linkable=\"filmFullCast\"";
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


    public Connection(String websiteUrl) throws IOException {
        changeUrlTo(websiteUrl);
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


    public static void downloadImage(String imageUrl, String fileName) throws IOException {
        try (InputStream inputStream = new URL(imageUrl).openStream()) {
            Files.copy(inputStream, Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void changeUrlTo(String newUrl) throws MalformedURLException {
        if(newUrl.matches("^" + FILMWEB + "/film/[^/]+?$")) {
            this.mainMoviePage = new URL(newUrl);
        }
        this.websiteUrl = new URL(newUrl);
    }

    public void changeMovieUrlToCastActors() throws MalformedURLException {
        this.websiteUrl = new URL(mainMoviePage.toString().concat("/cast/actors"));
    }
    public void changeMovieUrlToCastCrew() throws MalformedURLException {
        this.websiteUrl = new URL(mainMoviePage.toString().concat("/cast/crew"));
    }

    private String grepLineFromWebsite(String find) throws IOException {
        if(find == null) return null;
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
        logger.warn("Couldn't find proper line containing \"{}\" on \"{}\"", find, websiteUrl);
        return null;
    }

    private String extractItemFromFilmwebLine(String itemToExtract, String line) {
        if(itemToExtract == null || line == null) {
            logger.warn("Null as input - can't extract item \"{}\" from \"{}\"", itemToExtract, websiteUrl);
            return null;
        }
        Pattern pattern = Pattern.compile(itemToExtract + "(=|\":)?+[\">]+(.+?)[\"<]");
        Matcher matcher = pattern.matcher(line);
        if(matcher.find()) {
            return replaceAcutesHTML(matcher.group(2));
        } else {
            logger.warn("Couldn't extract \"{}\" from \"{}\"", itemToExtract, websiteUrl);
        }
        return null;
    }

    private List<String> extractListOfItemsFromFilmwebLine(String itemToExtract, String line) {
        if(itemToExtract == null || line == null) {
            logger.warn("Null as input - can't extract list of items \"{}\" from \"{}\"", itemToExtract, websiteUrl);
            return null;
        }
        Pattern pattern = Pattern.compile(itemToExtract + "=\\d+\">(.+?)</a>");
        Matcher matcher = pattern.matcher(line);
        List<String> listOfItems = new ArrayList<>();
        while(matcher.find()) {
            if(!listOfItems.contains(replaceAcutesHTML(matcher.group(1)))) {
                listOfItems.add(replaceAcutesHTML(matcher.group(1)));
            }
        }
        if(listOfItems.size() == 0) {
            logger.warn("Couldn't find any list item \"{}\" on \"{}\"", itemToExtract, websiteUrl);
        }
        return listOfItems;
    }

    private List<String> extractCastLinksFromFilmwebLink(String itemToExtract, String line) {
        if(itemToExtract == null || line == null) {
            logger.warn("Null as input - can't extract cast links \"{}\" from \"{}\"", itemToExtract, websiteUrl);
            return null;
        }
        List<String> listOfItems = new ArrayList<>();
        Pattern pattern = Pattern.compile("data-profession=\"" + itemToExtract + "\"(.+?)<a href=\"(.+?)\">");
        Matcher matcher = pattern.matcher(line);
        int numberOfMatcher = 0;
        while(matcher.find() && numberOfMatcher < 10) {
            listOfItems.add(FILMWEB.concat(matcher.group(2)));
            numberOfMatcher++;
        }
        if(listOfItems.size() == 0) {
            logger.warn("Couldn't find any cast link \"{}\" on \"{}\"", itemToExtract, websiteUrl);
        }
        return listOfItems;
    }

    private String extractDeathDateFromFilmwebLine(String line) {
        if(line == null) {
            logger.warn("Null as input - can't extract death date from \"{}\"", websiteUrl);
            return null;
        }
        Pattern pattern = Pattern.compile("dateToCalc=new Date\\((.+?)\\)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String[] split = matcher.group(1).split(",");
            return LocalDate.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])).toString();
        }
        return null;
    }


    public Actor createActorFromFilmwebLink() throws IOException, NullPointerException {
        Map<String, String> actorData = new HashMap<>();
        String foundLine = grepLineFromWebsite(ACTOR_LINE_KEY);

        String fullName = extractItemFromFilmwebLine(ACTOR_CLASS_FIELDS_MAP_FILMWEB_KEYS.get(Actor.NAME), foundLine).replaceAll(" [iIvVxX]+", "");
        String birthday = replaceNullWithDash(extractItemFromFilmwebLine(ACTOR_CLASS_FIELDS_MAP_FILMWEB_KEYS.get(Actor.BIRTHDAY), foundLine));
        String[] birthPlace = replaceNullWithDash(extractItemFromFilmwebLine(ACTOR_CLASS_FIELDS_MAP_FILMWEB_KEYS.get(Actor.NATIONALITY), foundLine)).replaceAll("\\(.+?\\)", "").split(", ");
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
        return new Actor(actorData);
    }

    private Map<String, List<String>> grabBasicMovieDataFromFilmwebAndCreateMovieMap() throws IOException {
        Map<String, List<String>> movieData = new HashMap<>();
        String foundLine = grepLineFromWebsite(MOVIE_LINE_KEY).concat(grepLineFromWebsite(MOVIE_LINE_KEY2));

        for (Map.Entry<String, String> pair : MOVIE_CLASS_FIELDS_MAP_FILMWEB_KEYS.entrySet()) {
            movieData.put(pair.getKey(), Collections.singletonList(extractItemFromFilmwebLine(pair.getValue(), foundLine)));
        }
        for(Map.Entry<String, String> pair : MOVIE_CLASS_LIST_FIELDS_MAP_FILMWEB_KEYS.entrySet()) {
            movieData.put(pair.getKey(), extractListOfItemsFromFilmwebLine(pair.getValue(), foundLine));
        }
        if(movieData.get(Movie.TITLE_ORG).get(0) == null) movieData.replace(Movie.TITLE_ORG, movieData.get(Movie.TITLE));
        movieData.put(Movie.FILMWEB, Collections.singletonList(mainMoviePage.toString()));

        logger.info("Data properly grabbed from \"{}\"", websiteUrl);
        return movieData;
    }

    private List<String> grabCastOrCrewFromFilmweb(String castType) throws IOException {
        if(castType == null || !castType.equals(MOVIE_CLASS_CAST_FIELDS_MAP_FILMWEB_KEYS.get(Movie.CAST)) &&
                !castType.equals(MOVIE_CLASS_CAST_FIELDS_MAP_FILMWEB_KEYS.get(Movie.DIRECTORS)) &&
                !castType.equals(MOVIE_CLASS_CAST_FIELDS_MAP_FILMWEB_KEYS.get(Movie.WRITERS))) {
            logger.warn("Wrong castType parameter, can't download cast data of \"{}\" from \"{}\"", castType, websiteUrl);
            throw new IllegalArgumentException("Wrong castType parameter");
        }
        return extractCastLinksFromFilmwebLink(castType, grepLineFromWebsite(CAST_LINE_KEY));
    }

    public List<Actor> createActorsFromFilmwebLinks(List<String> actorUrls, ContentList<Actor> allActors) {
        List<Actor> actorList = new ArrayList<>();
        if(actorUrls == null || actorUrls.size() == 0 || allActors == null || allActors.size() == 0) {
             logger.warn("Null or empty argument");
            return actorList;
        }
        for(String actorUrl : actorUrls) {
            if(allActors.find(actorUrl).size() == 0) {
                try {
                    Connection connection = new Connection(actorUrl);
                    Actor actor = connection.createActorFromFilmwebLink();
                    actorList.add(actor);
                    allActors.add(actor);
                } catch (IOException | NullPointerException e) {
                    logger.warn("Can't get data from \"{}\"", actorUrl);
                }
            } else actorList.add(allActors.find(actorUrl).get(0));

        }
        return actorList;
    }

    public Movie createMovieFromFilmwebLink(ContentList<Actor> allActors) throws IOException {
        Movie movie = new Movie(grabBasicMovieDataFromFilmwebAndCreateMovieMap());

        changeMovieUrlToCastActors();
        List<String> castUrls = grabCastOrCrewFromFilmweb(Connection.MOVIE_CLASS_CAST_FIELDS_MAP_FILMWEB_KEYS.get(Movie.CAST));
        List<Actor> actors = createActorsFromFilmwebLinks(castUrls, allActors);
        movie.addActors(actors);

        changeMovieUrlToCastCrew();
        List<String> directorUrls = grabCastOrCrewFromFilmweb(Connection.MOVIE_CLASS_CAST_FIELDS_MAP_FILMWEB_KEYS.get(Movie.DIRECTORS));
        List<Actor> directors = createActorsFromFilmwebLinks(directorUrls, allActors);
        movie.addDirectors(directors);

        List<String> writerUrls = grabCastOrCrewFromFilmweb(Connection.MOVIE_CLASS_CAST_FIELDS_MAP_FILMWEB_KEYS.get(Movie.WRITERS));
        List<Actor> writers = createActorsFromFilmwebLinks(writerUrls, allActors);
        movie.addWriters(writers);

        return movie;
    }









    private static String replaceNullWithDash(String object) {
        return object == null ? "-" : object;
    }

    private static String replaceAcutesHTML(String str) {
        if(str == null) return null;
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
