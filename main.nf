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
include {VARIANT} from './nf-workflows/variant' params(checkedArgs)
include {PSMC} from './nf-workflows/psmc' params(checkedArgs)
include {ORTHOFINDER} from './nf-workflows/orthofinder' params(checkedArgs)
include {CODEML} from './nf-workflows/codeml' params(checkedArgs)
include {HYPHY} from './nf-workflows/hyphy' params(checkedArgs)
include {HYPHY_ANALYSES} from './nf-workflows/hyphy_analyses' params(checkedArgs)

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
        case 'variant':
            VARIANT()
            break;
        case 'psmc':
            PSMC()
            break;
        case 'orthofinder':
            ORTHOFINDER()
            break;
        case 'codeml':
            CODEML()
            break;
        case 'hyphy':
            HYPHY()
            break;
        case 'hyphy_analyses':
            HYPHY_ANALYSES()
            break;
    }
}