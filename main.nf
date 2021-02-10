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

    include {MSA} from './nf-workflows/msa' params(params)
    include {HYPHY} from './nf-workflows/hyphy' params(params)

    switch(params.pipeline) {
        case 'msa':
            MSA()
            break;
        case 'hyphy':
            HYPHY()
            break;
        default
            println("Shouldn't get here")
            break;
    }
}