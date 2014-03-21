/* Copyright 2011, 2012 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.onionoo;

import java.io.File;

/* Update search data and status data files. */
public class Main {

  private Main() {
  }

  public static void main(String[] args) {

    LockFile lf = new LockFile();
    Logger.setTime();
    Logger.printStatus("Initializing.");
    if (lf.acquireLock()) {
      Logger.printStatusTime("Acquired lock");
    } else {
      Logger.printStatusTime("Could not acquire lock.  Is Onionoo "
          + "already running?  Terminating");
      return;
    }

    DescriptorSource dso = ApplicationFactory.getDescriptorSource();
    Logger.printStatusTime("Initialized descriptor source");
    DocumentStore ds = ApplicationFactory.getDocumentStore();
    Logger.printStatusTime("Initialized document store");
    LookupService ls = new LookupService(new File("geoip"));
    Logger.printStatusTime("Initialized Geoip lookup service");
    ReverseDomainNameResolver rdnr = new ReverseDomainNameResolver();
    Logger.printStatusTime("Initialized reverse domain name resolver");
    NodeDetailsStatusUpdater ndsu = new NodeDetailsStatusUpdater(rdnr,
        ls);
    Logger.printStatusTime("Initialized node data writer");
    BandwidthStatusUpdater bsu = new BandwidthStatusUpdater();
    Logger.printStatusTime("Initialized bandwidth status updater");
    WeightsStatusUpdater wsu = new WeightsStatusUpdater();
    Logger.printStatusTime("Initialized weights status updater");
    ClientsStatusUpdater csu = new ClientsStatusUpdater();
    Logger.printStatusTime("Initialized clients status updater");
    UptimeStatusUpdater usu = new UptimeStatusUpdater();
    Logger.printStatusTime("Initialized uptime status updater");
    StatusUpdater[] sus = new StatusUpdater[] { ndsu, bsu, wsu, csu,
        usu };

    DetailsDocumentWriter ddw = new DetailsDocumentWriter();
    Logger.printStatusTime("Initialized details document writer");
    BandwidthDocumentWriter bdw = new BandwidthDocumentWriter();
    Logger.printStatusTime("Initialized bandwidth document writer");
    WeightsDocumentWriter wdw = new WeightsDocumentWriter();
    Logger.printStatusTime("Initialized weights document writer");
    ClientsDocumentWriter cdw = new ClientsDocumentWriter();
    Logger.printStatusTime("Initialized clients document writer");
    UptimeDocumentWriter udw = new UptimeDocumentWriter();
    Logger.printStatusTime("Initialized uptime document writer");
    DocumentWriter[] dws = new DocumentWriter[] { ddw, bdw, wdw, cdw,
        udw };

    Logger.printStatus("Reading descriptors.");
    dso.readRelayNetworkConsensuses();
    Logger.printStatusTime("Read relay network consensuses");
    dso.readRelayServerDescriptors();
    Logger.printStatusTime("Read relay server descriptors");
    dso.readRelayExtraInfos();
    Logger.printStatusTime("Read relay extra-info descriptors");
    dso.readExitLists();
    Logger.printStatusTime("Read exit lists");
    dso.readBridgeNetworkStatuses();
    Logger.printStatusTime("Read bridge network statuses");
    dso.readBridgeServerDescriptors();
    Logger.printStatusTime("Read bridge server descriptors");
    dso.readBridgeExtraInfos();
    Logger.printStatusTime("Read bridge extra-info descriptors");
    dso.readBridgePoolAssignments();
    Logger.printStatusTime("Read bridge-pool assignments");

    Logger.printStatus("Updating internal status files.");
    for (StatusUpdater su : sus) {
      su.updateStatuses();
    }

    Logger.printStatus("Updating document files.");
    for (DocumentWriter dw : dws) {
      dw.writeDocuments();
    }

    Logger.printStatus("Shutting down.");
    dso.writeHistoryFiles();
    Logger.printStatusTime("Wrote parse histories");
    ds.flushDocumentCache();
    Logger.printStatusTime("Flushed document cache");

    Logger.printStatus("Gathering statistics.");
    for (StatusUpdater su : sus) {
      String statsString = su.getStatsString();
      if (statsString != null) {
        Logger.printStatistics(su.getClass().getSimpleName(),
            statsString);
      }
    }
    for (DocumentWriter dw : dws) {
      String statsString = dw.getStatsString();
      if (statsString != null) {
        Logger.printStatistics(dw.getClass().getSimpleName(),
            statsString);
      }
    }
    Logger.printStatistics("Descriptor source", dso.getStatsString());
    Logger.printStatistics("Document store", ds.getStatsString());
    Logger.printStatistics("GeoIP lookup service", ls.getStatsString());
    Logger.printStatistics("Reverse domain name resolver",
        rdnr.getStatsString());

    Logger.printStatus("Releasing lock.");
    if (lf.releaseLock()) {
      Logger.printStatusTime("Released lock");
    } else {
      Logger.printStatusTime("Could not release lock.  The next "
          + "execution may not start as expected");
    }

    Logger.printStatus("Terminating.");
  }
}

