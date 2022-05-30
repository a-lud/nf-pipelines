import groovy.json.JsonSlurper

class NfSchema {

    public static Map validateParameters(parameters, wrkflow) {
        
        // Parse pipeline schema as map - definitions of what arguments should be
        def strJson = new File("${wrkflow.projectDir}/nextflow_schema.json").text
        def Map mapSchemaDef = (Map) new JsonSlurper().parseText(strJson).get('definitions')

        // Check mandatory parameters
        checkMandatory(parameters, mapSchemaDef, wrkflow)

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
    public static void checkMandatory(parameters, schema, wrkflow) {
        def red = "\033[0;31m";
        def reset = "\033[0m";

        def Map mapMandatory = schema.get('mandatory').get('arguments')

        // Iterate over each key and it's values, checking parameters against it
        mapMandatory.each { key, value ->
            assert parameters.containsKey(key) : "${red}ERROR: Mandatory argument '--${key}' not provided${reset}"
            def v = parameters.get(key)
            
            // For the current mandatory key and value
            def type = value.get('type')
            switch(type) {
                case 'string':
                    assert v instanceof String: 
                        "${red}ERROR: '--${key} ${v}' is not a string. Please check the help page.${reset}"
                    break;
                case 'integer':
                    assert v instanceof Integer: 
                        "${red}ERROR: '--${key} ${v}' is not an integer. Please check the help page.${reset}"
                    break;
                case 'boolean':
                    assert v instanceof Boolean: 
                        "${red}ERROR: '--${key} ${v}' is not a boolean. Please check the help page.${reset}"
                    break;
            }

            // Check provided pipeline is a valid selection
            if (key == 'pipeline') {
                def valid = value.get('valid')
                assert valid.contains(v) : 
                    "${red}ERROR: Invalid pipeline choice '--pipeline ${v}'. Please select one of: ${valid.join(', ')}${reset}"
            }
        }
    }

    // checkCluster Checks the cluster profile
    public static void checkCluster(parameters, schema, wrkflow) {
        def red = "\033[0;31m";
        def reset = "\033[0m";

        // Partition values (schema + user)
        def Map partition_schema = schema.get('cluster').get('arguments').get('partition')
        def partition_user = parameters.get('partition')

        // Profile values (schema + user)
        def List profiles_schema = schema.get('nf_arguments').get('arguments').get('profile').get('valid')
        def List profiles_user = wrkflow.profile.tokenize(',')

        // Check selected profiles are valid + conda is one of the choices
        assert profiles_schema.containsAll(profiles_user) : 
            "${red}ERROR: Invalid profile selection '--profile ${profiles_user.join(', ')}'. Please select from the following: ${profiles_schema.join(', ')}${reset}"
        
        // TODO: How to manage software - conda is better (currently) but GADI needs modules...
        // assert profiles_user.contains('conda') :
            // "${red}ERROR: Missing 'conda' as a profile selection. This pipeline relies on conda for software handling${reset}"
        
        // Get partition values based on profile (e.g. phoenix, gadi etc...)
        def match = profiles_user.intersect(partition_schema.get('valid').keySet())[0]
        def valid_partitions = partition_schema.get('valid').get(match)
        if (match != 'standard') {
            assert valid_partitions.contains(partition_user):
                "${red}ERROR: Invalid parition choice '--partition ${partition_user}. Please select one of: ${valid_partitions.join(', ')}${reset}"
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
        def red = "\033[0;31m";
        def reset = "\033[0m";

        // Subset schema for pipeline parameters
        def Map mapPipelineSchema = schema.get(pipeline).get('arguments')
        def Map mutableParams = [:] // This is an ugly approach but does what I want...
        mutableParams.putAll(parameters)
        def paramToUpdate = [:]
        
        // Keys of interest - have pattern/nfiles fields
        def koi = traverseMap(mapPipelineSchema)

        // Iterate over each argument
        mapPipelineSchema.each { key,  values ->
            // If an optional argument, check its presence or move on if not given
            if (values.optional) {
                if (!parameters.containsKey(key)) {
                    return;
                }
            } else {
                assert parameters.containsKey(key) : "${red}ERROR: Missing argument '--${key}'${reset}"
            }

            def paramValue = parameters.get(key)

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
                    assert paramValue instanceof String: "${red}ERROR: '--${key} ${paramValue}' is not a string${reset}"
                    break;
                case 'integer':
                    assert paramValue instanceof Integer: "${red}ERROR: '--${key} ${paramValue}' is not an integer${reset}"
                    break;
                case 'boolean':
                    assert paramValue instanceof Boolean: "${red}ERROR: '--${key} ${paramValue}' is not a boolean${reset}"
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
                            assert path.exists() : 
                            "${red}ERROR: Directory '${paramValue}' does not exist (passed to '--${key}')${reset}"
                        }
                        break;
                    case 'valid':
                        // Check if the user can provide multiple arguments
                        if(schemaValue[1] == 'single') {
                            assert schemaValue[0].contains(paramValue) : 
                            "${red}ERROR: Selection '${paramValue}' is invalid (passed to '--${key}'). Select from ${schemaValue[0].join(', ')}${reset}"
                        } else {
                            def usrParamVal = paramValue.tokenize(" ")
                            assert schemaValue[0].containsAll(usrParamVal) :
                            "${red}ERROR: Selection '${paramValue}' is invalid (passed to '--${key}'). Select from ${schemaValue[0].join(', ')}${reset}"
                        }
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
