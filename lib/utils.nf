/*
Utilities script
    * Functions used by workflows 'nf-workflows'
*/

def printVersion(String version) {
    println(
        """
        ==============================================================
                              NF-PIPELINES ${version}           
        ==============================================================
        """.stripIndent()
    )
}

def printHelpMessage() {
    println(
        """
        Required arguments:
            --pipeline <str>                What pipeline do you want to run
            --files_dir <str>               Directory path to fastq/fasta files
            --files_ext <str>               Quoted regex string to match above files
            --files_type <str>              Type of files [paired, single, fasta]
            --outdir <str>                  Directory path to output location
            --email <str>                   University email
        """.stripIndent()
    )
}

def callHelp(Map args, String version) {

    if(args.help == true) {
        printVersion(version)
        printHelpMessage()
        System.exit(0)
    }
}

def checkRequiredArgs(Map args) {
    def requiredArguments = [ 'pipeline', 'files_dir', 'files_ext',
                              'files_type', 'outdir', 'email', 'partition' ]
    def valid_pipelines = [ 'msa', 'hyphy' ]
    def valid_types = [ 'paired', 'single', 'fasta' ]
    def valid_partitions = [ 'skylake', 'skylakehm', 'test' ]

    subset = args.subMap(requiredArguments)

    // Valid pipeline arguments
    assert valid_pipelines.any { it == subset.pipeline } : "ERROR: Selected pipeline \"${subset.pipeline}\" is invalid. Please select one of ${valid_pipelines}"

    // Check - files directory exists
    File dir = new File(subset.files_dir)
    assert dir.exists() : "ERROR: Files directory (--files_dir) doesn't exist - ${subset.files_dir}"

    // Check - file type is valid
    bool = valid_types.any {it == subset.files_type}
    assert bool : "ERROR: Invalid file type (--files_type) value - ${subset.files_type}"

    bool = valid_partitions.any { it == subset.partition }
    assert bool : "ERROR: Invalid Phoenix partition (--partition) specified - ${subset.partition}"

    // Check - no values equal 'false'
    subset.each { key, value ->
        try {
            assert value != false
        } catch(AssertionError e) {
            println("ERROR: Missing required argument --${key}\nError message: " + e.getMessage())
        }
    }
    
    return subset
}

def checkMsaArgs(Map args) {
    def msa = [ 'aligner', 'aligner_args', 'pep2nuc', 'nucleotide_dir', 'nucleotide_ext', 'clean_alignments', 'gblocks_args']
    def aligners = [ 'mafft', 'muscle', 'clustal', 'tcoffee' ]

    // Subset arguments
    subset = args.subMap(msa)

    // Check MSA required
    assert subset.aligner != false : "ERROR: Must select a multiple sequence aligner"

    // Check arguments - aligner
    bool = aligners.any { it == subset.aligner }
    assert bool == true : "ERROR: Must select a valid multiple sequence alignment tool - mafft/muscle/tcoffe/clustal"

    // Check arguments - convert alignments
    if(subset.pep2nuc) {

        // Check values have been passed to arugments
        try {
            assert subset.nucleotide_dir != false
        } catch (AssertionError e) {
            println("ERROR: --pep2nuc selected but missing arguments to --nucleotide_dir\nError message: " + e.getMessage())
        }

        try {
            assert subset.nucleotide_ext != false
        } catch (AssertionError e) {
            println("ERROR: --pep2nuc selected but missing arguments to --nucleotide_ext\nError message: " + e.getMessage())
        }

        // Check nucleotide directory exists
        try {
            File dir = new File(subset.nucleotide_dir)
            assert dir.exists()
        } catch (AssertionError e) {
            println("ERROR: Neucleotide directory doesn't exist\nError message: " + e.getMessage())
        }
    }

    // All checks passed? return subset
    return subset

}

def printMsaArgs(Map args, String pipeline) {

    def msaArguments = [ 'aligner', 'aligner_args', 'pep2nuc', 'nucleotide_dir', 'nucleotide_ext', 'clean_alignments', 'gblocks_args']
    subset = args.subMap(msaArguments)

    // Get the pipeline header ready
    String header = '--------------------- ' + pipeline
    Integer n = 50 - header.length() - 1
    println('\n' + header + ' ' + '-' * n + '\n')
    
    subset.each {key, value ->
        if(value instanceof java.util.ArrayList) {
            println("$key:")
            value.each { v -> 
                println("  $v")
            }
        } else {
            println("$key: $value")
        }
    }

    println('')

}

