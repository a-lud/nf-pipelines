import groovy.json.JsonSlurper

class WorkflowMain {

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
            def dash = key == 'nf_arguments' ? '-' : '--'
            def offset = key == 'nf_arguments' ? 14 : 13
            def title = c_yellow + value['title'] + c_reset
            def description = value['description'] instanceof String ? value['description'] : value['description'].join('\n')
            def arguments = value.get('arguments')

            // Sub-title formatting
            def Integer lineLen = 80 - (line.length() + title.length()) - 1
            println(
                line + title + ' ' + '-' * (lineLen + 11) + '\n\n' + 
                description + '\n'
            )

            // Format each set of arguments
            arguments.each { argKey, argVals ->
                def startPos = line.length()
                def type = argVals['type']
                def desc = argVals['description']
                def valid = ''
                if (argVals.containsKey('valid')) {
                    if (argVals.get('valid') instanceof org.apache.groovy.json.internal.LazyMap) {
                        def temp = []
                        argVals.get('valid')
                               .findAll{ it.key != 'standard' }
                               .each { entry ->
                                   temp << "$entry.key=" + entry.value
                               }
                        def val = temp.join(', ')
                        valid = " Options: " + c_red + val + c_reset + '.'
                    } else if(argVals.get('valid') instanceof java.util.ArrayList) {
                        valid = " Options: " + c_red + argVals.get('valid')[0].join(', ') + c_reset + '.'
                    }
                }
                
                // Get the length of the variable - calculate whitespace before 'desc'
                def len = c_green.length() + argKey.length() + c_reset.length() + type.length() + 5
                def ws = startPos - len + offset
                println(
                    "${dash}${c_green}${argKey}${c_reset} ${type}" + " " * ws + "${desc}${valid}"
                )
            }

            // Newline between help sections
            println()
        }
    }

    // summaryArguments prints the arguments to screen in a formatted style
    public static String summaryArguments(arguments, wrkflow) {
        def strJson = new File("${wrkflow.projectDir}/nextflow_schema.json").text
        def Map mapSchemaDef = (Map) new JsonSlurper().parseText(strJson).get('definitions')
        def pipeline = arguments.pipeline
        def profiles = wrkflow.profile.tokenize(',')
        def line = '---------------------------- '

        // Get arguments for relevant fields in the correct order
        def subKeys = profiles.contains('slurm') ? 
            ["mandatory", "nf_arguments", pipeline, "cluster"] : 
            ["mandatory", "nf_arguments", pipeline]
        def argSubset = mapSchemaDef.subMap(subKeys)
        argSubset["nf_arguments"] = "" // Blank so I can set actually accessible features below

        argSubset.each { key, values ->

            // Set features for NF-parameters that I can get from workflow
            // Ugly but does the trick
            def argNames = key == 'nf_arguments' ? 
                ["start", "workDir", "profile"] : 
                values.arguments.keySet()

            def argValues = key == 'nf_arguments' ? 
                wrkflow.toMap().subMap(argNames) : 
                arguments.subMap(argNames)

            // Print the header for each section
            def lineLen = 80 - (line.length() + key.length()) - 12
            println(line + key + ' ' + '-' * (lineLen + 11) + '\n')

            argValues.each {k, v ->
                
                if (v instanceof Map) {
                    println("${k}")
                    v.each { ka, va ->
                        def ws = line.length() - (ka.length() + 3)
                        println(" - ${ka}" + (" " * ws) + va)
                    }
                } else {
                    def ws = line.length() - k.length()
                    println(k + (" " * ws) + v)
                }
            }
            println()
        }
    }

    // initialise manage 'help', 'validation' and printing arguments to screen
    public static Map initialise(parameters, wrkflow) {
        
        // Call help
        if (parameters.help) {
            help(parameters, wrkflow)
        }

        // Validate parameters
        def checkedArgs = NfSchema.validateParameters(parameters, wrkflow)

        // Print the user provided arguments to screen
        summaryArguments(checkedArgs, wrkflow)
        return checkedArgs
    }
}
