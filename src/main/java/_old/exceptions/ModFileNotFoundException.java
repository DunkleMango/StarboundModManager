package _old.exceptions;

public class ModFileNotFoundException extends Exception {
    private String name;

    public ModFileNotFoundException(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
