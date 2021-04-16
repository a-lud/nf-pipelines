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

def printHelpMSA() {
    println(
        """
        MSA arguments:
            --aligner <str>                 MSA software to use: mafft,muscle,clustal,tcoffee
            --aligner_args <str>            String of aligner arguments
            --pep2nuc                       Flag that will result in the conversion of peptide alignments to nucleotide
            --nucleotide_dir <str>          Directory path to nucleotide Fasta files
            --nucleotide_ext <str>          String of nucleotide extensions (e.g. '.fa', '.fasta', '.fna')
            --clean_alignments              Flag that will result in GBlocks being used to clean gappy alignments
            --gblocks_args <str>            String of arguments to provide to GBlocks
        """.stripIndent()
    )
}

def printHelpHyPhy() {
    println(
        """
        HyPhy arguments:
            --method <str>                  Comma separated list of valid methods (fel,slac,fubar,meme,absrel,busted,relax)
            --tree <str>                    Comma separated list of tree files
            --fel_optional <str>            String of optional arguments for the 'fel' method
            --slac_optional <str>           String of optional arguments for the 'slac' method
            --fubar_optional <str>          String of optional arguments for the 'fubar' method
            --meme_optional <str>           String of optional arguments for the 'meme' method
            --absrel_optional <str>         String of optional arguments for the 'absrel' method
            --busted_optional <str>         String of optional arguments for the 'busted' method
            --relax_optional <str>          String of optional arguments for the 'relax' method
        """.stripIndent()
    )
}

def printHelpCodeml() {
    println(
        """
        CodeML arguments:
            --models <str>                  String of models to run separated by commas (e.g. 'M1,M2')
                                            [valid models: M0, M1, M2, M3, M4,
                                                           M5, M6, M7, M8, M8a,
                                                           M9, M10, M11, M12, M13,
                                                           SLR, bsA, bsA1, bsB, bsC,
                                                           bsD, b_free, b_neut, fb, fb_anc]
            --trees <str>                   List of tree files to use
            --tests <str>                   String of model comparisons (e.g. 'M2,M1 M3,M0')
            --codeml_optional <str>         Optional paramters for ETE-Evol CodeML
        """.stripIndent()
    )
}

def printHelpTransCurate() {
    println(
        """
        Transcriptome Curation Arguments:
            --cdhit_pid <float>             Percentage identity threshold for collapsing transcripts
            --database_dir <str>            Directory path containin premade BLAST and PFAM databases
            --completeORFs                  Flag to keep only CDS sequences with complete ORFs
        """.stripIndent()
    )
}

def callHelp(Map args, String version) {

    sep = "--------------------------------------------------------------"

    if(args.help == true) {
        printVersion(version)
        printHelpMessage()
        System.exit(0)
    } else if (args.help == 'msa') {
        printVersion(version)
        printHelpMessage()
        println(sep)
        printHelpMSA()
        System.exit(0)
    } else if (args.help == 'hyphy') {
        printVersion(version)
        printHelpMessage()
        println(sep)
        printHelpHyPhy()
        System.exit(0)
    } else if (args.help == 'codeml') {
        printVersion(version)
        printHelpMessage()
        println(sep)
        printHelpCodeml()
        System.exit(0)
    } else if (args.help == 'transcurate') {
        printVersion(version)
        printHelpMessage()
        println(sep)
        printHelpTransCurate()
        System.exit(0)
    }
}

