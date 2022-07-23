package Internet;

import Errors.MovieNotFoundException;
import FileOperations.StringFunctions;
import MoviesAndActors.Movie;
import MoviesAndActors.builder.MovieBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static Internet.WebOperations.*;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmwebClientMovie {

    @Getter
    private static final FilmwebClientMovie instance = new FilmwebClientMovie();

    public Movie createMovieFromUrl(URL movieUrl) throws MovieNotFoundException, IOException {
        Document parsedUrl = parseUrl(movieUrl);
        return MovieBuilder.builder()
                .createId()
                .filmweb(movieUrl)
                .title(getTitle(parsedUrl))
                .titleOrg(getTitleOrg(parsedUrl))
                .premiere(StringFunctions.convertStrToLocalDate(getPremiere(parsedUrl)))
                .duration(Integer.parseInt(getDuration(parsedUrl)))
                .rate(getRate(parsedUrl))
                .rateCount(getRateCount(parsedUrl))
                .imageUrl(createUrlFromString(getImageUrl(parsedUrl)).orElse(null))
                .description(getDescription(parsedUrl))
                .genres(getGenres(parsedUrl))
                .production(getCountries(parsedUrl))
                .build();
    }

    private String getDuration(Document parsedUrl) {
        return getAttrValueFromElementWithItemProp(parsedUrl, "timeRequired", "data-duration");
    }

    private String getDescription(Document parsedUrl) {
        return parsedUrl.getElementsByAttributeValue(ITEMPROP, "description")
                .first()
                .text();
    }

    private String getTitle(Document parsedUrl) throws MovieNotFoundException {
        return getFromDataSourceObject(parsedUrl, "title").textValue();
    }

    private String getTitleOrg(Document parsedUrl) throws MovieNotFoundException {
        Element titleOrg = parsedUrl.getElementsByAttributeValue("class", "filmCoverSection__originalTitle").first();
        return titleOrg != null ? titleOrg.text() : getTitle(parsedUrl);
    }

    private double getRate(Document parsedUrl) {
        double rate = getFromFilmDataRatingObject(parsedUrl, "rate").doubleValue();
        return (double) (Math.round(rate * 100)) / 100;
    }

    private int getRateCount(Document parsedUrl) {
        return getFromFilmDataRatingObject(parsedUrl, "ratingCount").intValue();
    }

    public String getPremiere(Document parsedUrl) throws MovieNotFoundException {
        return getFromFilmDataRatingObject(parsedUrl, "releaseWorldString").textValue();
    }

    private List<String> getListOfGenresAndCountries(Document parsedUrl) {
        return parsedUrl.getElementsByAttributeValueContaining("href", "/ranking/film/")
                .stream()
                .map(Element::text)
                .collect(Collectors.toList());
    }

    public Set<String> getGenres(URL movieUrl) throws IOException {
        return getGenres(parseUrl(movieUrl));
    }

    private Set<String> getGenres(Document parsedUrl) {
        return Set.of(parsedUrl.getElementsByAttributeValue(ITEMPROP, "genre")
                .first()
                .text()
                .split(" / "));
    }

    public Set<String> getCountries(URL movieUrl) throws IOException {
        return getCountries(parseUrl(movieUrl));
    }

    private Set<String> getCountries(Document parsedUrl) {
        Set<String> foundGenres = getGenres(parsedUrl);
        return getListOfGenresAndCountries(parsedUrl).stream()
                .filter(s -> !foundGenres.contains(s))
                .collect(Collectors.toSet());
    }

    private String getImageUrl(Document parsedUrl) {
        return parsedUrl.getElementsByAttributeValue("id", "filmPoster")
                .first()
                .attributes()
                .get("content");
    }

    private JsonNode getFromDataSourceObject(Document parsedUrl, String field) throws MovieNotFoundException {
        JsonNode jsonNode;
        try {
           jsonNode = getFieldFromObjectWithAttributeKeyAndValue(parsedUrl, field, "data-source", "filmTitle");
        } catch (NullPointerException e) {
            log.warn("Cannot find object for field " + field);
            throw new MovieNotFoundException("Cannot find object for field " + field);
        }
        return jsonNode;
    }

    private JsonNode getFromFilmDataRatingObject(Document parsedUrl, String field) {
        return getFieldFromObjectWithAttributeKeyAndValue(parsedUrl, field, "id", "filmDataRating");
    }

    public List<URL> getCastLinks(URL movieUrl) throws IOException {
        URL movieCastUrl = createUrlFromString(movieUrl.toString().concat("/cast/actors")).orElse(null);
        return convertStrListToUrlList(getActorLinksAsStrings(movieCastUrl, "actors"));
    }

    public List<URL> getDirectorLinks(URL movieUrl) throws IOException {
        URL movieCastUrl = createUrlFromString(movieUrl.toString().concat("/cast/crew")).orElse(null);;
        return convertStrListToUrlList(getActorLinksAsStrings(movieCastUrl, "director"));
    }

    public List<URL> getWritersLinks(URL movieUrl) throws IOException {
        URL movieCastUrl = createUrlFromString(movieUrl.toString().concat("/cast/crew")).orElse(null);;
        return convertStrListToUrlList(getActorLinksAsStrings(movieCastUrl, "screenwriter"));
    }

    private List<String> getActorLinksAsStrings(URL movieCastUrl, String role) throws IOException {
        Document parsedUrl = parseUrl(movieCastUrl);
        return parsedUrl.getElementsByAttributeValueStarting("data-source", "role-").stream()
                .limit(10)
                .map(Element::data)
                .map(StringFunctions::parseString)
                .filter(jsonNode -> jsonNode.get("profession").textValue().equals(role))
                .map(jsonNode -> jsonNode.get("person"))
                .map(jsonNode -> jsonNode.get("link").textValue())
                .map(FILMWEB::concat)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private List<URL> convertStrListToUrlList(List<String> stringList) {
        List<URL> list = new ArrayList<>();
        for (String s : stringList) {
            list.add(new URL(s));
        }
        return list;
    }

}
