import groovy.io.FileType

class Tools {

    // Will return True if the region subset files exist, false otherwise
    public static Boolean checkDirAndFiles(dir) {
        def fileList = []
        def checkDir = new File(dir)
        
        // If the output directory doesn't exist, exit early with false
        if (! checkDir.exists()) {
            return false
        }

        // List files
        checkDir.eachFileRecurse (FileType.FILES) { file ->
            fileList << file
        }
        
        return fileList.size() != 0 ? true : false
    }

    // Parse alignment sample sheet and check for existance of index files
    public static Map hasIndex(String csv, String idxType) {
        // Read all lines from CSV file and get unique references
        def refs = []
        def csv_lines = new File(csv).readLines()*.tokenize(',')

        // Unique references in CSV file
        csv_lines.each { row ->
            if (! refs.contains(row[1])) {
                refs.add(row[1])
            }
        }

        // Index type
        def out = [(true): [], (false): []]
        switch(idxType) {
            case 'fai':
                refs.each { ref ->
                    def val = new File(ref + '.fai').exists()
                    out[(val)].add(ref)
                }
                break;
            case 'bwa':
                refs.each { ref ->
                    def val = new File(ref + '.0123').exists()
                    out[(val)].add(ref)
                }
                break;
        }

        return out
    }
}