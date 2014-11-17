package com.bielu.gpw;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bielu.gpw.domain.ShareInfo;
import com.bielu.gpw.domain.Wallet;

public class GpwMonitor implements Closable {

    private static final String SHARES_DB_FILE = "/shares.db";
    private static final Log LOG = LogFactory.getLog(GpwMonitor.class);
    private final ScheduledExecutorService service;
    private final GpwTray gpwTray;
    private final SimpleDateFormat format;
    private File dbFile;
    private boolean exiting = false;

    protected GpwMonitor(CountDownLatch latch) {
        gpwTray = new GpwTray(this, latch);
        service = Executors.newScheduledThreadPool(2);
        format = new SimpleDateFormat("yyyy-MM-dd");
    }

    @Override
    public void close() {
        if (exiting  == true) {
            return;
        }
        exiting = true;
        LOG.info("Exiting application");
        gpwTray.close();
        service.shutdownNow();

        try {
            if (!service.awaitTermination(2, TimeUnit.SECONDS)) {
                LOG.info("Exiting forcefully");
                //System.exit(-1);
            }
        } catch (InterruptedException e) {
            LOG.info("Awaiting interrupted - exiting forcefully");
            System.exit(-1);
        }
    }

    protected GpwTray getGpwTray() {
        return gpwTray;
    }

    protected Wallet createWallet() {
        try {
            LineNumberReader in = new LineNumberReader(new InputStreamReader(findDb()));
            final List<ShareInfo> shares = new ArrayList<ShareInfo>();

            String line = null;
            while ((line = in.readLine()) != null) {
                String[] tmp = line.split(",");
                ShareInfo share = ShareInfo.newInstanceFromSharesCount(tmp[0].trim(),
                        Double.parseDouble(tmp[1].trim()),
                        Integer.parseInt(tmp[2].trim()),
                        format.parse(tmp[3].trim()));

                if (shares.contains(share)) {
                    ShareInfo prev = shares.get(shares.indexOf(share));
                    shares.remove(prev);
                    shares.add(ShareInfo.merge(share, prev));
                } else {
                    shares.add(share);
                }
            }
            in.close();
            return new Wallet(shares);
        } catch (Exception e) {
            LOG.error("Unable to read Wallet data.", e);
            return null;
        }
    }

    private InputStream findDb() throws FileNotFoundException {
        InputStream stream = Class.class.getResourceAsStream(SHARES_DB_FILE);
        if (stream == null) {
            String file = System.getProperty("user.dir") + SHARES_DB_FILE;
            stream = new FileInputStream(file);
            dbFile = new File(file);
        }
        return stream;
    }

    public long getLastDbModification() {
        if (dbFile == null) {
            return 0;
        }
        return dbFile.lastModified();
    }

    public void scheduleAtFixedRate(Runnable command, long initDelay, int period, TimeUnit unit) {
        service.scheduleAtFixedRate(command, initDelay, period, unit);
    }
}
