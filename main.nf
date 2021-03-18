#!/usr/bin/env nextflow

/*
################################################################################
Nextflow pipeline
################################################################################
*/

nextflow.enable.dsl = 2
version = '0.0.1'

/*
################################################################################
Utility functions
################################################################################
*/

include { callHelp;
          checkRequiredArgs;
          printArguments } from './lib/utils.nf'

/*
################################################################################
Check inputs
################################################################################
*/

callHelp(params, version)
checkRequiredArgs(params)
printArguments(params)

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
    } else if (params.pipeline == 'codeml' ) {
        include {CODEML} from './nf-workflows/codeml' params(params)
        CODEML()
    }
}