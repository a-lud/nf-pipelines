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

include {ASSEMBLY} from "./nf-workflows/assembly" params(checkedArgs)

workflow {
    switch(checkedArgs.pipeline) {
        case 'assembly':
            ASSEMBLY()
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