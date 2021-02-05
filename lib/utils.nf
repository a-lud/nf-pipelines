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
        Arguments to come...
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
    def requiredArguments = [ 'files_dir', 'files_ext', 'files_type',
                              'outdir', 'email', 'partition' ]
    def valid_types = [ 'paired', 'single', 'fasta' ]
    def valid_partitions = [ 'skylake', 'skylakehm', 'test' ]

    subset = args.subMap(requiredArguments)

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
    def msa = [ 'aligner', 'aligner_args', 'pep2nuc', 'nucleotide_dir', 'nucleotide_ext',
                'clean_alignments', 'gblocks_args']
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

def printArguments(Map args) {

    // Drop un-needed arguments
    args = args.findAll({!['help'].contains(it.key)})

    def requiredArguments = [ 'files_dir', 'files_ext', 'files_type',
                              'outdir', 'email' ]
    subset_required = args.subMap(requiredArguments)
    
    def msaArguments = [ 'aligner', 'aligner_args', 'pep2nuc', 'nucleotide_dir', 'nucleotide_ext',
                'clean_alignments', 'gblocks_args']
    subset_msa = args.subMap(msaArguments)    
    
    resourcesArguments = args.findAll { k,v -> !(k in requiredArguments + msaArguments) }.keySet()
    subset_resources = args.subMap(resourcesArguments)

    lst = [ subset_required, subset_msa, subset_resources ]

    println(
        """
        ##################################################
        ################### Arguments ####################
        """.stripIndent())

    lst.each { l -> 
        l.each {key, value ->

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
}
