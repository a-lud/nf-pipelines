class WorkflowMain {
	public static String printVersion(String version) {
		def message = ''
		message += """
		==============================================================
					NF-PIPELINES ${version}
		==============================================================
		""".stripIndent()

		println(message)
	}

	public static String printHelpMessage() {
		def message = ''
		message += """
		Nextflow Arguments:
			-profile <str>			Which Nextflow profile to use: Should ALWAYS be 'conda,slurm'
			-N <str>			Email that a notification of completion will be sent to
		
		Required Arguments:
			--pipeline <str>		What pipeline do you want to run
			--files_dir <str>		Directory path to fastq/fasta files
			--files_ext <str>		Quoted regex string to match above files
			--files_type <str>		Type of files [paired, single, fasta]
			--outdir <str>			Directory path to output location
			--email <str>			University email

		Help Pages: To show the help pages for the available pipelines, use the following command
			'nextflow run main.nf --help <msa,hyphy,codeml,transcurate>
		""".stripIndent()
		println(message)
	}

	public static String printHelpMSA() {
		def message = ''
		message += """
		MSA Arguments:
			--aligner <str>				MSA software to use: mafft,muscle,clustal,tcoffee
			--aligner_args <str>		String of optional arguments to be passed to MSA software
			--pep2nuc					Flag to convert the peptide MSA to nucleotide (retain gaps)
			--nucelotide_dir <str>		Path to directory with nucleotide files
			--nucleotide_ext <str>		Extension of nucleotide files (e.g. '.fa', '.fasta', '.fna')
			--clean_alignments <str>	Flag to use GBlocks to clean MSA outputs
			--gblocs_args <str>			String of optional arguments to be passed to GBlocks
		""".stripIndent()
		println(message)
	}

	public static String printHelpHyphy() {
		def message = ''
		message += """
		HyPhy Arguments:
			--method <str>			Comma separated list of valid methods (fel,slac,fubar,meme,absrel,busted,relax)
			--tree <str>			Comma separated list of tree files
			--fel_optional <str>		String of optional arguments for the 'fel' method
			--slac_optional <str>		String of optional arguments for the 'slac' method
			--fubar_optional <str>		String of optional arguments for the 'fubar' method
			--meme_optional <str>		String of optional arguments for the 'meme' method
			--absrel_optional <str>		String of optional arguments for the 'absrel' method
			--busted_optional <str>		String of optional arguments for the 'busted' method
			--relax_optional <str>		String of optional arguments for the 'relax' method
		""".stripIndent()
		println(message)
	}

	public static String printHelpCodeml() {
		def message = ''
		message += """
		CodeML Arguments:
			--models <str>			String of models to run separated by commas (e.g. 'M1,M2')
							valid models: [M0, M1, M2, M3, M4,
								       M5, M6, M7, M8, M8a,
								       M9, M10, M11, M12, M13,
								       SLR, bsA, bsA1, bsB, bsC,
								       bsD, b_free, b_neut, fb, fb_anc]
			--trees <str>			List of tree files to use
			--tests <str>			String of model comparisons (e.g. 'M2,M1 M3,M0')
			--codeml_optional <str>		Optional paramters for ETE-Evol CodeML
		""".stripIndent()
		println(message)
	}

	public static String printHelpTransCurate() {
		def message = ''
		message += """
		Transcriptome Curation Arguments:
			--cdhit_pid <float>		Percentage identity threshold for collapsing similar transcripts
			--database_dir <str>		Directory path containing pre-compiled BLAST/PFAM databases
			--completeORFs			Flag to keep only complete transcripts (start/stop codons)
		""".stripIndent()
		println(message)
	}

	public static String printHelpGenomeAssembly() {
		def message = ''
		message += """
		Genome Assembly Arguments:
			--
		"""
	}

	public static void callHelp(help, String version) {
		if (help) {
			// Call general help message
			printVersion(version)
			printHelpMessage()
			switch(help) {
				case 'msa':
					printHelpMSA()
					break;
				case 'hyphy':
					printHelpHyphy()
					break;
				case 'codeml':
					printHelpCodeml()
					break;
				case 'transcurate':
					printHelpTransCurate()
					break;
				default:
					printHelpMSA()
					printHelpHyphy()
					printHelpCodeml()
					printHelpTransCurate()
					break;
			}

			def temp_outdir = new File('false')
			if (temp_outdir.exists()) {
				temp_outdir.deleteDir()
			}
			System.exit(0)
		}
	}
}