def checkRequiredArgs(Map args) {
    def requiredArguments = [ 'pipeline', 'files_dir', 'files_ext',
                              'files_type', 'outdir', 'email', 'partition' ]
    def valid_pipelines = [ 'msa', 'hyphy', 'codeml', 'transcurate' ]
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
            println("ERROR: Missing required argument --${key}") 
            println(e.getMessage())
            System.exit(1)
            
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
            println("ERROR: --pep2nuc selected but missing arguments to --nucleotide_dir")
            println(e.getMessage())
            System.exit(1)
        }

        try {
            assert subset.nucleotide_ext != false
        } catch (AssertionError e) {
            println("ERROR: --pep2nuc selected but missing arguments to --nucleotide_ext")
            println(e.getMessage())
            System.exit(1)
        }

        // Check nucleotide directory exists
        try {
            File dir = new File(subset.nucleotide_dir)
            assert dir.exists()
        } catch (AssertionError e) {
            println("ERROR: Neucleotide directory doesn't exist")
            println(e.getMessage())
            System.exit(1)
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
    def hyphy = [ 'method', 'tree', 'fel_optional','slac_optional',
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

    // Check tree file
    assert subset.tree != false : "ERROR: Must provide file path to tree"
    tree_lst = subset.tree.tokenize(',')
    tree_lst.each { t ->
        try {
            File file = new File(t)
            assert file.exists()
        } catch(AssertionError e) {
            println("ERROR: Tree file does not exist or is empty")
            println(e.getMessage())
            System.exit(1)
        }
    }

    // Checks are passed - clean up arguments and return
    subset.put('method', method_lst)
    subset.put('tree', tree_lst)

    return subset
}

def printHyphyArgs(Map args, String pipeline) {
    def hyphyArguments = [ 'method', 'tree', 'fel_optional', 
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

def checkCodemlArgs(Map args) {
    def codeml = [ 'models', 'trees', 'tests', 'codeml_optional' ]
    def valid_models = [ 'M0', 'M1', 'M2', 'M3', 'M4', 'M5', 'M6', 'M7',
                         'M8', 'M8a', 'M9', 'M10', 'M11', 'M12', 'M13',
                         'SLR',' bsA', 'bsA1', 'bsB', 'bsC', 'bsD', 
                         'b_free', 'b_neut', 'fb', 'fb_anc' ]

    // Subset arguments to CodeML specific
    subset = args.subMap(codeml)

    // Check required arguments
    try {
        assert subset.models
        assert subset.trees
    } catch (AssertionError e) {
        println("ERROR: Missing required argument")
        println(e.getMessage())
        System.exit(1)
    }

    // Check models are valid
    try {
        assert valid_models.containsAll(subset.models.tokenize(','))
    } catch (AssertionError e) {
        println("ERROR: Model/s provided are not valid")
        println(e.getMessage())
        System.exit(1)
    }

    // Check tests are part of provided models
    if(subset.tests) {
        try {
            mod = subset.models.tokenize(',')
            test = subset.tests.replaceAll(' ', ',').tokenize(',')
            assert mod.containsAll(test)
        } catch (AssertionError e) {
            println("ERROR: Models provided to '--tests' do no match '--models'")
            println(e.getMessage())
            System.exit(1)
        }
    }

    // Check tree files
    def list_trees = []
    subset.trees.tokenize(',').each {
        try {
            File file = new File(it)
            assert file.exists()
            list_trees.add(file)
        } catch (AssertionError e){
            println("ERROR: Tree file does not exist - ${it}")
            println(e.getMessage())
            System.exit(1)
        }
    }

    subset.models = subset.models.replaceAll(',', ' ')
    subset.trees = list_trees
    return subset
}

def printCodemlArgs(Map args, String pipeline) {
    def codemlArguments = [ 'models', 'trees', 'tests', 'codeml_optional' ]
    subset = args.subMap(codemlArguments)

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

def checkTransCurateArgs(Map args) {
    def transCurate = [ 'cdhit_pid', 'database_dir', 'completeORFs' ]

     // Subset arguments to TransCurate specific
    subset = args.subMap(transCurate)

    // Check CDHIT parameters
    if (subset.cdhit_pid) {
        assert subset.cdhit_pid instanceof Number
        assert subset.cdhit_pid < 1
    } else {
        subset.cdhit_pid = 0.95
    }

    return subset
}

def printTransCurateArgs(Map args, String pipeline) {
    def transCurateArgs = [ 'cdhit_pid', 'database_dir', 'completeORFs' ]
    subset = args.subMap(transCurateArgs)

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

    def hyphyArguments = [ 'method', 'tree', 'fel_optional',         
                           'slac_optional', 'fubar_optional', 'meme_optional', 'absrel_optional', 'busted_optional', 'relax_optional' ]

    def codemlArguments = [ 'models', 'trees', 'tests', 'codeml_optional' ]

    def transCurateArguments = [ 'cdhit_pid', 'database_dir', 'completeORFs' ]
        
    // Subset for resource arguments only
    resourcesArguments = args.findAll { k,v -> !(k in requiredArguments + msaArguments + hyphyArguments + codemlArguments + transCurateArguments) }.keySet()
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
