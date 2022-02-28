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
