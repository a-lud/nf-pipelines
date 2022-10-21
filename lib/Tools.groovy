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
}