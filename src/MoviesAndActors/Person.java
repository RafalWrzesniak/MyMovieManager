package MoviesAndActors;

import java.time.LocalDate;

public abstract class Person {
    private final int personId;
    private final String name;
    private final String surname;
    private final String nationality;
    private final LocalDate birthday;
    private final int age;
    private final String imagePath;
    private static int classId = 0;

    public Person(String name, String surname, String nationality, LocalDate birthday, String imagePath) {
        this.name = checkForNullOrEmptyOrIllegalChar(name, "Name");
        this.surname = checkForNullOrEmptyOrIllegalChar(surname, "Surname");
        this.nationality = checkForNullOrEmptyOrIllegalChar(nationality, "Nationality");
        this.birthday = setBirthday(birthday);
        this.age = setAge();
        this.imagePath = checkForNullOrEmptyOrIllegalChar(imagePath, "ImagePath");
        this.personId = classId;
        classId++;
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

    public int getPersonId() {
        return personId;
    }

    public String getImagePath() {
        return imagePath;
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

    public static String checkForNullOrEmptyOrIllegalChar(String stringToCheck, String argName) {
        if(stringToCheck == null) {
            throw new IllegalArgumentException(String.format("%s argument cannot be null!", argName));
        } else if(stringToCheck.isEmpty()) {
            throw new IllegalArgumentException(String.format("%s argument cannot be empty!", argName));
        }
        for (char aChar : stringToCheck.toCharArray()) {
            if (((aChar < 65 || (aChar > 90 && aChar < 96) || (aChar > 122 && aChar < 192))
                    && aChar != 20 && aChar != 39 && aChar != 44 && aChar != 46 && aChar != 47 && aChar != 58 && aChar != 92)) {
                throw new IllegalArgumentException(String.format("%s argument contains illegal char: '%s'", argName, aChar));
            }
        }
        return stringToCheck;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        Person person = (Person) o;
        return getName().equals(person.getName()) &&
                getSurname().equals(person.getSurname()) &&
                getBirthday().equals(person.getBirthday());
    }

    @Override
    public String toString() {
        return "Person{" +
                "personId=" + getPersonId() +
                ", name='" + getName() + '\'' +
                ", surname='" + getSurname() + '\'' +
                ", nationality='" + getNationality() + '\'' +
                ", birthday=" + getBirthday() +
                ", age=" + getAge() +
                '}';
    }
}
