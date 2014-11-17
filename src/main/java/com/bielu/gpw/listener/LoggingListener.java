package com.bielu.gpw.listener;

import java.math.MathContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bielu.gpw.Util;
import com.bielu.gpw.domain.Wallet;

public class LoggingListener implements ChangeListener<Wallet> {

    private static final Log LOG = LogFactory.getLog(LoggingListener.class);
    
    private final Wallet myWallet;
    
    public LoggingListener(Wallet myWallet) {
        this.myWallet = myWallet;
    }
    
    @Override
    public void stateChanged(Wallet source) {
        try {
            printResults(source);
        } catch (RuntimeException e) {
            LOG.error("Unable to print results", e);
        }
    }

    private void printResults(Wallet current) {
        LOG.info(String.format("You invested: %.2f", myWallet.getWalletValue()));
        LOG.info(String.format("You have: %.2f", current.getWalletValue()));
        
        if (myWallet.compareTo(current) > 0) {
            LOG.info(String.format("You lost: %.2f (%.2f%%)", 
                    myWallet.getWalletValue().subtract(current.getWalletValue(), MathContext.DECIMAL32),
                    Util.percentage(myWallet, current)));
        } else {
            LOG.info(String.format("You earned: %.2f (%.2f%%)", 
                    current.getWalletValue().subtract(myWallet.getWalletValue()), 
                    Util.percentage(myWallet, current)));
        }
    }
}
