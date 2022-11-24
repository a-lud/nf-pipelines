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
    #           ETE3: Branch lengths to marked tree            #
    # -------------------------------------------------------- #
    Add branch lengths to a marked tree. The branch lengths
    typically will come from running the M0 model from CodeML.
    Incorportating the branch length estimates from M0 should
    help more complex CodeML models run.
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
        "tree_marked",
        help="Filepath to Newick tree",
        metavar="/path/to/tree-marked.nwk",
    )

    parser.add_argument(
        "tree_lengths",
        help="Filepath to Newick tree",
        metavar="/path/to/tree-lengths.nwk",
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
    mtree = Tree(args.tree_marked, format=1)
    ltree = Tree(args.tree_lengths, format=1)

    # The trees are identical bar the '#1' in the labels. Therefore, if we
    # iterate over them the same way each time we'll get the same index value.
    logging.info("Identifying marked branches")
    marked = {}
    i = 0
    for node in mtree.traverse("postorder"):
        m = True if "#1" in node.name else False
        marked[i] = [m]
        i += 1

    # appending distance information
    logging.info("Parsing branch length information")
    i = 0
    for node in ltree.traverse("postorder"):
        marked[i].append(node.dist)
        i += 1

    logging.info("Updating tree")
    i = 0
    for node in mtree.traverse("postorder"):
        if marked[i][0]:
            name = node.name.replace("#1", "").rstrip() if node.name != "" else ""
            bl = marked[i][1]
            node.name = f"{name}__{bl} #1"
        else:
            name = node.name if node.name != "" else ""
            bl = marked[i][1]
            node.name = f"{name}__{bl}"
        i += 1

    # Writing to file
    logging.info("Writing updated tree to file")
    with open(args.output, "w") as file:
        file.write(mtree.write(format=8).replace("__", ": "))
