package utils;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class FileTreeUtils {

    /* Filter out all but plain files. */
    private static final FilenameFilter PLAIN_FILES =
            new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return new File(dir, name).isFile();
                }
            };

    /*  Returns a list of the names of all plain files in the directory DIR, in
     *  lexicographic order as Java Strings.  Returns null if DIR does
     *  not denote a directory. */
    public static List<String> plainFilenamesIn(File dir) {
        String[] files = dir.list(PLAIN_FILES);
        if (files == null) {
            return null;
        } else {
            Arrays.sort(files);
            return Arrays.asList(files);
        }
    }

    /*  Returns a list of the names of all plain files in the directory DIR, in
     *  lexicographic order as Java Strings.  Returns null if DIR does
     *  not denote a directory. */
    public static List<String> plainFilenamesIn(String dir) {
        return plainFilenamesIn(new File(dir));
    }

    /* OTHER FILE UTILITIES */

    /*  Return the concatentation of FIRST and OTHERS into a File designator,
     *  analogous to the {@link java.nio.file.Paths//#get(String, String[])}
     *  method. */
    public static File join(String first, String... others) {
        return Paths.get(first, others).toFile();
    }

    /*  Return the concatentation of FIRST and OTHERS into a File designator,
     *  analogous to the {@link java.nio.file.Paths//#get(String, String[])}
     *  method. */
    public static File join(File first, String... others) {
        return Paths.get(first.getPath(), others).toFile();
    }

    /*
    * Return the file from Path
    * for example, dir is CWD, fileName is a/b/c.txt
    * we return c.txt
    * */
    public static File getFileFromTreePath(File dir, String fileName) {
        String[] fileNameSplits = fileName.split("/");
        for (String s : fileNameSplits) {
            dir = join(dir, s);
        }
        return dir;
    }
}
