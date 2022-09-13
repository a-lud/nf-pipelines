#!/usr/bin/env python3

import argparse
from sys import exit
from cogent3 import load_unaligned_seqs, load_aligned_seqs


def pep2nuc(id, pep, nuc):
    pro = load_aligned_seqs(pep, format="fasta", moltype="protein")
    dna = load_unaligned_seqs(nuc, format="fasta", moltype="dna")

    try:
        dna_noStop = dna.trim_stop_codons()
        print("Trim stop complete: " + pep)
    except ValueError as er:
        print("\nERROR: Couldn't trim stop codons for sample " + id)
        print("Protein file: " + pep + "\nNucleotide file: " + nuc)
        print(er)
        exit()
        # pass

    # Replace sequences
    aln_dna = pro.replace_seqs(dna_noStop, aa_to_codon=True)

    # Write to file
    aln_dna.write(id + "_translated.fasta", format="fasta")


def main():
    # Arguments
    parser = argparse.ArgumentParser(
        description="Convert peptide aligned sequences to nucleotide, retaining gaps"
    )

    parser.add_argument(
        "-n",
        "--nucleotide",
        help="File path to nucleotide fasta file",
        required=True,
        type=str,
    )
    parser.add_argument(
        "-p",
        "--peptide",
        help="File path to peptide fasta file",
        required=True,
        type=str,
    )
    parser.add_argument(
        "-i", "--identifier", help="Sample identifier", required=True, type=str
    )

    args = parser.parse_args()

    pep2nuc(id=args.identifier, pep=args.peptide, nuc=args.nucleotide)


if __name__ == "__main__":
    main()
