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
    }
//     if (params.pipeline == 'msa') {
//         include {MSA} from './nf-workflows/msa' params(params)
//         MSA()
//     } else if (params.pipeline == 'hyphy') {
//         include {HYPHY} from './nf-workflows/hyphy' params(params)
//         HYPHY()
//     } else if (params.pipeline == 'codeml') {
//         include {CODEML} from './nf-workflows/codeml' params(params)
//         CODEML()
//     } else if (params.pipeline == 'transcurate') {
//         include {TRANSCURATE} from './nf-workflows/transcurate' params(params)
//         TRANSCURATE()
//     }
}