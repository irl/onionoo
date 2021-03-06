/* Copyright 2014--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.onionoo.writer;

public interface DocumentWriter {

  public abstract void writeDocuments(long mostRecentStatusMillis);

  public abstract String getStatsString();
}

