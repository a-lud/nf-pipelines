#!/usr/bin/env nextflow

/*
################################################################################
Nextflow pipeline
################################################################################
*/

nextflow.enable.dsl = 2

/*
################################################################################
Check inputs
################################################################################
*/

// Validate/Help page
checkedArgs = WorkflowMain.initialise(params, workflow)

/*
################################################################################
Main workflow
################################################################################
*/

include {QC} from './nf-workflows/qc' params(checkedArgs)
include {ASSEMBLY} from "./nf-workflows/assembly" params(checkedArgs)
include {ASSEMBLY_ASSESSMENT} from './nf-workflows/assembly_assessment' params(checkedArgs)
include {ALIGNMENT} from './nf-workflows/alignment' params(checkedArgs)
include {CONSENSUS} from './nf-workflows/consensus' params(checkedArgs)
include {CODEML} from './nf-workflows/codeml' params(checkedArgs)

workflow {
    switch(checkedArgs.pipeline) {
        case 'qc':
            QC()
            break;
        case 'assembly':
            ASSEMBLY()
            break;
        case 'assembly_assessment':
            ASSEMBLY_ASSESSMENT()
            break;
        case 'alignment':
            ALIGNMENT()
            break;
        case 'consensus':
            CONSENSUS()
            break;
        case 'codeml':
            CODEML()
            break;
    }
}