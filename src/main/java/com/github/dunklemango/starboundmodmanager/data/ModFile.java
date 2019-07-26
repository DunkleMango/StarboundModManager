package com.github.dunklemango.starboundmodmanager.data;

import com.github.dunklemango.starboundmodmanager.exceptions.ModFileGenerationException;

import java.io.File;
import java.util.Date;

public class ModFile {

    private final File file;
    private final Date date;
    private String name;

    public ModFile(File file, String name) throws ModFileGenerationException {
        this.file = file;
        this.date = generateDate(file);
        this.name = name;
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
        return name.hashCode();
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

    public String getName() {
        return name;
    }
}
