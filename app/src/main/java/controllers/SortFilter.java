package controllers;

import MoviesAndActors.Actor;
import MoviesAndActors.ContentList;
import MoviesAndActors.ContentType;
import MoviesAndActors.Movie;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.RangeSlider;

import java.net.URL;
import java.time.temporal.ValueRange;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Slf4j
public final class SortFilter<T extends ContentType> implements Initializable {

//    == fields ==
    @FXML public HBox filterHBox;
    @FXML private MenuButton genresMenu, productionMenu, filtersMenu;
    @FXML private CustomMenuItem actorCustomCountry, movieCustomGenre, moreFilters, movieCustomProduction;
    @FXML private Label actorAgeLowValue, actorAgeHighValue, moviePremiereHigh, moviePremiereLow, movieDurationLow,
            movieDurationHigh, movieRateLow, movieRateHigh, movieRateCountLow, movieRateCountHigh;
    @FXML private RangeSlider actorAgeSlider, moviePremiere, movieDuration, movieRate, movieRateCount;
    @FXML private GridPane actorCountriesGrid, movieGenreGrid, movieProductionGrid;

    @Setter private MainController mainController;
    @Getter private ContentList<T> contentList;
    @Getter private List<T> filteredList;

    private List<String> actorFilteredCountries, movieFilteredGenres, movieFilteredProduction;
    private Comparator<T> chosenSorter;
    private ResourceBundle resourceBundle;
    private static String lastChosenSortId;


//    == init ==
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resourceBundle = resources;
        filteredList = new ArrayList<>();
        actorFilteredCountries = new ArrayList<>();
        movieFilteredGenres = new ArrayList<>();
        movieFilteredProduction = new ArrayList<>();
        if(actorCustomCountry != null) actorCustomCountry.setHideOnClick(false);
        if(movieCustomGenre != null) movieCustomGenre.setHideOnClick(false);
        if(moreFilters != null) moreFilters.setHideOnClick(false);
        if(movieCustomProduction != null) movieCustomProduction.setHideOnClick(false);

        if(productionMenu != null) productionMenu.showingProperty().addListener(borderButtonListener(productionMenu));
        if(genresMenu != null) genresMenu.showingProperty().addListener(borderButtonListener(genresMenu));
        if(filtersMenu != null) filtersMenu.showingProperty().addListener(borderButtonListener(filtersMenu));
    }


