package _old.data;

import _old.exceptions.ModFileGenerationException;

import java.io.File;
import java.util.Date;

public class ModFile {
    public static final String MOD_FILE_EXTENSION = ".pak";
    private final File file;
    private final Date date;
    private Integer id;

    public ModFile(File file, Integer id) throws ModFileGenerationException {
        this.file = file;
        this.date = generateDate(file);
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("{%s, %s, %s}", this.file, this.date, this.id);
    }

    private Date generateDate(File path) throws ModFileGenerationException {
        File target = path;
        if (path.isDirectory()) {
            File[] content = path.listFiles((subDir, name) -> name.toLowerCase().endsWith(".pak"));
            if (content.length == 0) throw new ModFileGenerationException();
            target = content[0];
        }
        return new Date(target.lastModified());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(this.getClass())) return false;
        ModFile otherModFile = (ModFile) obj;
        return otherModFile.hashCode() == this.hashCode();
    }

    @Override
    public int hashCode() {
        return id;
    }

    public boolean isNewerThan(ModFile modFile) {
        return date.after(modFile.getDate());
    }

    public File getFile() {
        return file;
    }

    public Date getDate() {
        return date;
    }

    public Integer getId() {
        return id;
    }
}
