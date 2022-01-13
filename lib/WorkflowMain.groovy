import groovy.json.JsonSlurper

class WorkflowMain {

    // prettyprint Helper function to print the help page nicely
    public static String prettyPrint(helpSubset) {
        // Coloured output
        def c_reset = "\033[0m"
        def c_green = "\033[0;32m"
        def c_yellow = "\033[0;33m"
        def c_red = "\033[0;31m"
        def line = '---------------------------- '

        // Iterate over the subset
        helpSubset.each { key, value ->
            def title = c_yellow + value['title'] + c_reset
            def arguments = value.get('arguments')

            // Sub-title formatting
            def Integer lineLen = 80 - (line.length() + title.length()) - 1
            println(
                line + title + ' ' + '-' * (lineLen + 11) + '\n\n' + 
                value['description'] + '\n'
            )

            arguments.each { argKey, argVals ->
                def startPos = line.length()
                def type = argVals['type']
                def desc = argVals['description']
                def valid = argVals.containsKey('valid') ? 
                    " Options: " + c_red + argVals.get('valid').join(', ') + c_reset + '.' : 
                    ''
                
                // Get the length of the variable - calculate whitespace before 'desc'
                def len = c_green.length() + argKey.length() + c_reset.length() + type.length() + 5
                def ws = startPos - len + 13
                println(
                    "--${c_green}${argKey}${c_reset} ${type}" + " " * ws + "${desc}${valid}"
                )
            }

            // Newline between help sections
            println()
        }
    }

    // help Prints the help text to screen
    public static String help(parameters, workflow) {
        
        if (parameters.help) {
            // Coloured output
            def c_reset = "\033[0m"
            def c_green = "\033[0;32m"
            def c_yellow = "\033[0;33m"

            // Parse pipeline schema as map - definitions of what arguments should be
            def strJson = new File("${workflow.projectDir}/nextflow_schema.json").text
            def Map mapSchema = (Map) new JsonSlurper().parseText(strJson)

            // Title for meta information
            def pipelineMeta = mapSchema.subMap([ 'version', 'title', 'description' ])
            def line1 = "\n" + "=" * 80 + "\n"
            def title = c_yellow + pipelineMeta['title'] + ' ' + pipelineMeta['version'] + c_reset
            def centerPoint = (80 - title.length())/2 + 4
            println(
                line1 + ' ' * centerPoint + title + line1 + 
                '\n' + pipelineMeta['description'].join('\n') + '\n'
            )

            // Defualt help page - or help page for sub-workflow too?
            def helpSubset = parameters.help instanceof String ? 
                mapSchema.get('definitions').subMap(['mandatory', parameters.help]) : 
                mapSchema.get('definitions').subMap(mapSchema.get('definitions').keySet( ))

            // Print the help pages nicely
            prettyPrint(helpSubset)

            // 'false' is created when help is run - delete it!
            def temp_outdir = new File('./false')
            if (temp_outdir.exists()) {
                temp_outdir.deleteDir()
            }
            System.exit(0)
        }
    }

    // initialise manage 'help', 'validation' and printing arguments to screen
    public static String initialise() {
            ...
    }
}
