#!/usr/bin/env python3

import argparse
import logging
from ete3 import Tree

logging.basicConfig(
    level=logging.INFO, format="%(asctime)s %(levelname)-8s %(message)s"
)


def getArgs():
    """Get user arguments and set up parser"""
    desc = """\
    # -------------------------------------------------------- #
    #              ETE3: Remove foreground branches            #
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

    # Original tree
    logging.info("Importing tree (format 1)")
    tree = Tree(args.tree, format=1)

    logging.info(f"Initial tree:\n{tree}")

    # Find which node to remove based on length
    logging.info("Removing marked Nodes/Leaves")
    for n in tree.traverse('postorder'):
        if '#' in n.name:
            if n.is_leaf():
                n.detach()
            else:
                n.delete()

    # Clean up any internal nodes that have now been made leaves through pruning.
    logging.info("Removing internal nodes that are now leaves without children")
    for n in tree.traverse('postorder'):
        if n.is_leaf() and n.name == '':
            n.detach()

    logging.info("Deleting internal nodes that only have one child (i.e. remove node and keep leaf)")
    for n in tree.traverse('postorder'):
        if not n.is_leaf() and len(n.children) < 2:
            n.delete()

    # Post trimming
    logging.info(f"Final tree:\n{tree}")

    # # Write new tree to file
    tree.write(format=1, outfile=args.output)
