package MoviesAndActors.dto;

import lombok.Data;

import java.net.URL;
import java.time.LocalDate;

@Data
public class ActorDto {

    private String name;
    private LocalDate birthDate;
    private LocalDate deathDate;
    private URL image;

}
