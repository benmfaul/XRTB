package com.xrtb.common;

import java.util.Comparator;

/**
 * Sorts nodes in descending order by the number of times the node has returned no-bid (false).
 */

public class SortNodesFalseCount implements Comparator<Node> {
    public int compare(Node a, Node b)
    {
        if (a.getFalseCount() == b.getFalseCount())
            return 0;
        if (a.getFalseCount() < b.getFalseCount()) {
            return 1;
        }

        return -1;

    }
}
