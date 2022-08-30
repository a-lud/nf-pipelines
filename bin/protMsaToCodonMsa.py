#!/usr/bin/env python3

# from asyncio import subprocess
import os
import argparse
import logging
import subprocess
from shutil import rmtree
from pathlib import Path
from timeit import default_timer as timer

from pyfaidx import Fasta

# --------------------------------------------------------------------------- #
# Logging information
logging.basicConfig(
    level=logging.INFO, format="%(asctime)s %(threadName)s %(levelname)-8s %(message)s"
)


def parse_arguments():
    desc = """\
    # -------------------------------------------------------- #
    #                 Protein MSA to Codon MSA                 #
    # -------------------------------------------------------- #
    This script conducts the following:
        1. Creates a CDS multi-fasta matching the prot alignment
        2. Rename sequence identifiers with species names
        3. pal2nal to convert protein alignment to codon
    
    Gene sequences in nucleotide format are expected to have the
    same sequence identifiers as their protein equivalents that
    were used to make the MSA files.

    Nucleotide sequence files should also take the form
    '<species_name>.f*' as this script assigns species name from
    the files basename.
    ------------------------------------------------------------
    """

    parser = argparse.ArgumentParser(
        formatter_class=argparse.RawDescriptionHelpFormatter,
        description=desc,
    )

    parser.add_argument(
        "-f",
        "--fasta",
        help="Directory containing gene sequence multi-fasta files",
        metavar="<path>",
        type=str,
    )

    parser.add_argument(
        "-m",
        "--msa",
        help="Directory path containing the MSA files generated by OrthoFinder",
        metavar="<path>",
        type=str,
    )

    parser.add_argument(
        "-o",
        "--outdir",
        help="Output directory path",
        metavar="<path>",
        type=str,
    )

    args = parser.parse_args()
    return args

# createOutdirs


def createOutdirs(path):
    if Path(path).is_dir():
        if len(os.listdir(path)) != 0:
            logging.info(f"[Path::rmdir] Overwriting {path}")
            rmtree(path)
            Path(path).mkdir(parents=True, exist_ok=True)
    else:
        logging.info(f"[Path::mkdir] Creating {path}")
        Path(path).mkdir(parents=True, exist_ok=True)

# getFilesInDir get the files in the current directory


def getFiles(path, ext):
    '''Return the files in the current directory as a list'''
    lst = [
        os.path.join(path, f)
        for f in os.listdir(path)
        if f.endswith(ext)
        if os.path.isfile(os.path.join(path, f))
    ]

    return lst

# getKeys get sequence identifiers from MSA files
# { species_name: [ cds_headers, Fasta() object ] }
def getKeys(file):
    genes = Fasta(file)
    fa_header = genes.keys()

    kv = {os.path.basename(file).split('.', 1)[0]: [fa_header, genes]}
    return kv

# makeCdsDict return a dictionary of the form: { spec: [ headers, Fasta() object ] }
def makeCdsDict(path):
    # Get sequence identifiers in each CDS file
    cds_keys = [getKeys(f) for f in getFiles(path, '.cds')]
    cds_keys_dict = {k: v for d in cds_keys for k, v in d.items()}
    return cds_keys_dict

# makeInputFiles create CDS multi-fasta file + clean up headers in protein MSA
def makeInputFiles(msa, cds, out_cds, out_clean):
    # Ortholog identifier
    orthid = os.path.basename(msa).split('.')[0]

    # Rename MSA headers using 'matches' dict
    with open(msa, 'r') as reader:
        msadata = reader.read()

    # PyFaidx object to get seqid's + sequences
    genes = Fasta(msa)
    msa_keys = genes.keys()

    # Write CDS to file + create dict to rename input MSA (prot)
    # matches: { species: read id }
    for l in msa_keys:
        spec = "_".join(l.split('_', 2)[:2]) # species name is prefix (from .faa file basename)
        readid = l.replace(spec + '_', '')

        # Rename MSA file - headers are now species identifiers
        msadata = msadata.replace(l, spec)

        # TODO - this should be temporary - SANITIZE headers before final run
        if 'SOZL' in readid:
            readid = readid.replace('WGS_SOZL', "WGS:SOZL")
        
        # Subset Fasta() object for CDS sequence + write to file
        seq = cds[spec][1][readid]
        with open(os.path.join(out_cds, orthid + '.fa'), 'a') as out:
            out.write(f">{spec}\n{seq}\n")

    # Write updated MSA to file
    with open(os.path.join(out_clean, orthid + '.clean'), 'w') as clean:
        clean.write(msadata)

# makeCodonAln make codon MSA file
def makeCodonAln(cds_path, msa_path, codon_path, log_path):
    logging.info("[makeCodonAln] Creating codon alignments")
    cds_files = getFiles(cds_path, '.fa')
    
    for cds in cds_files:
        bn = os.path.basename(cds).split('.')[0]
        cln_msa = os.path.join(msa_path, bn + '.clean')
        out = os.path.join(codon_path, bn + '.fa')
        stderrlog = os.path.join(log_path, 'pal2nal.log')

        # Try running PAL2NAL as sub-process
        cmd = [
            'pal2nal.pl',
            cln_msa,
            cds,
            '-output',
            'fasta'
        ]
        try:
            with open(stderrlog, 'a') as log, open(out, 'w') as outfile:
                subprocess.run(
                    cmd,
                    check=True,
                    stdout=outfile,
                    stderr=log,
                    universal_newlines=True
                )
        except subprocess.CalledProcessError as error:
            logging.error(f"[makeCodonAln] {bn} failed")
            print(
                f"\nCommand: {error.cmd}\nExit code: {error.returncode}\nStderr: {error.stderr}"
            )
            exit()


if __name__ == "__main__":
    start = timer()

    # ----------------------------------------------------------------------- #
    # Arguments
    args = parse_arguments()

    # ----------------------------------------------------------------------- #
    # Create output directory
    cds_path = os.path.join(args.outdir, 'cds')
    cln_path = os.path.join(args.outdir, 'clean')
    codon_path = os.path.join(args.outdir, 'codon_alignments')
    log_path = os.path.join(args.outdir, 'logs')

    createOutdirs(cds_path)
    createOutdirs(cln_path)
    createOutdirs(codon_path)
    createOutdirs(log_path)

    # ----------------------------------------------------------------------- #
    # Create dict of cds files with their corresponding headers + Fasta() obj
    cds_dict = makeCdsDict(args.fasta)

    # ----------------------------------------------------------------------- #
    # Write CDS multi-fasta
    files = getFiles(path=args.msa, ext='.fa')

    logging.info(f"[makeInputFiles] CDS-fasta + clean MSA")
    for msa in files:
        makeInputFiles(msa=msa, cds=cds_dict,
                       out_cds=cds_path, out_clean=cln_path)

    # ----------------------------------------------------------------------- #
    # Run PAL2NAL
    makeCodonAln(cds_path, cln_path, codon_path, log_path)

    # ----------------------------------------------------------------------- #
    # Pipeline run-time
    end = timer()
    time = end - start
    logging.info(f"[Timer::total] {time}")
