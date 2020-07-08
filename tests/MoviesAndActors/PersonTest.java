package MoviesAndActors;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class PersonTest {

    static Collection<Object[]> stringParams() {
        return Arrays.asList(new Object[][]{
                {"Rafał", "Wrześniak", "Poland", LocalDate.of(1994, 8, 11), "E:\\xInne\\me.jpg", 26, 0},
                {"Steven", "Spielberg", "USA", LocalDate.of(1946, 12, 18), "E:\\xInne\\ss.jpg", 74, 1},
                {"Cezary", "Pazura", "Poland", LocalDate.of(1962, 6, 13), "E:\\xInne\\cp.jpg", 58, 2},
                {"Jeniffer", "Aniston", "USA", LocalDate.of(1969, 2, 11), "E:\\xInne\\ja.jpg", 51, 3},
                {"Tom", "Hanks", "USA", LocalDate.of(1956, 7, 9), "E:\\xInne\\th.jpg", 64, 4}
        });
    }


    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getName(String name, String surname, String nationality, LocalDate birthday, String imagePath) {
        Person person = new Actor(name, surname, nationality, birthday, imagePath);
        assertEquals(name, person.getName());
    }

    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getSurname(String name, String surname, String nationality, LocalDate birthday, String imagePath) {
        Person person = new Actor(name, surname, nationality, birthday, imagePath);
        assertEquals(surname, person.getSurname());
    }

    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getNationality(String name, String surname, String nationality, LocalDate birthday, String imagePath) {
        Person person = new Actor(name, surname, nationality, birthday, imagePath);
        assertEquals(nationality, person.getNationality());
    }

    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getBirthday(String name, String surname, String nationality, LocalDate birthday, String imagePath) {
        Person person = new Actor(name, surname, nationality, birthday, imagePath);
        assertEquals(birthday, person.getBirthday());
    }

    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getAge(String name, String surname, String nationality, LocalDate birthday, String imagePath, int age) {
        Person person = new Actor(name, surname, nationality, birthday, imagePath);
        assertEquals(age, person.getAge());
    }

    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getPersonId(String name, String surname, String nationality, LocalDate birthday, String imagePath, int age, int id) {
        Person person = new Actor(name, surname, nationality, birthday, imagePath);
        assertEquals(id, person.getPersonId());
    }

    @ParameterizedTest(name = "#{index} - Test with Argument = {arguments}")
    @MethodSource("stringParams")
    void getImagePath(String name, String surname, String nationality, LocalDate birthday, String imagePath) {
        Person person = new Actor(name, surname, nationality, birthday, imagePath);
        assertEquals(imagePath, person.getImagePath());
    }

    @org.junit.jupiter.api.Test
    void checkForNullOrEmptyOrIllegalChar() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> Person.checkForNullOrEmptyOrIllegalChar(null, "Name"));
        assertEquals("Name argument cannot be null!", exception.getMessage());

        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class,
                () -> Person.checkForNullOrEmptyOrIllegalChar("", "Surname"));
        assertEquals("Surname argument cannot be empty!", exception2.getMessage());

        IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class,
                () -> Person.checkForNullOrEmptyOrIllegalChar("as[d", "Nationality"));
        assertEquals("Nationality argument contains illegal char: '['", exception3.getMessage());
    }

    @org.junit.jupiter.api.Test
    void testEquals() {
        Actor person = new Actor("Jack", "Sparrow", "Karaibian", LocalDate.of(1957, 6, 2), "E:\\xInne\\dk.jpg");
        Actor person2 = new Actor("Jack", "Sparrow", "Karaibian", LocalDate.of(1957, 6, 2), "E:\\xInne\\dk.jpg");
        assertEquals(person, person2);

    }

    @org.junit.jupiter.api.Test
    void testToString() {
        Actor person = new Actor("Jack", "Sparrow", "Karaibian", LocalDate.of(1957, 6, 2), "E:\\xInne\\dk.jpg");

        String expected = "Person{personId=" + person.getPersonId() + ", name='Jack', surname='Sparrow', nationality='Karaibian', birthday=1957-06-02, age=63}";
        assertEquals(expected, person.toString());
    }
}