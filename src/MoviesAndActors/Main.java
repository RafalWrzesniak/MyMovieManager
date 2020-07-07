package MoviesAndActors;

import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello there!");
        Person person = new Person("Jack", "Sparrow", "Karaibian", LocalDate.of(1957, 6, 2));
        System.out.println(person.getAge());
        System.out.println(person.toString());

    }
}
