package Internet;

import Errors.MovieNotFoundException;
import MoviesAndActors.Movie;
import MoviesAndActors.dto.ActorDto;
import MoviesAndActors.dto.MovieDto;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

import static FileOperations.StringFunctions.parseString;
import static FileOperations.StringFunctions.slashed;
import static org.springframework.web.reactive.function.client.WebClient.create;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImdbClient {

    private static final String API_ACTOR = "Name";
    private static final String API_MOVIE = "Title";
    private static final String API_SEARCH_MOVIE = "SearchMovie";
    private static final String API_KEY = "k_zki1rd0q";
    private final WebClient client = create("https://imdb-api.com/pl/API/");

    @Getter
    private static final ImdbClient instance = new ImdbClient();

    public MovieDto getMovieById(String id) {
        return retrievePathFromApi(API_MOVIE, id)
                .bodyToMono(MovieDto.class)
                .block();
    }

    public ActorDto getActorById(String id) {
        return retrievePathFromApi(API_ACTOR, id)
                .bodyToMono(ActorDto.class)
                .block();
    }

    public MovieDto findBestMovieForSearchByTitle(String title) throws MovieNotFoundException {
        String id = findPossibleMoviesByTitle(title).findValue(Movie.ID).textValue();
        return getMovieById(id);//.withTitlePl(title);
    }

    public JsonNode findPossibleMoviesByTitle(String title) {
        String rawData = retrievePathFromApi(API_SEARCH_MOVIE, title)
                .bodyToMono(String.class)
                .block();
        return parseString(rawData);
    }

    private WebClient.ResponseSpec retrievePathFromApi(String API, String path) {
        return client.get().uri(slashed(API) + slashed(API_KEY) + slashed(path)).retrieve();
    }

}
