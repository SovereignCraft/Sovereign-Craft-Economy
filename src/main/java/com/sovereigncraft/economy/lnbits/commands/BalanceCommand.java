package com.sovereigncraft.economy.lnbits.commands;

import com.sovereigncraft.economy.lnbits.LNBitsClient;
import com.sovereigncraft.economy.lnbits.LNBitsUtils;
import com.sovereigncraft.economy.lnbits.LNBitsCacheUsers;

public class BalanceCommand {

    private final LNBitsClient client;

    public BalanceCommand(LNBitsClient client) {
        this.client = client;
    }
}