//    == methods ==
    public void setContentList(ContentList<T> contentList) {
        log.info("Created SortFilter object for \"{}\"", contentList);
        if(contentList == null || contentList.get(0) == null) return;
        this.filteredList = contentList.getList();
        this.contentList = contentList;

        if(contentList.get(0) instanceof Actor) {
            addActorListeners();
            handleActorObjects();
            if(lastChosenSortId == null) lastChosenSortId = mainController.getActorAlpha().getId();
        } else if(contentList.get(0) instanceof Movie) {
            addMovieListeners();
            handleMovieObjects();
            if(lastChosenSortId == null) lastChosenSortId = mainController.getMovieAlpha().getId();
        }

        // apply last chosen sorter
        sortItemsBy(lastChosenSortId);

        // sorting
        mainController.getSort().getItems().forEach(item -> item.setOnAction(event -> {
            sortItemsBy(item.getId());
            mainController.getSort().setText(item.getText());
        }));

        mainController.getSort().setText(resourceBundle.getString("main_view.sort.type"));
        mainController.getSort().getItems().forEach(item -> {
            item.setVisible(false);
            if(filteredList.get(0) instanceof Movie) {
                if(item.getId().startsWith("movie")) {
                    item.setVisible(true);
                }
            } else if(filteredList.get(0) instanceof Actor) {
                 if(item.getId().startsWith("actor")) {
                    item.setVisible(true);
                }
            }
        });


    }


    private void handleMovieObjects() {
        // noinspection unchecked
        ContentList<Movie> movieList = (ContentList<Movie>) contentList;

        moviePremiere.setMin(movieList.getList().stream().min(Movie.COMP_PREMIERE).orElseGet(() -> movieList.get(0)).getPremiere().getYear());
        moviePremiere.setMax(movieList.getList().stream().max(Movie.COMP_PREMIERE).orElseGet(() -> movieList.get(0)).getPremiere().getYear());
        moviePremiere.setLowValue(moviePremiere.getMin());
        moviePremiere.setHighValue(moviePremiere.getMax());

        movieDuration.setMin(movieList.getList().stream().min(Movie.COMP_DURATION).orElseGet(() -> movieList.get(0)).getDuration());
        movieDuration.setMax(movieList.getList().stream().max(Movie.COMP_DURATION).orElseGet(() -> movieList.get(0)).getDuration());
        movieDuration.setLowValue(movieDuration.getMin());
        movieDuration.setHighValue(movieDuration.getMax());

        movieRate.setMin(movieList.getList().stream().min(Movie.COMP_RATE).orElseGet(() -> movieList.get(0)).getRate());
        movieRate.setMax(movieList.getList().stream().max(Movie.COMP_RATE).orElseGet(() -> movieList.get(0)).getRate());
        movieRate.setLowValue(movieRate.getMin());
        movieRate.setHighValue(movieRate.getMax());

        movieRateCount.setMin(movieList.getList().stream().min(Movie.COMP_POPULARITY).orElseGet(() -> movieList.get(0)).getRateCount());
        movieRateCount.setMax(movieList.getList().stream().max(Movie.COMP_POPULARITY).orElseGet(() -> movieList.get(0)).getRateCount());
        movieRateCount.setLowValue(movieRateCount.getMin());
        movieRateCount.setHighValue(movieRateCount.getMax());

        SortedSet<String> genresSet = new TreeSet<>();
        SortedSet<String> productionSet = new TreeSet<>();
        for (Movie movie : movieList.getList()) {
            for(String genre : movie.getGenres()) {
                if(genre != null && !genre.equals("-")) {
                    genresSet.add(genre);
                }
            }
            for(String production : movie.getProduction()) {
                if(production != null && !production.equals("-")) {
                    productionSet.add(production);
                }
            }
        }
        addSetToCustomGridPane(genresSet, movieFilteredGenres, movieGenreGrid);
        addSetToCustomGridPane(productionSet, movieFilteredProduction, movieProductionGrid);
    }

    private void handleActorObjects() {
        SortedSet<String> countries = new TreeSet<>();
        int minAge = 100, maxAge = 0;
        for (ContentType obj : contentList.getList()) {
            if(obj instanceof Actor) {
                Actor actor = (Actor) obj;
                if (actor.getNationality() != null && !actor.getNationality().equals("-")) {
                    countries.add(actor.getNationality());
                }
                if (actor.getAge() > maxAge) maxAge = actor.getAge();
                if (actor.getAge() < minAge) minAge = actor.getAge();
            }
        }

        addSetToCustomGridPane(countries, actorFilteredCountries, actorCountriesGrid);
        actorAgeSlider.setMin(minAge);
        actorAgeSlider.setLowValue(minAge);
        actorAgeSlider.setMax(maxAge);
        actorAgeSlider.setHighValue(maxAge);

    }

    private void addMovieListeners() {
        moviePremiere.setOnMouseReleased(mouseEvent -> applyFilters());
        moviePremiere.lowValueProperty().addListener((observableValue, number, t1) ->
                moviePremiereLow.setText(String.valueOf(Math.round((Double) t1))));
        moviePremiere.highValueProperty().addListener((observableValue, number, t1) ->
                moviePremiereHigh.setText(String.valueOf(Math.round((Double) t1))));

        movieDuration.setOnMouseReleased(mouseEvent -> applyFilters());
        movieDuration.lowValueProperty().addListener((observableValue, number, t1) ->
                movieDurationLow.setText(String.format("%sh %smin", Math.round((double) (t1))/60, Math.round((double) t1)%60)));
        movieDuration.highValueProperty().addListener((observableValue, number, t1) ->
                movieDurationHigh.setText(String.format("%sh %smin", Math.round((double) (t1))/60, Math.round((double) t1)%60)));

        movieRate.setOnMouseReleased(mouseEvent -> applyFilters());
        movieRate.lowValueProperty().addListener((observableValue, number, t1) ->
                movieRateLow.setText(String.format(Locale.US ,"%.2f", (double) t1)));
        movieRate.highValueProperty().addListener((observableValue, number, t1) ->
                movieRateHigh.setText(String.format(Locale.US ,"%.2f", (double) t1)));

        movieRateCount.setOnMouseReleased(mouseEvent -> applyFilters());
        movieRateCount.lowValueProperty().addListener((observableValue, number, t1) ->
                movieRateCountLow.setText(String.valueOf(Math.round((Double) t1))));
        movieRateCount.highValueProperty().addListener((observableValue, number, t1) ->
                movieRateCountHigh.setText(String.valueOf(Math.round((Double) t1))));
    }


    private void addActorListeners() {
        actorAgeSlider.setOnMouseReleased(mouseEvent -> applyFilters());

        actorAgeSlider.lowValueProperty().addListener((observableValue, number, t1) ->
                actorAgeLowValue.setText(String.valueOf(Math.round((Double) t1))));
        actorAgeSlider.highValueProperty().addListener((observableValue, number, t1) ->
                actorAgeHighValue.setText(String.valueOf(Math.round((Double) t1))));
    }

    public Predicate<T> predicate() {
        if(contentList.getList() == null || contentList.getList().size() == 0 || contentList.getList().get(0) == null) {
            return content -> true;
        }
        if(contentList.getList().get(0) instanceof Actor) {
            String age = String.format("[%d, %d]", Math.round(actorAgeSlider.getLowValue()), Math.round(actorAgeSlider.getHighValue()));
            String countries = actorFilteredCountries.size() == 0 ? "all" : actorFilteredCountries.toString();
            log.info("Current filter for \"{}\" is age: {} and countries: {}", contentList, age, countries);
            return content -> {
                Actor actor = (Actor) content;
                return actor.getAge() >= Math.round(actorAgeSlider.getLowValue()) &&
                       actor.getAge() <= Math.round(actorAgeSlider.getHighValue()) &&
                       (actorFilteredCountries.isEmpty() || actorFilteredCountries.contains(actor.getNationality()));
            };

        } else if(contentList.getList().get(0) instanceof Movie) {
            String premiereInfo = String.format("[%d, %d]", Math.round(moviePremiere.getLowValue()), Math.round(moviePremiere.getHighValue()));
            String durationInfo = String.format("[%d, %d]", Math.round(movieDuration.getLowValue()), Math.round(movieDuration.getHighValue()));
            String rateInfo = String.format(Locale.US, "[%.2f, %.2f]", movieRate.getLowValue(), movieRate.getHighValue());
            String rateCountInfo = String.format("[%d, %d]", Math.round(movieRateCount.getLowValue()), Math.round(movieRateCount.getHighValue()));
            String productionsInfo = movieFilteredProduction.size() == 0 ? "all" : movieFilteredProduction.toString();
            String genresInfo = movieFilteredGenres.size() == 0 ? "all" : movieFilteredGenres.toString();
            log.info("Current filter for \"{}\" is premiere: {}, genres: {}, duration: {}, rate: {}, rateCount: {}, production: {}",
                     contentList, premiereInfo, genresInfo, durationInfo, rateInfo, rateCountInfo, productionsInfo);

            return content -> {
                Movie movie = (Movie) content;
                return ValueRange.of(Math.round(moviePremiere.getLowValue()), Math.round(moviePremiere.getHighValue())).isValidIntValue(movie.getPremiere().getYear()) &&
                       ValueRange.of(Math.round(movieDuration.getLowValue()), Math.round(movieDuration.getHighValue())).isValidIntValue(movie.getDuration()) &&
                       ValueRange.of(Math.round(movieRate.getLowValue()*100), Math.round(movieRate.getHighValue()*100)).isValidIntValue(Math.round(movie.getRate()*100)) &&
                       ValueRange.of(Math.round(movieRateCount.getLowValue()), Math.round(movieRateCount.getHighValue())).isValidIntValue(movie.getRateCount()) &&
                       (movieFilteredGenres.isEmpty() || movie.getGenres().stream().anyMatch(genre -> movieFilteredGenres.contains(genre))) &&
                       (movieFilteredProduction.isEmpty() || movie.getProduction().stream().anyMatch(production -> movieFilteredProduction.contains(production)));
            };

        }
        return content -> true;
    }


    private void applyFilters() {
        filteredList = contentList.getList().stream().filter(predicate()).collect(Collectors.toList());
        Platform.runLater(() -> mainController.displayPanesByPopulating(true));
    }


    private void addSetToCustomGridPane(Set<String> givenSet, List<String> filteredOut, GridPane gridPane) {
        Iterator<String> iterator = givenSet.iterator();
        for(int row = 0; row < 20; row++) {
            for(int column = 0; column < 4; column++) {
                if(iterator.hasNext()) {
                    String genre = iterator.next();
                    CheckBox checkBox = new  CheckBox(genre);
                    checkBox.hoverProperty().addListener((observableValue, aBoolean, t1) -> {
                        if(t1) checkBox.setStyle("-fx-text-fill: my_blue;");
                        else checkBox.setStyle("-fx-text-fill: almost_white;");
                    });
                    checkBox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
                        if(t1) filteredOut.add(genre);
                        else filteredOut.remove(genre);
                        applyFilters();
                    });
                    gridPane.add(checkBox, column, row);
                } else {
                    break;
                }
            }
        }
    }

    private void sortItemsBy(String itemId) {
        if(contentList.get(0) instanceof Movie) {
            // noinspection unchecked cast
            chosenSorter = (Comparator<T>) mainController.getSortMovieMap().get(itemId);
        } else if(contentList.get(0) instanceof Actor) {
            // noinspection unchecked cast
            chosenSorter = (Comparator<T>) mainController.getSortActorMap().get(itemId);
        }
        lastChosenSortId = itemId;
        log.debug("Now sorting by \"{}\"", itemId);
        filteredList.sort(chosenSorter);
        mainController.displayPanesByPopulating(true);
    }


    public static ChangeListener<? super Boolean> borderButtonListener(MenuButton menuButton) {
        return (observableValue, aBoolean, t1) -> {
            if(t1) {
                menuButton.getStyleClass().add("buttonPressed");
            } else {
                menuButton.getStyleClass().remove("buttonPressed");
            }
        };
    }

}
