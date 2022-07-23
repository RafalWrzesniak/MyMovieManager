package Internet;

import Errors.MovieNotFoundException;
import FileOperations.StringFunctions;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebOperations {

    public static final String FILMWEB = "https://www.filmweb.pl";
    public static final String ITEMPROP = "itemprop";
    private final static FilmwebClientMovie filmwebClientMovie = FilmwebClientMovie.getInstance();

    public static Document parseUrl(URL websiteUrl) throws IOException {
        return Jsoup.parse(websiteUrl, 3000);
    }

    public static String getAttrValueFromElementWithItemProp(Document parsedUrl, String itemprop, String attribute) {
        Element element = parsedUrl.getElementsByAttributeValue(ITEMPROP, itemprop).first();
        return element != null ? element.attributes().get(attribute) : null;
    }

    public static String getTextValueFromElementWithItemProp(Document parsedUrl, String itemprop) {
        Element element = parsedUrl.getElementsByAttributeValue(ITEMPROP, itemprop).first();
        return element != null ? element.text() : null;
    }

    public static JsonNode getFieldFromObjectWithAttributeKeyAndValue(Document parsedUrl, String field, String key, String match) {
        log.debug("Trying to fetch field '{}' with key '{}' by match '{}'", field, key, match);
        Elements dataSource = parsedUrl.getElementsByAttributeValueContaining(key, match);
        log.debug(dataSource.toString());
        return StringFunctions.parseString(dataSource.first().data()).get(field);
    }

    public static Optional<URL> createFilmwebQueryFrom(String query) {
        try {
            return Optional.of(new URL(FILMWEB.concat("/films/search?q=").concat(URLEncoder.encode(query, "UTF-8"))));
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static Map<URL, String> findTitlesAndItsLinksFromQuery(URL filmwebQuery) throws IOException {
        Document parsedUrl = parseUrl(filmwebQuery);
        return parsedUrl.getElementsByAttributeValue("class", "filmPreview__link").stream()
                .limit(5)
                .collect(Collectors.toMap(
                        element -> createUrlFromString(FILMWEB.concat(element.attributes().get("href"))).orElse(null),
                        element -> element.getElementsByAttributeValue(ITEMPROP, "name").first().text()
                ));
    }

    public static Optional<URL> createUrlFromString(String string) {
        try {
            return Optional.of(new URL(string));
        } catch (MalformedURLException e) {
            return Optional.empty();
        }
    }

    public static URL getMostSimilarTitleUrlFromQuery(String desiredTitle) throws IOException, MovieNotFoundException {
        Map.Entry<URL, String> chosenEntry = null;
        URL query = createFilmwebQueryFrom(desiredTitle).orElse(null);
        Map<URL, String> foundTitleAndLinkMap = findTitlesAndItsLinksFromQuery(query);
        if(foundTitleAndLinkMap == null || foundTitleAndLinkMap.size() == 0) return null;
        double highestCorrelation = -1;

        for(Map.Entry<URL, String> titleWithLinkPairToCheck : foundTitleAndLinkMap.entrySet()) {
            double correlation = StringFunctions.calculateStringsCorrelation(desiredTitle, titleWithLinkPairToCheck.getValue());
            if(correlation > highestCorrelation) {
                highestCorrelation = correlation;
                chosenEntry = titleWithLinkPairToCheck;
            }
            // if two movies has the same title but was released in different years
            Matcher titleWithYear = Pattern.compile(".+?\\((\\d{4})\\)$").matcher(desiredTitle);
            if(hasSameCorrelationAndYearIsGiven(correlation, highestCorrelation, titleWithYear) && chosenEntry != null) {
                int desireYear = Integer.parseInt(titleWithYear.group(1));
                chosenEntry = chooseEntryMatchedWithGivenYear(chosenEntry, titleWithLinkPairToCheck, desireYear);
            }
        }
        logChoosingBestTitleResult(chosenEntry, desiredTitle, highestCorrelation);
        return (chosenEntry != null && Math.round(highestCorrelation*100) > 65) ? chosenEntry.getKey() : null;
    }

    private static boolean hasSameCorrelationAndYearIsGiven(double correlation, double highestCorrelation, Matcher matcher) {
        return correlation == highestCorrelation && matcher.find();
    }

    private static Map.Entry<URL, String> chooseEntryMatchedWithGivenYear(Map.Entry<URL, String> chosenEntry, Map.Entry<URL, String> titleWithLinkPairToCheck, int desireYear) throws IOException, MovieNotFoundException {
        int pretendedTitleYear = StringFunctions.convertStrToLocalDate(filmwebClientMovie.getPremiere(parseUrl(titleWithLinkPairToCheck.getKey()))).getYear();
        if(pretendedTitleYear == desireYear) {
            return titleWithLinkPairToCheck;
        }
        return chosenEntry;
    }

    private static void logChoosingBestTitleResult(Map.Entry<URL, String> chosenEntry, String desiredTitle, double highestCorrelation) {
        String result = String.format("\"%s\" with correlation: %d%%", chosenEntry != null ? chosenEntry.getValue() : null, Math.round(highestCorrelation * 100));
        String fullResultMessage;
        if(Math.round(highestCorrelation*100) > 65) {
            fullResultMessage = String.format("For query \"%s\" there was found %s", desiredTitle, result);
            log.info(fullResultMessage);
        } else {
            fullResultMessage = String.format("For query \"%s\" there was found %s. Low correlation. Possibility of wrong movie assigning. Aborting.", desiredTitle, result);
            log.warn(fullResultMessage);
        }
    }

    public static boolean downloadImage(URL fromImageUrl, Path toFileName) {
        if(fromImageUrl == null || toFileName == null) return false;
        try (InputStream inputStream = fromImageUrl.openStream()) {
            Files.copy(inputStream, toFileName, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.warn("Failed to download image from \"{}\"", fromImageUrl);
            return false;
        }
        log.debug("Image downloaded from \"{}\"", fromImageUrl);
        return true;
    }

}
