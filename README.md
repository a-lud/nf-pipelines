# Nextflow Pipelines

This repository will contain a collection of `Nextflow` pipelines relating to
work that I carry out in my PhD. My goal is to document as many analyses in
standardised workflows to:

1. Better document my code
2. Produce reproducible pipelines
3. Have a central code repository for publication purposes

## Update to pipeline functionality

I've made some significant changes to the functionality of this implicit
pipeline. I'm now using native `groovy` functions to handle a lot of the
behind the scenes input checks/pipeline maintenance. I'm also using a
schema file to define pipeline arguments and types.

### What does this mean?

The `Assembly` pipeline is the first pipeline I've written with these
changes in mind. **If you download the master branch of this repository**,
**only the `Assembly` pipeline can be run/used!** None of the other sub-workflows
have been adapted yet to use the schema/updated parameter checking code.

If you want to use any of the other implemented pipelines, please
download the pipeline at **[THIS][version]** commit version (2f89cd6).
All previous pipelines (*excluding the assembly pipeline*) should work
using this version of the pipeline.

## Wiki

You can acceess the wiki from the tab at the top of this page or from this [link][link].


## Software requirements

I've attempted to use `conda` where I can for software dependencies. Unfortunately I've not been
able to package everything. Ensure the following softare are installed manually:

* `satool` - [GitHub][satool] (used to convert `pin_hic` *SAT* file to *AGP*)

I also note here that many of the scripts in the `bin` directory are not my own and all
credit goes to the authors! I've simply aggregated the scripts I need in this repository.
I've tried to make it clear in the scripts where I've got them if the information is not
already present.

## Getting started

First, ensure you've got `Nextflow` installed with all it's required dependencies. Once you've
checked that, clone this repository, ensuring you also clone all sub-modules e.g. using `HTTPS`

```bash
$ git clone --recurse-submodules https://github.com/a-lud/nf-pipelines.git
```

Once you've got the repository cloned, check that you can run the `main.nf` script. The example
demonstrates the ability to call `--help` for a specific sub-workflow. This will only print
the mandatory arguments along with the arguments for that sub-workflow. Without the trailing
`assembly`, the `--help` command would print all help arguments for each sub-workflow.

```bash
$ nextflow run /path/to/nf-pipelines/main.nf --help assembly
```

An example of the help page

```text
================================================================================
                             NF-PIPELINES 0.0.1
================================================================================

This repository houses a collection of workflows I have developed over the
course of my PhD. They are typically related to analyses that I needed to carry
out at some point or another. Feel free to try them out, adapt them or even use
them as a guide for your own custom pipelines.

---------------------------- Mandatory Arguments -------------------------------

These options MUST be provided to run the pipeline:

--outdir string              Path to the output directory where the results will be saved.
--pipeline string            Specification of which sub-workflow to run. Options: msa, hyphy, codeml, transcurate, assembly, assembly_assessment, repeat.
--partition string           Which HPC partition to use Options: skylake, skylakehm, test.

---------------------------- Assembly pipeline options -------------------------

Genome assembly pipeline arguments.

--input string               Directory path containing the HiFi Fastq file/s.
--assembly string            Which genome assembly output to analyses. Options: primary, haplotype1, haplotype2, haplotypes, all.
--hic string                 Directory path containing the Hi-C Fastq files.
--scaffolder string          Which scaffolding software to use Options: pin_hic, salsa2, all.
--busco_db string            Directory path to a pre-downloaded BUSCO database.
```

If this produces an error, come back here and I'll try and help figure out why it's not working.

## Running the pipeline

To run the pipeline on your own data, generate a *bash* script with a call to the pipeline (see below).

```bash
#!/usr/bin/env bash

# Helps to not overwhelm the head node
export NXF_OPTS="-Xms500M -Xmx2G"

# Can ignore this if you have nextflow available from $PATH
CONDA_BASE=$(conda info --base)
source "${CONDA_BASE}/etc/profile.d/conda.sh"
conda activate nextflow-env

## Pipeline/Directory paths
PIPE="/home/a1234567/hpcfs/software/nf-pipelines"       # Path to where you cloned this repo on your system
OUTDIR="/path/to/assembly-test"                         # Parent directory to pipeline output (see call below)

mkdir -p ${DIR}                                         # Make parent directory

# Call to the Nextflow pipeline
nextflow run ${PIPE}/main.nf \
    -profile 'conda,slurm' \                            # Conda for software/slurm for hpc execution
    -work-dir "${OUTDIR}/test-work" \                   # Manually specifying working directory location
    -with-notification 'first.last@email' \             # Your email. Will send a nicely formatted run-summary
    --outdir "${OUTDIR}" \                              # Output directory location. Sub-directories will be made in here
    --pipeline 'assembly' \                             # Which sub-workflow to run (only assembly is supported currently!)
    --input '/path/to/hifi/data/dir' \                  # Path to directory containing HIFI FASTQ file
    --hic '/path/to/hic/data/dir' \                     # Path to directory containing Hi-C FASTQ file
    --scaffolder 'salsa2'                               # Which scaffolding tool to use
    --assembly 'primary' \                              # Which Hifiasm output to use throughout the pipeline
    --busco_db '/path/to/busco_db/tetrapoda_odb10' \    # Path to pre-downloaded BUSCO database
    --partition 'skylakehm' \                           # Which HPC partition to submit the job to
    -resume                                             # I leave this in. Resume the pipeline if it fails for some reason

conda deactivate
```

