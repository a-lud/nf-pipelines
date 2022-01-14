import groovy.json.JsonSlurper

class NfSchema {

    public static Map validateParameters(parameters, wrkflow) {
        
        // Parse pipeline schema as map - definitions of what arguments should be
        def strJson = new File("${wrkflow.projectDir}/nextflow_schema.json").text
        def Map mapSchemaDef = (Map) new JsonSlurper().parseText(strJson).get('definitions')

        // Check mandatory parameters
        checkMandatory(parameters, mapSchemaDef)

        // Check cluster settings (if any)
        checkCluster(parameters, mapSchemaDef, wrkflow)

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
                    assert v instanceof String: "ERROR: '--${key} ${v}' is not a string. Please check the help page."
                    break;
                case 'integer':
                    assert v instanceof Integer: "ERROR: '--${key} ${v}' is not an integer. Please check the help page."
                    break;
                case 'boolean':
                    assert v instanceof Boolean: "ERROR: '--${key} ${v}' is not a boolean. Please check the help page."
                    break;
            }

            // Check provided pipeline is a valid selection
            if (key == 'pipeline') {
                def valid = value.get('valid')
                assert valid.contains(v) : "ERROR: Invalid pipeline choice '--pipeline ${v}'. Please select one of: ${valid.join(', ')}"
            }
        }
    }

    // checkCluster Checks the cluster profile
    public static void checkCluster(parameters, schema, wrkflow) {
        // Partition values (schema + user)
        def Map partition = schema.get('cluster').get('arguments').get('partition')
        def partitionVal = parameters.get('partition')

        // Profile values (schema + user)
        def Map profile = schema.get('nf_arguments').get('arguments').get('profile')
        def List vProfiles = profile.get('valid')
        def List uProfiles = wrkflow.profile.tokenize(',')

        // Check selected profiles are valid + conda is one of the choices
        assert vProfiles.containsAll(uProfiles) : 
            "ERROR: Invalid profile selection '--profile ${uProfiles.join(', ')}'. Please select from the following: ${vProfiles.join(', ')}"
        assert uProfiles.contains('conda') : 
            "ERROR: Missing 'conda' as a profile selection. This pipeline relies on conda for software handling"

        // If SLURM profile is selected - check the partition argument
        if (uProfiles.contains('slurm')) {

            // Schema values for partition 
            def type = partition.get('type')
            def valid = partition.get('valid')

            assert partitionVal instanceof String: 
                "ERROR: '--partition ${partitionVal}' is not a string"

            assert valid.contains(partitionVal) : 
                "ERROR: Invalid parition choice '--partition ${partitionVal}. Please select one of: ${valid.join(', ')}"
        }
    }

    // traverseMap Traverse the argument map and return which arguments have pattern/nfile fields
    public static List traverseMap(argMap) {
        def koi = []
        for (entry in argMap) {
            def key = entry.key
            def valueMap = entry.value
            
            if (valueMap.keySet().containsAll(['pattern', 'nfiles']) ) {
                koi << key
            }
        }
        return koi
    }

    // checkPipelineArgs Check the provided arguments match the specifications in the schema
    public static Map checkPipelineArgs(parameters, schema, pipeline) {
        // Subset schema for pipeline parameters
        def Map mapPipelineSchema = schema.get(pipeline).get('arguments')
        def Map mutableParams = [:] // This is an ugly approach but does what I want...
        mutableParams.putAll(parameters)
        def paramToUpdate = [:]
        
        // Keys of interest - have pattern/nfiles fields
        def koi = traverseMap(mapPipelineSchema)

        // Iterate over each argument
        mapPipelineSchema.each { key,  values ->
            assert parameters.containsKey(key) : "ERROR: Missing argument '--${key}'"
            def paramValue = parameters.get(key)

            def remove = []
            if (values.keySet().contains("optional")) { // is an optional argument?
                if (!paramValue) { // If the argument is not passed, move on
                    return;
                }
            }

            // Does the argument have pattern/nfile fields? - will have both or neither
            def tmpMap = [:]
            if (koi.contains(key)) {
                tmpMap["${key}"] = [
                    "path": "tmpPath",
                    "pattern": "tempPattern", 
                    "nfiles": "tempNfiles" 
                ]
            } else {
                tmpMap = false
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
                        assert schemaValue.contains(paramValue) : 
                            "ERROR: Selection '${paramValue}' is invalid (passed to '--${key}'). Select from ${schemaValue.join(', ')}"
                        break;
                    case 'pattern':
                    case 'nfiles':
                        if(tmpMap) {
                            tmpMap[key]["path"] = paramValue
                            tmpMap[key][schemaKey] = schemaValue
                            // println(["${schemaKey}": schemaValue])
                            break;
                        } else {
                            break;
                        }
                }
            }

            // Update parameters with pattern/nfile fields
            if (tmpMap) {
                paramToUpdate.putAll(tmpMap)
            }
        }
        
        // Return updated parameters - some values from schema added (nfiles/patterns)
        mutableParams.putAll(paramToUpdate)
        
        return mutableParams
    }
}
