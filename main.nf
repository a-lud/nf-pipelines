#!/usr/bin/env nextflow

/*
################################################################################
Nextflow pipeline
################################################################################
*/

nextflow.enable.dsl = 2
version = '0.1.0'

/*
################################################################################
Check inputs
################################################################################
*/

WorkflowMain.callHelp(params.help, version)
WorkflowArguments.checkArguments(params, workflow.profile)

/*
################################################################################
Main workflow
################################################################################
*/

workflow {
    if (params.pipeline == 'msa') {
        include {MSA} from './nf-workflows/msa' params(params)
        MSA()
    } else if (params.pipeline == 'hyphy') {
        include {HYPHY} from './nf-workflows/hyphy' params(params)
        HYPHY()
    } else if (params.pipeline == 'codeml') {
        include {CODEML} from './nf-workflows/codeml' params(params)
        CODEML()
    } else if (params.pipeline == 'transcurate') {
        include {TRANSCURATE} from './nf-workflows/transcurate' params(params)
        TRANSCURATE()
    }
}