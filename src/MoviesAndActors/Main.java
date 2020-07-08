package MoviesAndActors;

import java.time.LocalDate;


public class Main {

    public static void main(String[] args) {
        System.out.println("Hello there!");
        Actor person = new Actor("Jack", "Sparrow", "Karaibian", LocalDate.of(1957, 6, 2), "E:\\xInne\\dk.jpg");
        Person person2 = new Actor("Jack", "Sparrow", "Karaibian", LocalDate.of(1957, 6, 2), "E:\\xInne\\dk.jpg");
        Person person3 = new Actor("John", "Wick", "USA", LocalDate.of(1977, 2, 22), "E:\\xInne\\dk.jpg");

        person.addMovieActorPlayedIn(30);
        person.addMovieActorPlayedIn(421);
        System.out.println(person.getAllMoviesActorPlayedIn().toString());
        if(!person.addMovieActorPlayedIn(30)) {
            System.out.println("Nie udało się");
        }
        System.out.println(person.equals(person2));
        System.out.println(person.equals(person3));

        Director director = new Director("John", "Wick", "USA", LocalDate.of(1977, 2, 22), "E:\\xInne\\dk.jpg");
        director.addMovieDirectedBy(40);
        director.addMovieDirectedBy(275);
        director.addMovieDirectedBy(124);
        System.out.println(director.isDirecting(40));
        System.out.println(director.getAllMoviesDirectedBy().toString());
        director.addSeveralMoviesToActor(person.getAllMoviesActorPlayedIn());
        System.out.println(director.getAllMoviesActorPlayedIn().toString());

    }

}

