/* Copyright 2014 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.onionoo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

public class UptimeDocumentWriter implements FingerprintListener,
    DocumentWriter {

  private DescriptorSource descriptorSource;

  private DocumentStore documentStore;

  private long now;

  public UptimeDocumentWriter() {
    this.descriptorSource = ApplicationFactory.getDescriptorSource();
    this.documentStore = ApplicationFactory.getDocumentStore();
    this.now = ApplicationFactory.getTime().currentTimeMillis();
    this.registerFingerprintListeners();
  }

  private void registerFingerprintListeners() {
    this.descriptorSource.registerFingerprintListener(this,
        DescriptorType.RELAY_CONSENSUSES);
    this.descriptorSource.registerFingerprintListener(this,
        DescriptorType.BRIDGE_STATUSES);
  }

  private SortedSet<String> newRelayFingerprints = new TreeSet<String>(),
      newBridgeFingerprints = new TreeSet<String>();

  public void processFingerprints(SortedSet<String> fingerprints,
      boolean relay) {
    if (relay) {
      this.newRelayFingerprints.addAll(fingerprints);
    } else {
      this.newBridgeFingerprints.addAll(fingerprints);
    }
  }

  public void writeDocuments() {
    UptimeStatus uptimeStatus = this.documentStore.retrieve(
        UptimeStatus.class, true);
    if (uptimeStatus == null) {
      return;
    }
    for (String fingerprint : this.newRelayFingerprints) {
      this.updateDocument(true, fingerprint,
          uptimeStatus.getRelayHistory());
    }
    for (String fingerprint : this.newBridgeFingerprints) {
      this.updateDocument(false, fingerprint,
          uptimeStatus.getBridgeHistory());
    }
    Logger.printStatusTime("Wrote uptime document files");
  }

  private int writtenDocuments = 0;

  private void updateDocument(boolean relay, String fingerprint,
      SortedSet<UptimeHistory> knownStatuses) {
    UptimeStatus uptimeStatus = this.documentStore.retrieve(
        UptimeStatus.class, true, fingerprint);
    if (uptimeStatus != null) {
      SortedSet<UptimeHistory> history = relay
          ? uptimeStatus.getRelayHistory()
          : uptimeStatus.getBridgeHistory();
      UptimeDocument uptimeDocument = new UptimeDocument();
      uptimeDocument.setDocumentString(this.formatHistoryString(relay,
          fingerprint, history, knownStatuses));
      this.documentStore.store(uptimeDocument, fingerprint);
      this.writtenDocuments++;
    }
  }

  private String[] graphNames = new String[] {
      "1_week",
      "1_month",
      "3_months",
      "1_year",
      "5_years" };

  private long[] graphIntervals = new long[] {
      DateTimeHelper.ONE_WEEK,
      DateTimeHelper.ROUGHLY_ONE_MONTH,
      DateTimeHelper.ROUGHLY_THREE_MONTHS,
      DateTimeHelper.ROUGHLY_ONE_YEAR,
      DateTimeHelper.ROUGHLY_FIVE_YEARS };

  private long[] dataPointIntervals = new long[] {
      DateTimeHelper.ONE_HOUR,
      DateTimeHelper.FOUR_HOURS,
      DateTimeHelper.TWELVE_HOURS,
      DateTimeHelper.TWO_DAYS,
      DateTimeHelper.TEN_DAYS };

  private String formatHistoryString(boolean relay, String fingerprint,
      SortedSet<UptimeHistory> history,
      SortedSet<UptimeHistory> knownStatuses) {
    StringBuilder sb = new StringBuilder();
    sb.append("{\"fingerprint\":\"" + fingerprint + "\"");
    sb.append(",\n\"uptime\":{");
    int graphIntervalsWritten = 0;
    for (int graphIntervalIndex = 0; graphIntervalIndex <
        this.graphIntervals.length; graphIntervalIndex++) {
      String timeline = this.formatTimeline(graphIntervalIndex, relay,
          history, knownStatuses);
      if (timeline != null) {
        sb.append((graphIntervalsWritten++ > 0 ? "," : "") + "\n"
            + timeline);
      }
    }
    sb.append("}");
    sb.append("\n}\n");
    return sb.toString();
  }

  private String formatTimeline(int graphIntervalIndex, boolean relay,
      SortedSet<UptimeHistory> history,
      SortedSet<UptimeHistory> knownStatuses) {
    String graphName = this.graphNames[graphIntervalIndex];
    long graphInterval = this.graphIntervals[graphIntervalIndex];
    long dataPointInterval =
        this.dataPointIntervals[graphIntervalIndex];
    int dataPointIntervalHours = (int) (dataPointInterval
        / DateTimeHelper.ONE_HOUR);
    List<Integer> statusDataPoints = new ArrayList<Integer>();
    long intervalStartMillis = ((this.now - graphInterval)
        / dataPointInterval) * dataPointInterval;
    int statusHours = 0;
    for (UptimeHistory hist : knownStatuses) {
      if (hist.isRelay() != relay) {
        continue;
      }
      long histEndMillis = hist.getStartMillis() + DateTimeHelper.ONE_HOUR
          * hist.getUptimeHours();
      if (histEndMillis < intervalStartMillis) {
        continue;
      }
      while (hist.getStartMillis() >= intervalStartMillis
          + dataPointInterval) {
        statusDataPoints.add(statusHours * 5 > dataPointIntervalHours
            ? statusHours : -1);
        statusHours = 0;
        intervalStartMillis += dataPointInterval;
      }
      while (histEndMillis >= intervalStartMillis + dataPointInterval) {
        statusHours += (int) ((intervalStartMillis + dataPointInterval
            - Math.max(hist.getStartMillis(), intervalStartMillis))
            / DateTimeHelper.ONE_HOUR);
        statusDataPoints.add(statusHours * 5 > dataPointIntervalHours
            ? statusHours : -1);
        statusHours = 0;
        intervalStartMillis += dataPointInterval;
      }
      statusHours += (int) ((histEndMillis - Math.max(
          hist.getStartMillis(), intervalStartMillis))
          / DateTimeHelper.ONE_HOUR);
    }
    statusDataPoints.add(statusHours * 5 > dataPointIntervalHours
        ? statusHours : -1);
    List<Integer> uptimeDataPoints = new ArrayList<Integer>();
    intervalStartMillis = ((this.now - graphInterval)
        / dataPointInterval) * dataPointInterval;
    int uptimeHours = 0;
    long firstStatusStartMillis = -1L;
    for (UptimeHistory hist : history) {
      if (hist.isRelay() != relay) {
        continue;
      }
      if (firstStatusStartMillis < 0L) {
        firstStatusStartMillis = hist.getStartMillis();
      }
      long histEndMillis = hist.getStartMillis() + DateTimeHelper.ONE_HOUR
          * hist.getUptimeHours();
      if (histEndMillis < intervalStartMillis) {
        continue;
      }
      while (hist.getStartMillis() >= intervalStartMillis
          + dataPointInterval) {
        if (firstStatusStartMillis < intervalStartMillis
            + dataPointInterval) {
          uptimeDataPoints.add(uptimeHours);
        } else {
          uptimeDataPoints.add(-1);
        }
        uptimeHours = 0;
        intervalStartMillis += dataPointInterval;
      }
      while (histEndMillis >= intervalStartMillis + dataPointInterval) {
        uptimeHours += (int) ((intervalStartMillis + dataPointInterval
            - Math.max(hist.getStartMillis(), intervalStartMillis))
            / DateTimeHelper.ONE_HOUR);
        uptimeDataPoints.add(uptimeHours);
        uptimeHours = 0;
        intervalStartMillis += dataPointInterval;
      }
      uptimeHours += (int) ((histEndMillis - Math.max(
          hist.getStartMillis(), intervalStartMillis))
          / DateTimeHelper.ONE_HOUR);
    }
    uptimeDataPoints.add(uptimeHours);
    List<Double> dataPoints = new ArrayList<Double>();
    for (int dataPointIndex = 0; dataPointIndex < statusDataPoints.size();
        dataPointIndex++) {
      if (dataPointIndex >= uptimeDataPoints.size()) {
        dataPoints.add(0.0);
      } else if (uptimeDataPoints.get(dataPointIndex) >= 0 &&
          statusDataPoints.get(dataPointIndex) > 0) {
        dataPoints.add(((double) uptimeDataPoints.get(dataPointIndex))
            / ((double) statusDataPoints.get(dataPointIndex)));
      } else {
        dataPoints.add(-1.0);
      }
    }
    int firstNonNullIndex = -1, lastNonNullIndex = -1;
    for (int dataPointIndex = 0; dataPointIndex < dataPoints.size();
        dataPointIndex++) {
      double dataPoint = dataPoints.get(dataPointIndex);
      if (dataPoint >= 0.0) {
        if (firstNonNullIndex < 0) {
          firstNonNullIndex = dataPointIndex;
        }
        lastNonNullIndex = dataPointIndex;
      }
    }
    if (firstNonNullIndex < 0) {
      return null;
    }
    long firstDataPointMillis = (((this.now - graphInterval)
        / dataPointInterval) + firstNonNullIndex)
        * dataPointInterval + dataPointInterval / 2L;
    if (graphIntervalIndex > 0 && firstDataPointMillis >=
        this.now - graphIntervals[graphIntervalIndex - 1]) {
      /* Skip uptime history object, because it doesn't contain
       * anything new that wasn't already contained in the last
       * uptime history object(s). */
      return null;
    }
    long lastDataPointMillis = firstDataPointMillis
        + (lastNonNullIndex - firstNonNullIndex) * dataPointInterval;
    double factor = 1.0 / 999.0;
    int count = lastNonNullIndex - firstNonNullIndex + 1;
    StringBuilder sb = new StringBuilder();
    sb.append("\"" + graphName + "\":{"
        + "\"first\":\"" + DateTimeHelper.format(firstDataPointMillis)
        + "\",\"last\":\"" + DateTimeHelper.format(lastDataPointMillis)
        + "\",\"interval\":" + String.valueOf(dataPointInterval
        / DateTimeHelper.ONE_SECOND)
        + ",\"factor\":" + String.format(Locale.US, "%.9f", factor)
        + ",\"count\":" + String.valueOf(count) + ",\"values\":[");
    int dataPointsWritten = 0, previousNonNullIndex = -2;
    boolean foundTwoAdjacentDataPoints = false;
    for (int dataPointIndex = firstNonNullIndex; dataPointIndex <=
        lastNonNullIndex; dataPointIndex++) {
      double dataPoint = dataPoints.get(dataPointIndex);
      if (dataPoint >= 0.0) {
        if (dataPointIndex - previousNonNullIndex == 1) {
          foundTwoAdjacentDataPoints = true;
        }
        previousNonNullIndex = dataPointIndex;
      }
      sb.append((dataPointsWritten++ > 0 ? "," : "")
          + (dataPoint < -0.5 ? "null" :
          String.valueOf((long) (dataPoint * 999.0))));
    }
    sb.append("]}");
    if (foundTwoAdjacentDataPoints) {
      return sb.toString();
    } else {
      return null;
    }
  }

  public String getStatsString() {
    StringBuilder sb = new StringBuilder();
    sb.append("    " + Logger.formatDecimalNumber(this.writtenDocuments)
        + " uptime document files written\n");
    return sb.toString();
  }
}

