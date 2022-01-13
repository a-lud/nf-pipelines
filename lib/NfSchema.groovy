import groovy.json.JsonSlurper

class NfSchema {

    public static Map validateParameters(workflow, parameters) {
        
        // Parse pipeline schema as map - definitions of what arguments should be
        def strJson = new File("${workflow.projectDir}/nextflow_schema.json").text
        def Map mapSchemaDef = (Map) new JsonSlurper().parseText(strJson).get('definitions')

        // Check mandatory parameters
        checkMandatory(parameters, mapSchemaDef)

        // Check pipeline parameters
        def mapChecked = checkPipelineArgs(parameters, 
                                           mapSchemaDef, 
                                           parameters.get('pipeline'))

        // Return checked arguments
        return mapChecked
        
    }

    // checkmandatory Check that required pipeline paraemters have been passed and are of correct type
    public static void checkMandatory(parameters, schema) {
        def Map mapMandatory = schema.get('mandatory').get('arguments')

        // Iterate over each key and it's values, checking parameters against it
        mapMandatory.each { key, value ->
            assert parameters.containsKey(key) : "ERROR: Mandatory argument '--${key}' not provided"
            def v = parameters.get(key)
            
            // For the current mandatory key and value
            def type = value.get('type')
            switch(type) {
                case 'string':
                    assert v instanceof String: "ERROR: '--${key} ${v}' is not a string"
                    break;
                case 'integer':
                    assert v instanceof Integer: "ERROR: '--${key} ${v}' is not an integer"
                    break;
                case 'boolean':
                    assert v instanceof Boolean: "ERROR: '--${key} ${v}' is not a boolean"
                    break;
            }

            // Check provided pipeline is a valid selection
            if (key == 'pipeline') {
                def valid = value.get('valid')
                assert valid.contains(v) : "ERROR: Invalid pipeline choice '--pipeline ${v}'. Please select one of: ${valid.join(', ')}"
            }
        }

    }

    // checkPipelineArgs Check the provided arguments match the specifications in the schema
    public static Map checkPipelineArgs(parameters, schema, pipeline) {
        // Subset schema for pipeline parameters
        def Map mapPipelineSchema = schema.get(pipeline).get('arguments')

        // prime parameters with empty map for 'pattern' and 'nfiles' - always will be present
        parameters.put('pattern', "")
        parameters.put('nfile', "")

        // Iterate over each argument
        mapPipelineSchema.each { key,  values ->
            assert parameters.containsKey(key) : "ERROR: Missing argument '--${key}'"
            def paramValue = parameters.get(key)

            

            if (values.keySet().contains("optional")) { // is an optional argument?
                if (!paramValue) { // If the argument is not passed, move on
                    return;
                }
            }

            // Check parameter type - all will have a type
            def type = values.get('type')
            switch(type) {
                case 'string':
                    assert paramValue instanceof String: "ERROR: '--${key} ${paramValue}' is not a string"
                    break;
                case 'integer':
                    assert paramValue instanceof Integer: "ERROR: '--${key} ${paramValue}' is not an integer"
                    break;
                case 'boolean':
                    assert paramValue instanceof Boolean: "ERROR: '--${key} ${paramValue}' is not a boolean"
                    break;
            }
            
            // Iterate over the schema definitions and run checks on 'paramValue'
            values.each { schemaKey, schemaValue ->
                if ([ 'type', 'description', 'optional' ].contains(schemaKey) ) { // Can ignore these 
                    return
                }

                switch(schemaKey) {
                    case 'format':
                        if (schemaValue == 'directory-path' || schemaValue == 'file-path') {
                            File path = new File(paramValue)
                            assert path.exists() : "ERROR: Directory '${paramValue}' does not exist (passed to '--${key}')"
                        }
                        break;
                    case 'valid':
                        assert schemaValue.contains(paramValue) : """
                        ERROR: Selection '${paramValue}' is invalid (passed to '--${key}'). Select from ${schemaValue.join(', ')}
                        """.stripIndent()
                        break;
                    case 'pattern':
                    case 'nfiles':
                        parameters.put([ key, schemaKey].join('_'), schemaValue)
                        break;
                }
            }
        }
        
        // Return updated parameters - some values from schema added (nfiles/patterns)
        return parameters
    }
}