I'll typically run this script from a screen environment on the head node.

```bash
$ screen -S nf-assembly         # This will activate a screen environment
$ bash nf-assembly.sh           # Run the bash script (e.g. example above)
```

You should then be greeted by something that looks like the following

```text
N E X T F L O W  ~  version 21.10.0
Launching `/home/a1645424/hpcfs/software/nf-pipelines/main.nf` [kickass_poincare] - revision: efba8a88d7
---------------------------- mandatory -----------------------------------------

outdir                       /path/to/assembly-test
pipeline                     assembly
partition                    skylakehm

---------------------------- nf_arguments --------------------------------------

start                        2022-04-14T10:30:53.253378+09:30
workDir                      /path/to/assembly-test/test-work
profile                      conda,slurm

---------------------------- assembly ------------------------------------------

input
 - path                      /path/to/hifi/data/dir
 - pattern                   *.fastq.gz
 - nfiles                    1
assembly                     primary
hic
 - path                      /path/to/hic/data/dir
 - pattern                   *_R{1,2}.fastq.gz
 - nfiles                    2
scaffolder                   salsa2
busco_db                     /path/to/busco_db/tetrapoda_odb10

---------------------------- cluster -------------------------------------------

partition                    skylakehm
max_memory                   377 GB
max_cpus                     40
max_time                     3d

[67/2844aa] process > ASSEMBLY:hifiadapterfilt (HifiAdapterFilt hydmaj) [  0%] 0 of 1
[-        ] process > ASSEMBLY:seqkit_fq2fa                             -
[-        ] process > ASSEMBLY:hifiasm_hic                              -
[-        ] process > ASSEMBLY:busco_contig                             -
[-        ] process > ASSEMBLY:bwa_mem2_index                           -
[-        ] process > ASSEMBLY:arima_map_filter_combine                 -
[-        ] process > ASSEMBLY:arima_dedup_sort                         -
[-        ] process > ASSEMBLY:matlock_bam2                             -
[-        ] process > ASSEMBLY:pin_hic                                  -
[-        ] process > ASSEMBLY:salsa2                                   -
[-        ] process > ASSEMBLY:busco_salsa2                             -
[-        ] process > ASSEMBLY:busco_pin_hic                            -
[-        ] process > ASSEMBLY:assembly_visualiser_pin_hic              -
[-        ] process > ASSEMBLY:assembly_visualiser_salsa                -
[0a/46267b] process > ASSEMBLY:kmc (KMC hydmaj)                         [  0%] 0 of 1
[-        ] process > ASSEMBLY:genomescope                              -
```

Once it looks like everything is running ok, you can exit the screen environment by pressing `ctrl + a + d`. \
To get back to your screen environment, type `$ screen -r nf-assembly`

## Planned pipelines

I have other pipelines that I've written, however these are **NOT** functional with the current pipeline architecture.
See the message at the beginning of this read-me to see which commit to use for previous pipeline
iterations.

### Implemented
- [x] [Multiple Sequence Alignment][MSA]
- [x] Selection analyses
  - [CodeML (ETE3 implementation)][ETE]
  - [HyPhy][HYPHY]
- [x] [Transcriptome Curation (`CD-HIT` and `TransDecoder`)][TRAN]
- [x] [Assembly][ASSEMBLY]

## Citation

If you use any of the pipelines within this repository, please link to this
repo and that'll be more than enough.

## Author information

Alastair Ludington  
alastair.ludington@adelaide.edu.au  
PhD Candidate, The University of Adelaide

[MSA]: https://github.com/a-lud/nf-pipelines/wiki/Multiple-Sequence-Alignment
[ETE]: https://github.com/a-lud/nf-pipelines/wiki/CodeML---ETE3-implementation
[HYPHY]: https://github.com/a-lud/nf-pipelines/wiki/HyPhy
[TRAN]: https://github.com/a-lud/nf-pipelines/wiki/Trascriptome-Curation
[ASSEMBLY]: https://github.com/a-lud/nf-pipelines/wiki/Genome-Assembly
[version]: https://github.com/a-lud/nf-pipelines/tree/2f89cd605320afe77ce384743ff6cd840ba38bde
[link]: https://github.com/a-lud/nf-pipelines/wiki
[satool]: https://github.com/dfguan/satool