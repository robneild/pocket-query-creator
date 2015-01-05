package org.pquery.util;

import java.io.File;
import java.util.Comparator;

public class FileComparator implements Comparator<File> {

    @Override
    public int compare(File lhs, File rhs) {
        if (lhs.equals(rhs))
            return 0;
        return String.CASE_INSENSITIVE_ORDER.compare(lhs.getName(), rhs.getName());
    }

}
