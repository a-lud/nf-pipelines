#!/usr/bin/env python3

import argparse
import logging
from cogent3 import load_tree

logging.basicConfig(
    level=logging.INFO, format="%(asctime)s %(levelname)-8s %(message)s"
)


def getArgs():
    """Get user arguments and set up parser"""
    desc = """\
    # -------------------------------------------------------- #
    #                ETE3 Evol output-to-table                 #
    # -------------------------------------------------------- #
    Remove foreground samples from a Newick tree of Site-model
    testing.
    ------------------------------------------------------------
    """

    epi = """\
    Code written by Alastair J. Ludington
    University of Adelaide
    2022
    """

    parser = argparse.ArgumentParser(
        formatter_class=argparse.RawDescriptionHelpFormatter,
        description=desc,
        epilog=epi,
    )

    # Required, positional input file arguments
    parser.add_argument(
        "tree",
        help="Filepath to Newick tree",
        metavar="/path/to/input-tree.nwk",
    )
    parser.add_argument(
        "output", help="Output filename", metavar="/path/to/out-tree.nwk"
    )

    args = parser.parse_args()
    return args


if __name__ == "__main__":
    args = getArgs()

    # Marked tree
    tree = load_tree(args.tree)
    logging.info(f"Original tree:\n")
    print(tree.ascii_art())
    print("\n" + "-" * 100 + "\n")

    # Get oldest marked node
    nodes = [ l for l in tree.get_node_names() if '#' in l ]
    len_tips = 0
    tips = None
    for n in nodes:
        t = tree.get_node_matching_name(n).tips()
        if len_tips < len(t):
            len_tips = len(t)
            tips = t

    # Prune tips from tree (will automatically remove single internal nodes)
    for t in tips:
        print(f"\t- {t.name}")
        t.parent.remove(t)
        tree.prune()

    # Post trimming
    print()
    logging.info("Final tree:")
    print("\n" + tree.ascii_art(show_internal=False))
    print("\n" + "-" * 100 + "\n")

    # Write new tree to file
    tree.write(args.output)
