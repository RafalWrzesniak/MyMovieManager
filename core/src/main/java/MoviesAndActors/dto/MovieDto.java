package MoviesAndActors.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieDto {

    @With
    private String titlePl;
    private String title;
    private LocalDate releaseDate;
    private Integer runtimeMins;
    private double imDbRating;
    private Integer imDbRatingVotes;

    private List<HashMap<String, String>> actorList;
//    private List<HashMap<String, String>> genreList;
//    private List<HashMap<String, String>> countryList;
    private List<HashMap<String, String>> directorList;
    private List<HashMap<String, String>> writerList;

    private URL image;
    private String plotLocal;
}
