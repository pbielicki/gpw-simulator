package com.bielu.gpw.listener;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bielu.gpw.Util;
import com.bielu.gpw.domain.ShareInfo;
import com.bielu.gpw.domain.Wallet;

public class DataWriterListener implements ChangeListener<Wallet> {

    private static final Log LOG = LogFactory.getLog(DataWriterListener.class);
    
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final Wallet myWallet;

    public DataWriterListener(Wallet myWallet) {
        this.myWallet = myWallet;
    }
    
    @Override
    public void stateChanged(Wallet source) {
        try {
            storeResults(source);
        } catch (Exception e) {
            LOG.error("Error while writing data", e);
        }
    }

    private void storeResults(Wallet current) throws IOException {
        FileWriter out = new FileWriter("out.txt", true);
        
        for (int i = 0; i < current.size(); i++) {
            ShareInfo cur = current.getShareInfo(i);
            ShareInfo wal = myWallet.getShareInfo(i);
            if (wal.hasError() == false) {
                out.write(String.format("%s - %s %.3f (%s)\n", 
                        formatter.format(new Date()), 
                        cur.getName(), 
                        cur.getQuote(), 
                        Util.percentagePlus(wal, cur)));
            }
        }
        out.write(String.format("%s: %.2f\n", formatter.format(new Date()), current.getWalletValue()));
        out.close();
    }
}
