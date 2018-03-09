package com.xrtb.common;

/**
 * A singleton class that holds preshuffled lists of campaigns.
 */

import com.xrtb.tools.XORShiftRandom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton class to hold preshuffled campaigns.
 */
public enum Preshuffle {

    // Instance of the singleton
    INSTANCE;

    // Shuffled list of campaigns
    volatile List<List<Campaign>> list = new ArrayList();
    // Fast random numbers
    XORShiftRandom xor = new XORShiftRandom();

    /**
     * Return the instance of the pre shuffled campaigns.
     * @return Presuffle. This instance.
     */
    public static Preshuffle getInstance() {
        return INSTANCE;
    }

    /**
     * Compile a new list of shuffled campaigns.
     */
    public void compile() {
        list.clear();

        // Get the effective campaigns.
        List<Campaign> org = Configuration.getInstance().getCampaignsList();

        for (int i=0; i<org.size();i++) {
            List<Campaign> x = new ArrayList(org);
            Collections.shuffle(x);
            list.add(x);
        }
    }

    /**
     * Return a shuffled list of campaigns.
     * @return List. A list of campaigns to use in selection of a campaign.
     */
    public List<Campaign> getPreShuffledCampaignList() {
        int x = xor.random(list.size());
        return list.get(x);
    }
}
