# Nextflow Pipelines

## Table of Contents

<!--ts-->
- [Nextflow Pipelines](#nextflow-pipelines)
  - [Table of Contents](#table-of-contents)
  - [Background](#background)
  - [Accessing old pipelines/versions](#accessing-old-pipelinesversions)
  - [Installation](#installation)
    - [Clone the pipeline repository](#clone-the-pipeline-repository)
    - [Test the installation](#test-the-installation)
  - [External software dependencies](#external-software-dependencies)
  - [Wiki](#wiki)
  - [Citation](#citation)
  - [Author information](#author-information)
<!--te-->

## Background

This repository is a collection of `Nextflow` workflows that I've written relating
to projects I've worked on. This repository acts as the implicit workflow, calling
sub-workflows which are stored in the git submodule `nf-workflows`. The sub-workflows
use 'processes' stored in the `nf-modules` git submodule.

The idea is to have workflows that generally follow best-practices with code that
is publication ready.

## Accessing old pipelines/versions

I've made some significant changes to how my pipelines operate recently. At the time of writing,
the `master` branch of this repository only has a few of the sub-workflows I've implemented.

To access the old version of this pipeline, download [this version (2f89cd6)][VERSION].

## Installation

Before doing anything, ensure you have a working installation of `Nextflow` on your system.
[Visit the installation page][INSTALL] for details on how to do that.

The pipeline also utilises `Conda` for software management. This can be installed following
the instructions provided [here][CONDA].

### Clone the pipeline repository

Clone this repository using the following command

```bash
git clone --recurse-submodules https://github.com/a-lud/nf-pipelines.git
```

**NOTE**: It's important to install the software to a location that has a reasonable amount of storage space.
The pipeline will create a '`conda cache`' directory inside this repository which houses all the environments
used by this pipeline.

### Test the installation

To check that `Nextflow` and the pipeline 'works', try calling the help page

```bash
nextflow run /path/to/nf-pipelines/main.nf --help
```

The pipelines help page can be called using the '`--help`' command. Help pages for specific
sub-workflows can be called by passing the sub-workflow name to the help command.

```bash
nextflow run /path/to/nf-pipelines/main.nf --help assembly
```

If you encounter any errors, come back here and I'll try and help out.

## External software dependencies

I've attempted to use `conda` where I can for software dependencies. Unfortunately I've not been
able to package everything. Ensure the following softare are installed manually:

- `satool` - [GitHub][SATOOL] (used to convert `pin_hic` *SAT* file to *AGP*)

I also note here that many of the scripts in the `bin` directory are not my own and all
credit goes to the authors. I've simply aggregated the scripts I need in this repository.
I've tried to make it clear in the scripts where I've got them if the information is not
already present.

## Wiki

You can acceess the wiki from the tab at the top of this page or from [this link][WIKI].

## Citation

If you use any of the pipelines within this repository, please link to this
repo and that'll be more than enough.

## Author information

Alastair Ludington  
alastair.ludington@adelaide.edu.au  
PhD Candidate, The University of Adelaide

[INSTALL]: https://www.nextflow.io/docs/latest/getstarted.html
[CONDA]: https://docs.conda.io/projects/conda/en/latest/user-guide/install/index.html
[MSA]: https://github.com/a-lud/nf-pipelines/wiki/Multiple-Sequence-Alignment
[ETE]: https://github.com/a-lud/nf-pipelines/wiki/CodeML---ETE3-implementation
[HYPHY]: https://github.com/a-lud/nf-pipelines/wiki/HyPhy
[TRAN]: https://github.com/a-lud/nf-pipelines/wiki/Trascriptome-Curation
[ASSEMBLY]: https://github.com/a-lud/nf-pipelines/wiki/Genome-Assembly
[VERSION]: https://github.com/a-lud/nf-pipelines/tree/2f89cd605320afe77ce384743ff6cd840ba38bde
[WIKI]: https://github.com/a-lud/nf-pipelines/wiki
[SATOOL]: https://github.com/dfguan/satool