
// Argument class
class WorkflowArguments {
	// Check the required arguments for the pipeline
	public static void checkRequiredArgs(Map arguments, String profile) {
		def required = [ 
			args: [ 'pipeline', 'files_dir', 'files_ext', 'files_type', 'outdir', 'email' ],
			pipelines: [ 'msa', 'hyphy', 'codeml', 'transcurate' ],
			filetypes: [ 'paired', 'single', 'fasta' ],
			partitions: [ 'skylake', 'skylakehm', 'test' ]
		]

		def subset = arguments.subMap(required.args)

		// Check none of the arguments are false
		subset.each { key, value ->
				assert value instanceof java.lang.String : "ERROR: Missing required argument '--${key}'"
		}

		assert required.pipelines.contains(subset.pipeline) : "ERROR: Pipeline selection '${subset.pipeline}' is invalid. Select on of the following - " + required.pipelines.join(', ')
		
		File dir = new File(subset.files_dir)
		assert dir.exists() : "ERROR: Parameter passed to '--files_dir' does not exist - ${subset.files_dir}"

		assert required.filetypes.contains(subset.files_type) : "ERROR: '--files_type ${subset.files_type}' is invalid. Select form the following - " + required.filetypes.join(', ')

		// Ensure a valid partition is used if submitting to Phoenix
		if (profile.tokenize(',').contains('slurm')) {
			assert required.partitions.contains(arguments.partition) : "ERROR: Partition '--partition ${arguments.partition}' is invalid. Select one of the following - " + required.partitions.join(', ')
		}
	}


	// Check methods for each pipeline
	public static void checkMsaArgs(Map arguments) {
		def msa = [ 
			args: [ 'aligner', 'aligner_args', 'pep2nuc', 'nucleotide_dir', 'nucleotide_ext', 'clean_alignments', 'gblocks_args'],
			aligners: [ 'mafft', 'muscle', 'clustal', 'tcoffee' ] 
		]

		// Subset all params for MSA specific
	 	def subset = arguments.subMap(msa.args)

		assert subset.aligner instanceof java.lang.String : "ERROR: Aligner '${subset.aligner}' is not valid. Select from - " + msa.aligners.join(', ') // Aligner provided
		assert msa.aligners.contains(subset.aligner) : "ERROR: Aligner '${subset.aligner}' is not valid. Select from " + msa.aligners.join(', ')

		if (subset.pep2nuc) {
			// Check fasta files for peptide -> nucleotide conversion
			assert subset.nucleotide_dir instanceof java.lang.String : "ERROR: Provide path to '--nucleotide_dir'"
			
			File dir = new File(subset.nucleotide_dir)
			assert dir.exists() : "ERROR: The path '--nucleotide_dir ${subset.nucleotide_dir}' doesn't exist"

			assert subset.nucleotide_ext instanceof java.lang.String : "ERROR: Fasta extension is not a string - e.g. '.fa'"
		}
	}

	public static void checkHyphyArgs(Map arguments) {
		def hyphy = [
			args: [ 'method', 'tree', 'fel_optional','slac_optional', 
					'fubar_optional', 'meme_optional', 'absrel_optional',
					'busted_optional','relax_optional' ],
			methods: [ 'fel', 'slac', 'fubar', 'meme', 'absrel', 'busted', 'relax' ]
		]

		def subset = arguments.subMap(hyphy.args)

		assert subset.method instanceof java.lang.String : "ERROR: Please choose a supported method - " + hyphy.methods.join(', ')
		assert hyphy.methods.containsAll(subset.method.tokenize(',')) : "ERROR: Method selection '--method ${subset.method}' is invalid. Select from - " + hyphy.methods.join(',')

		assert subset.tree instanceof java.lang.String : "ERROR: Provide path/s to tree files"
		subset.tree.tokenize(',').each { tr ->
			File f = new File(tr)
			assert f.exists() : "ERROR: Tree file '--tree ${tr}' does not exist"
		}
	}

