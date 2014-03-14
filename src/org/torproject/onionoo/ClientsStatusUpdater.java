/* Copyright 2014 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.onionoo;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.ExtraInfoDescriptor;

/*
 * Example extra-info descriptor used as input:
 *
 * extra-info ndnop2 DE6397A047ABE5F78B4C87AF725047831B221AAB
 * dirreq-stats-end 2014-02-16 16:42:11 (86400 s)
 * dirreq-v3-resp ok=856,not-enough-sigs=0,unavailable=0,not-found=0,
 *   not-modified=40,busy=0
 * bridge-stats-end 2014-02-16 16:42:17 (86400 s)
 * bridge-ips ??=8,in=8,se=8
 * bridge-ip-versions v4=8,v6=0
 *
 * Clients status file produced as intermediate output:
 *
 * 2014-02-15 16:42:11 2014-02-16 00:00:00
 *   259.042 in=86.347,se=86.347  v4=259.042
 * 2014-02-16 00:00:00 2014-02-16 16:42:11
 *   592.958 in=197.653,se=197.653  v4=592.958
 */
public class ClientsStatusUpdater implements DescriptorListener,
    StatusUpdater {

  private DescriptorSource descriptorSource;

  private DocumentStore documentStore;

  private long now;

  public ClientsStatusUpdater(DescriptorSource descriptorSource,
      DocumentStore documentStore, Time time) {
    this.descriptorSource = descriptorSource;
    this.documentStore = documentStore;
    this.now = time.currentTimeMillis();
    this.registerDescriptorListeners();
  }

  private void registerDescriptorListeners() {
    this.descriptorSource.registerDescriptorListener(this,
        DescriptorType.BRIDGE_EXTRA_INFOS);
  }

  public void processDescriptor(Descriptor descriptor, boolean relay) {
    if (descriptor instanceof ExtraInfoDescriptor && !relay) {
      this.processBridgeExtraInfoDescriptor(
          (ExtraInfoDescriptor) descriptor);
    }
  }

  private static final long ONE_HOUR_MILLIS = 60L * 60L * 1000L,
      ONE_DAY_MILLIS = 24L * ONE_HOUR_MILLIS;

  private SortedMap<String, SortedSet<ClientsHistory>> newResponses =
      new TreeMap<String, SortedSet<ClientsHistory>>();

  private void processBridgeExtraInfoDescriptor(
      ExtraInfoDescriptor descriptor) {
    long dirreqStatsEndMillis = descriptor.getDirreqStatsEndMillis();
    long dirreqStatsIntervalLengthMillis =
        descriptor.getDirreqStatsIntervalLength() * 1000L;
    SortedMap<String, Integer> responses = descriptor.getDirreqV3Resp();
    if (dirreqStatsEndMillis < 0L ||
        dirreqStatsIntervalLengthMillis != ONE_DAY_MILLIS ||
        responses == null || !responses.containsKey("ok")) {
      return;
    }
    double okResponses = (double) (responses.get("ok") - 4);
    if (okResponses < 0.0) {
      return;
    }
    String hashedFingerprint = descriptor.getFingerprint().toUpperCase();
    long dirreqStatsStartMillis = dirreqStatsEndMillis
        - dirreqStatsIntervalLengthMillis;
    long utcBreakMillis = (dirreqStatsEndMillis / ONE_DAY_MILLIS)
        * ONE_DAY_MILLIS;
    for (int i = 0; i < 2; i++) {
      long startMillis = i == 0 ? dirreqStatsStartMillis : utcBreakMillis;
      long endMillis = i == 0 ? utcBreakMillis : dirreqStatsEndMillis;
      if (startMillis >= endMillis) {
        continue;
      }
      double totalResponses = okResponses
          * ((double) (endMillis - startMillis))
          / ((double) ONE_DAY_MILLIS);
      SortedMap<String, Double> responsesByCountry =
          this.weightResponsesWithUniqueIps(totalResponses,
          descriptor.getBridgeIps(), "??");
      SortedMap<String, Double> responsesByTransport =
          this.weightResponsesWithUniqueIps(totalResponses,
          descriptor.getBridgeIpTransports(), "<??>");
      SortedMap<String, Double> responsesByVersion =
          this.weightResponsesWithUniqueIps(totalResponses,
          descriptor.getBridgeIpVersions(), "");
      ClientsHistory newResponseHistory = new ClientsHistory(
          startMillis, endMillis, totalResponses, responsesByCountry,
          responsesByTransport, responsesByVersion); 
      if (!this.newResponses.containsKey(hashedFingerprint)) {
        this.newResponses.put(hashedFingerprint,
            new TreeSet<ClientsHistory>());
      }
      this.newResponses.get(hashedFingerprint).add(
          newResponseHistory);
    }
  }

  private SortedMap<String, Double> weightResponsesWithUniqueIps(
      double totalResponses, SortedMap<String, Integer> uniqueIps,
      String omitString) {
    SortedMap<String, Double> weightedResponses =
        new TreeMap<String, Double>();
    int totalUniqueIps = 0;
    if (uniqueIps != null) {
      for (Map.Entry<String, Integer> e : uniqueIps.entrySet()) {
        if (e.getValue() > 4) {
          totalUniqueIps += e.getValue() - 4;
        }
      }
    }
    if (totalUniqueIps > 0) {
      for (Map.Entry<String, Integer> e : uniqueIps.entrySet()) {
        if (!e.getKey().equals(omitString) && e.getValue() > 4) {
          weightedResponses.put(e.getKey(),
              (((double) (e.getValue() - 4)) * totalResponses)
              / ((double) totalUniqueIps));
        }
      }
    }
    return weightedResponses;
  }

  public void updateStatuses() {
    for (Map.Entry<String, SortedSet<ClientsHistory>> e :
        this.newResponses.entrySet()) {
      String hashedFingerprint = e.getKey();
      ClientsStatus clientsStatus = this.documentStore.retrieve(
          ClientsStatus.class, true, hashedFingerprint);
      if (clientsStatus == null) {
        clientsStatus = new ClientsStatus();
      }
      this.addToHistory(clientsStatus, e.getValue());
      this.compressHistory(clientsStatus);
      this.documentStore.store(clientsStatus, hashedFingerprint);
    }
    Logger.printStatusTime("Updated clients status files");
  }

  private void addToHistory(ClientsStatus clientsStatus,
      SortedSet<ClientsHistory> newIntervals) {
    SortedSet<ClientsHistory> history = clientsStatus.history;
    for (ClientsHistory interval : newIntervals) {
      if ((history.headSet(interval).isEmpty() ||
          history.headSet(interval).last().endMillis <=
          interval.startMillis) &&
          (history.tailSet(interval).isEmpty() ||
          history.tailSet(interval).first().startMillis >=
          interval.endMillis)) {
        history.add(interval);
      }
    }
  }

  private void compressHistory(ClientsStatus clientsStatus) {
    SortedSet<ClientsHistory> history = clientsStatus.history;
    SortedSet<ClientsHistory> compressedHistory =
        new TreeSet<ClientsHistory>();
    ClientsHistory lastResponses = null;
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM");
    dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    String lastMonthString = "1970-01";
    for (ClientsHistory responses : history) {
      long intervalLengthMillis;
      if (this.now - responses.endMillis <=
          92L * 24L * 60L * 60L * 1000L) {
        intervalLengthMillis = 24L * 60L * 60L * 1000L;
      } else if (this.now - responses.endMillis <=
          366L * 24L * 60L * 60L * 1000L) {
        intervalLengthMillis = 2L * 24L * 60L * 60L * 1000L;
      } else {
        intervalLengthMillis = 10L * 24L * 60L * 60L * 1000L;
      }
      String monthString = dateTimeFormat.format(responses.startMillis);
      if (lastResponses != null &&
          lastResponses.endMillis == responses.startMillis &&
          ((lastResponses.endMillis - 1L) / intervalLengthMillis) ==
          ((responses.endMillis - 1L) / intervalLengthMillis) &&
          lastMonthString.equals(monthString)) {
        lastResponses.addResponses(responses);
      } else {
        if (lastResponses != null) {
          compressedHistory.add(lastResponses);
        }
        lastResponses = responses;
      }
      lastMonthString = monthString;
    }
    if (lastResponses != null) {
      compressedHistory.add(lastResponses);
    }
    clientsStatus.history = compressedHistory;
  }

  public String getStatsString() {
    int newIntervals = 0;
    for (SortedSet<ClientsHistory> hist : this.newResponses.values()) {
      newIntervals += hist.size();
    }
    StringBuilder sb = new StringBuilder();
    sb.append("    "
        + Logger.formatDecimalNumber(newIntervals / 2)
        + " client statistics processed from extra-info descriptors\n");
    sb.append("    "
        + Logger.formatDecimalNumber(this.newResponses.size())
        + " client status files updated\n");
    return sb.toString();
  }
}
