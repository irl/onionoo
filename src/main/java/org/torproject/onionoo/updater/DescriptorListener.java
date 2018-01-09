/* Copyright 2016--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.onionoo.updater;

import org.torproject.descriptor.Descriptor;

public interface DescriptorListener {
  abstract void processDescriptor(Descriptor descriptor, boolean relay);
}

