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


## Getting started

First, ensure you've got `Nextflow` installed with all it's required dependencies. Then:

1. Clone this repository to a location on you're happy with on your system.
2. Check that the help page works: `nextflow run main.nf --help`

Check back here if the help page doesn't work and I'll try and help troubleshoot.

Then, write a simple bash script with the call to the pipeline. Save it to a file e.g.
`nf-assembly.sh`.

```bash
#!/usr/bin/env bash

# Helps not use too many resources on the head node
export NXF_OPTS="-Xms500M -Xmx2G"

CONDA_BASE=$(conda info --base)
source "${CONDA_BASE}/etc/profile.d/conda.sh"
conda activate nextflow-env

## Location of where the pipeline is installed
PIPE="/home/a1234567/hpcfs/software/nf-pipelines"
DIR="/home/a1234567/hpcfs/assembly-test"

mkdir -p ${DIR}

nextflow run ${PIPE}/main.nf \
    -profile 'conda,slurm' \
    -work-dir "${DIR}/test-work" \
    -N 'first.last@adelaide.edu.au' \
    -resume \
    --outdir "/home/a1234567/hpcfs/assembly-test/test-out" \
    --email 'first.last@adelaide.edu.au' \
    --pipeline 'assembly' \
    --input '/home/a1234567/hpcfs/data/hifi' \
    --hic '/home/a1234567/hpcfs/data/hic' \
    --assembler 'hifiasm' \
    --busco_db '/home/a1234567/hpcfs/busco_downloads/lineages/tetrapoda_odb10' \
    --partition 'skylakehm'

conda deactivate
```

I'll typically run this script from a screen environment on the head node.

```bash
$ screen -S nf-assembly         # This will activate a screen environment
$ bash nf-assembly.sh
```

You should then be greeted by something that looks like the following

```
---------------------------- mandatory -----------------------------------------

outdir                       /home/a1234567/hpcfs/assembly-test/test-out
email                        first.last@adelaide.edu.au
pipeline                     assembly
partition                    skylakehm

---------------------------- nf_arguments --------------------------------------

start                        2022-02-28T10:16:40.568271+10:30
workDir                      /home/a1234567/hpcfs/assembly-test/test-work
profile                      conda,slurm

---------------------------- assembly ------------------------------------------

input
 - path                      /home/a1234567/hpcfs/data/hifi
 - pattern                   *.fastq.gz
 - nfiles                    1
assembler                    hifiasm
hic
 - path                      /home/a1234567/hpcfs/data/hic
 - pattern                   *_R{1,2}.fastq.gz
 - nfiles                    2
busco_db                     /home/a1234567/hpcfs/busco_downloads/lineages/tetrapoda_odb10

---------------------------- cluster -------------------------------------------

partition                    skylakehm
max_memory                   377 GB
max_cpus                     40
max_time                     3d

[a6/b0998e] process > ASSEMBLY:seqkit_fq2fa (seqkit hydmaj)                   [0%] 0 of 1,
[90/4f585f] process > ASSEMBLY:hifiasm_hic (hifiasm hydmaj)                   [0%] 0 of 1,
[]          process > ASSEMBLY:busco_contig (BUSCO hydmaj-hap1 contig)
[]          process > ASSEMBLY:bwa_mem2_index (BWA Index hydmaj-hap1)
[]          process > ASSEMBLY:bwa_mem2_mem (bwa_mem2_mem hydmaj-hap1 350845)
[]          process > ASSEMBLY:pin_hic (pin_hic - hydmaj-hap1)
[]          process > ASSEMBLY:tgsgapcloser (TGS-GapCloser hydmaj-hap2)
[]          process > ASSEMBLY:minimap2_pb_hifi (Minimap2 hydmaj-hap2)
[]          process > ASSEMBLY:mosdepth (Mosdepth hydmaj-hap2)
[]          process > ASSEMBLY:merqury (Merqury K-mer)
[]          process > ASSEMBLY:busco_scaffold (BUSCO hydmaj-hap2 scaffold)
[]          process > ASSEMBLY:quast (QUAST)
[]          process > ASSEMBLY:busco_plot (BUSCO plot)
[28/bb9b97] process > ASSEMBLY:kmc (KMC hydmaj)                               [0%] 0 of 1,
[]          process > ASSEMBLY:genomescope (GenomeScope hydmaj)
```

## Planned pipelines

Pipelines I've implemented/plan to write include.

### Implemented
- [x] [Multiple Sequence Alignment][MSA]
- [x] Selection analyses
  - [CodeML (ETE3 implementation)][ETE]
  - [HyPhy][HYPHY]
- [x] [Transcriptome Curation (`CD-HIT` and `TransDecoder`)][TRAN]
- [x] [Assembly][ASSEMBLY]

### To be written
- [ ] Read Based Phasing (`Whatshap`)
- [ ] Demographic Analyses (`PSMC`, `MSMC`, `MSMC-IM`)
- [ ] Subtree Topology Weighting (`Twisst`)
- [ ] Admixture Tests

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