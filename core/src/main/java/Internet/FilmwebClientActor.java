package Internet;

import FileOperations.StringFunctions;
import MoviesAndActors.Actor;
import MoviesAndActors.builder.ActorBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;

import static Internet.WebOperations.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmwebClientActor {

    @Getter
    private static final FilmwebClientActor instance = new FilmwebClientActor();

    public Actor createActorFromFilmweb(URL actorUrl) throws IOException {
        Document parsedUrl = WebOperations.parseUrl(actorUrl);
        return ActorBuilder.builder()
                .createId()
                .fullName(getFullName(parsedUrl))
                .filmweb(actorUrl)
                .birthday(StringFunctions.convertStrToLocalDate(getBirthday(parsedUrl)))
                .deathDay(StringFunctions.convertStrToLocalDate(getDeathDay(parsedUrl)))
                .nationality(getNationality(parsedUrl))
                .imageUrl(createUrlFromString(getImageUrl(parsedUrl)).orElse(null))
                .build();
    }

    private String getFullName(Document parsedUrl) {
        String fullName = getTextValueFromElementWithItemProp(parsedUrl, "name");
        return (fullName != null && fullName.contains(" ")) ? fullName : getAdditionalName(parsedUrl);
    }

    private String getAdditionalName(Document parsedUrl) {
        return getTextValueFromElementWithItemProp(parsedUrl, "additionalName");
    }

    private String getNationality(Document parsedUrl) {
        String birthdayString = getTextValueFromElementWithItemProp(parsedUrl, "birthplace");
        if(birthdayString == null) return null;
        String[] fullBirthPlace = birthdayString.split(", ");
        return fullBirthPlace[fullBirthPlace.length-1];
    }

    private String getImageUrl(Document parsedUrl) {
        return getAttrValueFromElementWithItemProp(parsedUrl, "image", "src");
    }

    private String getBirthday(Document parsedUrl) {
        return getAttrValueFromElementWithItemProp(parsedUrl, "birthDate", "content");
    }

    private String getDeathDay(Document parsedUrl) {
        return getAttrValueFromElementWithItemProp(parsedUrl, "deathDate", "data-death-date");
    }

}