def checkHyphyArgs(Map args) {
    def hyphy = [ 'method', 'tree', 'pvalue', 'fel_optional','slac_optional',
    'fubar_optional', 'meme_optional', 'absrel_optional',
    'busted_optional','relax_optional' ]
    def methods = [ 'fel', 'slac', 'fubar', 'meme', 'absrel',
                    'busted', 'relax' ]

    // Subset arguments to HyPhy specific
    subset = args.subMap(hyphy)

    // Method/s have been provided
    assert subset.method != false : 'ERROR: Must provide at least one method - ' + methods

    // Convert provided method/s to list
    method_lst = subset.method.tokenize(',')

    // Check that provided methods are valid
    assert methods.containsAll(method_lst) : "ERROR: At least one of the provided methods is invalid"

    // Check pvalue
    assert subset.pvalue != false : 'ERROR: Must provide a p-value between 0 and 1'

    // Check tree file
    assert subset.tree != false : "ERROR: Must provide file path to tree"
    tree_lst = subset.tree.tokenize(',')
    tree_lst.each { t ->
        try {
            File file = new File(t)
            assert file.exists()
        } catch(AssertionError e) {
            println("ERROR: Tree file does not exist or is empty\n Error message: " + e.getMessage())
            System.exit(1)
        }
    }

    // Checks are passed - clean up arguments and return
    subset.put('method', method_lst)
    subset.put('tree', tree_lst)

    return subset
}

def printHyphyArgs(Map args, String pipeline) {
    def hyphyArguments = [ 'method', 'tree', 'pvalue', 'fel_optional', 
                           'slac_optional', 'fubar_optional', 'meme_optional', 'absrel_optional', 'busted_optional', 'relax_optional' ]
    subset = args.subMap(hyphyArguments)

    // Get the pipeline header ready
    String header = '--------------------- ' + pipeline
    Integer n = 50 - header.length() - 1
    println('\n' + header + ' ' + '-' * n + '\n')
    
    subset.each {key, value ->
        if(value instanceof java.util.ArrayList) {
            println("$key:")
            value.each { v -> 
                println("  $v")
            }
        } else {
            println("$key: $value")
        }
    }

    println('')
}

def printArguments(Map args) {

    // Drop un-needed arguments
    args = args.findAll({!['help'].contains(it.key)})

    // Subset arguments relating to each sub-workflow
    def requiredArguments = [ 'pipeline', 'files_dir', 'files_ext',
                              'files_type', 'outdir', 'email' ]
    subset_required = args.subMap(requiredArguments)
    
    def msaArguments = [ 'aligner', 'aligner_args', 'pep2nuc', 'nucleotide_dir', 'nucleotide_ext', 'clean_alignments', 'gblocks_args']

    def hyphyArguments = [ 'method', 'tree', 'pvalue', 'fel_optional',         
                           'slac_optional', 'fubar_optional', 'meme_optional', 'absrel_optional', 'busted_optional', 'relax_optional' ]
    
    // Subset for resource arguments only
    resourcesArguments = args.findAll { k,v -> !(k in requiredArguments + msaArguments + hyphyArguments) }.keySet()
    subset_resources = args.subMap(resourcesArguments)

    println(
        """
        ##################################################
        ################### Arguments ####################
        """.stripIndent())

    // Print required arguments
    println('--------------------- Main -----------------------\n')
    subset_required.each {key, val ->
        if(val instanceof java.util.ArrayList) {
            println "${key}:"
            val.each {v ->
                println "  ${v}"
            }
        } else {
            println "${key}: ${val}"
        }
    }

    // Print resources
    String header = '--------------------- Resources'
    Integer n = 50 - header.length() - 1
    println('\n' + header + ' ' + '-' * n + '\n')

    subset_resources.each { key, val ->
        if(val instanceof java.util.ArrayList) {
            println "${key}:"
            val.each {v ->
                println "  ${v}"
            }
        } else {
            println "${key}: ${val}"
        }
    }
}