	public static void checkCodemlArgs(Map arguments) {
		def codeml = [
			args: [ 'models', 'trees', 'tests', 'codeml_optional' ],
			models: [ 'M0', 'M1', 'M2', 'M3', 'M4', 'M5', 'M6', 'M7',
					'M8', 'M8a', 'M9', 'M10', 'M11', 'M12', 'M13',
					'SLR',' bsA', 'bsA1', 'bsB', 'bsC', 'bsD', 
					'b_free', 'b_neut', 'fb', 'fb_anc' ]
		]

		def subset = arguments.subMap(codeml.args)

		assert subset.models instanceof java.lang.String : "ERROR: Please choose a model/s (see help page for full list)"
		assert subset.trees instanceof java.lang.String : "ERROR: Plase provide files paths to tree/s"
		assert codeml.models.containsAll(subset.models.tokenize(',')) : "ERROR: Invalid model selection - " + subset.models
		
		if (subset.tests) {
			assert subset.models.tokenize(',').containsAll(subset.tests.replaceAll(' ', ',').tokenize(',')) : "ERROR: Discrepancy between '--models' and '--tests'"
		}
		
		subset.trees.tokenize(',').each { tr -> 
			File f = new File(tr)
			assert f.exists() : "ERROR: Tree file '--tree ${tr}' does not exist"
		}
	}

	public static void checkTransCurateArgs(Map arguments) {
		def transcurate = [ 
			args: [ 'cdhit_pid', 'database_dir', 'completeORFs' ],
		]

		def subset = arguments.subMap(transcurate.args)

		assert subset.cdhit_pid instanceof Number : "ERROR: Value provided to '--cdhit_pid' is not a number"
		assert subset.cdhit_pid < 1 : "ERROR: Value '--cdhit_pid ${subset.cdhit_pid}' is larger than 1 (0 < cdhit_pid < 1])"

		// Database directory/files is checked in the workflow - will create the database if not provided
	}
	
	// Print the arguments
	public static String prettyPrint(Map submap) {
		def c_reset = "\033[0m"
    	def c_green = "\033[0;32m"
    	def c_yellow = "\033[0;33m"

		println(
		"""
		##################################################
		################### ${c_yellow}Arguments${c_reset} ####################
		""".stripIndent()
		)

		// Print required arguments
		submap.each { key, val ->
			// required, resources, <pipeline>
			if ([ 'required', 'resources' ].contains(key)) {
				String header = "------------------- ${c_yellow}${key.capitalize()}${c_reset}"
				Integer n = 50 - header.length() - 1 + 11 // 11 accounts for the colour
				println('\n' + header + ' ' + '-' * n + '\n')
				
				val.each {k, v ->
					println("${c_green}${k}${c_reset}: ${v}")
				}
			} else {
				String header = "------------------- ${c_yellow}${key.capitalize()}${c_reset}"
				Integer n = 50 - header.length() - 1 + 11
				println('\n' + header + ' ' + '-' * n + '\n')
				
				val.each { k,v ->
					if (v.toString().contains(',')) {
						println("${c_green}${k}${c_reset}:")

						// Codeml specific condition for '--tests'. Values are separated by ' ', not ','
						if (k == 'tests') {
							v.tokenize(' ').each { i -> 
								println("  ${i}")
							}
						} else {
							v.tokenize(',').each {i ->
								println("  ${i}")
							}
						}
					} else {
						println("${c_green}${k}${c_reset}: ${v}")
					}
				}
			}
		}

		println()
	}

	public static String printArguments(Map arguments) {
		def map_workflow = [
			required: [ 'pipeline', 'files_dir', 'files_ext', 'files_type', 'outdir', 'email' ],
			msa: [ 'aligner', 'aligner_args', 'pep2nuc', 'nucleotide_dir', 'nucleotide_ext', 'clean_alignments', 'gblocks_args'],
			hyphy: [ 'method', 'tree', 'fel_optional', 'slac_optional', 'fubar_optional', 'meme_optional', 'absrel_optional', 'busted_optional', 'relax_optional' ],
			codeml: [ 'models', 'trees', 'tests', 'codeml_optional' ],
			transcurate: [ 'cdhit_pid', 'database_dir', 'completeORFs' ],
			resources: [ 'partition', 'max_memory', 'max_cpu', 'max_time']
		]

		def subset = map_workflow.subMap( ['required', arguments.pipeline, 'resources' ] )
		def subset_print = [:]
		subset.each { key, value ->
			subset_print.put(key, arguments.subMap(value))
		}
		
		prettyPrint(subset_print)
	}

	public static void checkArguments(Map arguments, String profile) {
		// Check the required arguments regardless of sub-workflow
		checkRequiredArgs(arguments, profile)

		// Check subworkflow arguments
		switch(arguments.pipeline) {
			case 'msa':
				checkMsaArgs(arguments)
				break;
			case 'hyphy':
				checkHyphyArgs(arguments)
				break;
			case 'codeml':
				checkCodemlArgs(arguments)
				break;
			case 'transcurate':
				checkTransCurateArgs(arguments)
				break;
		}

		// Print arguments to screen
		printArguments(arguments)
	}
}
