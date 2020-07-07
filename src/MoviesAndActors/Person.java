package MoviesAndActors;

import java.time.LocalDate;

public class Person {
    private final String name;
    private final String surname;
    private final String nationality;
    private final LocalDate birthday;
    private final int age;

    public Person(String name, String surname, String nationality, LocalDate birthday) {
        this.name = name;
        this.surname = surname;
        this.nationality = setNationality(nationality);
        this.birthday = setBirthday(birthday);
        this.age = setAge();
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getNationality() {
        return nationality;
    }


    public LocalDate getBirthday() {
        return birthday;
    }

    public int getAge() {
        return age;
    }

    private String setNationality(String nationality) {
        if(nationality == null) {
            throw new IllegalArgumentException("Nationality argument cannot be null!");
        } else if(nationality.isEmpty()) {
            throw new IllegalArgumentException("Nationality argument cannot be empty!");
        } else {
            for (char aChar : nationality.toCharArray()) {
                if (((aChar < 65 || (aChar > 90 && aChar < 97) || aChar > 122) && aChar != 20)) {
                    throw new IllegalArgumentException(String.format("Nationality argument contains illegal char: '%s'", aChar));
                }
            }
        }
        return nationality;
    }

    private LocalDate setBirthday(LocalDate birthday) {
        if(birthday == null) {
            throw new IllegalArgumentException("Birthday argument cannot be null!");
        } else {
            return birthday;
        }
    }

    private int setAge() {
        return LocalDate.now().minusYears(getBirthday().getYear()).getYear();
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", nationality='" + nationality + '\'' +
                ", birthday=" + birthday +
                ", age=" + age +
                '}';
    }


}
