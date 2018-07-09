/* Copyright 2013--2018 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.onionoo.docs;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

@SuppressWarnings("checkstyle:membername")
public class DetailsDocument extends Document {

  /* We must ensure that details files only contain ASCII characters
   * and no UTF-8 characters.  While UTF-8 characters are perfectly
   * valid in JSON, this would break compatibility with existing files
   * pretty badly.  We do this by escaping non-ASCII characters, e.g.,
   * \u00F2.  Gson won't treat this as UTF-8, but will think that we want
   * to write six characters '\', 'u', '0', '0', 'F', '2'.  The only thing
   * we'll have to do is to change back the '\\' that Gson writes for the
   * '\'. */
  private static String escapeJson(String stringToEscape) {
    return StringEscapeUtils.escapeJava(stringToEscape);
  }

  private static String unescapeJson(String stringToUnescape) {
    return StringEscapeUtils.unescapeJava(stringToUnescape);
  }

  private String nickname;

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public String getNickname() {
    return this.nickname;
  }

  private String fingerprint;

  public void setFingerprint(String fingerprint) {
    this.fingerprint = fingerprint;
  }

  public String getFingerprint() {
    return this.fingerprint;
  }

  private String hashed_fingerprint;

  public void setHashedFingerprint(String hashedFingerprint) {
    this.hashed_fingerprint = hashedFingerprint;
  }

  public String getHashedFingerprint() {
    return this.hashed_fingerprint;
  }

  private List<String> or_addresses;

  public void setOrAddresses(List<String> orAddresses) {
    this.or_addresses = orAddresses;
  }

  public List<String> getOrAddresses() {
    return this.or_addresses;
  }

  private List<String> exit_addresses;

  public void setExitAddresses(List<String> exitAddresses) {
    this.exit_addresses = !exitAddresses.isEmpty() ? exitAddresses : null;
  }

  public List<String> getExitAddresses() {
    return this.exit_addresses == null ? new ArrayList<String>()
        : this.exit_addresses;
  }

  private String dir_address;

  public void setDirAddress(String dirAddress) {
    this.dir_address = dirAddress;
  }

  public String getDirAddress() {
    return this.dir_address;
  }

  private String last_seen;

  public void setLastSeen(long lastSeen) {
    this.last_seen = DateTimeHelper.format(lastSeen);
  }

  public long getLastSeen() {
    return DateTimeHelper.parse(this.last_seen);
  }

  private String last_changed_address_or_port;

  public void setLastChangedAddressOrPort(
      long lastChangedAddressOrPort) {
    this.last_changed_address_or_port = DateTimeHelper.format(
        lastChangedAddressOrPort);
  }

  public long getLastChangedAddressOrPort() {
    return DateTimeHelper.parse(this.last_changed_address_or_port);
  }

  private String first_seen;

  public void setFirstSeen(long firstSeen) {
    this.first_seen = DateTimeHelper.format(firstSeen);
  }

  public long getFirstSeen() {
    return DateTimeHelper.parse(this.first_seen);
  }

  private Boolean running;

  public void setRunning(Boolean running) {
    this.running = running;
  }

  public Boolean getRunning() {
    return this.running;
  }

  private SortedSet<String> flags;

  public void setFlags(SortedSet<String> flags) {
    this.flags = flags;
  }

  public SortedSet<String> getFlags() {
    return this.flags;
  }

  private String country;

  public void setCountry(String country) {
    this.country = country;
  }

  public String getCountry() {
    return this.country;
  }

  private String country_name;

  public void setCountryName(String countryName) {
    this.country_name = escapeJson(countryName);
  }

  public String getCountryName() {
    return unescapeJson(this.country_name);
  }

  private String region_name;

  public void setRegionName(String regionName) {
    this.region_name = escapeJson(regionName);
  }

  public String getRegionName() {
    return unescapeJson(this.region_name);
  }

  private String city_name;

  public void setCityName(String cityName) {
    this.city_name = escapeJson(cityName);
  }

  public String getCityName() {
    return unescapeJson(this.city_name);
  }

  private Float latitude;

  public void setLatitude(Float latitude) {
    this.latitude = latitude;
  }

  public Float getLatitude() {
    return this.latitude;
  }

  private Float longitude;

  public void setLongitude(Float longitude) {
    this.longitude = longitude;
  }

  public Float getLongitude() {
    return this.longitude;
  }

  private String as_number;

  public void setAsNumber(String asNumber) {
    this.as_number = escapeJson(asNumber);
  }

  public String getAsNumber() {
    return unescapeJson(this.as_number);
  }

  private String as_name;

  public void setAsName(String asName) {
    this.as_name = escapeJson(asName);
  }

  public String getAsName() {
    return unescapeJson(this.as_name);
  }

  private Long consensus_weight;

  public void setConsensusWeight(Long consensusWeight) {
    this.consensus_weight = consensusWeight;
  }

  public Long getConsensusWeight() {
    return this.consensus_weight;
  }

  private String host_name;

  public void setHostName(String hostName) {
    this.host_name = escapeJson(hostName);
  }

  public String getHostName() {
    return unescapeJson(this.host_name);
  }

  private List<String> verified_host_names;

  /**
   * Creates a copy of the list with each string escaped for JSON compatibility
   * and sets this as the verified host names, unless the argument was null in
   * which case the verified host names are just set to null.
   */
  public void setVerifiedHostNames(List<String> verifiedHostNames) {
    if (null == verifiedHostNames) {
      this.verified_host_names = null;
      return;
    }
    this.verified_host_names = new ArrayList<>();
    for (String hostName : verifiedHostNames) {
      this.verified_host_names.add(escapeJson(hostName));
    }
  }

  /**
   * Creates a copy of the list with each string having its escaping for JSON
   * compatibility reversed and returns the copy, unless the held reference was
   * null in which case null is returned.
   */
  public List<String> getVerifiedHostNames() {
    if (null == this.verified_host_names) {
      return null;
    }
    List<String> verifiedHostNames = new ArrayList<>();
    for (String escapedHostName : this.verified_host_names) {
      verifiedHostNames.add(unescapeJson(escapedHostName));
    }
    return verifiedHostNames;
  }

  private List<String> unverified_host_names;

  /**
   * Creates a copy of the list with each string escaped for JSON compatibility
   * and sets this as the unverified host names, unless the argument was null in
   * which case the unverified host names are just set to null.
   */
  public void setUnverifiedHostNames(List<String> unverifiedHostNames) {
    if (null == unverifiedHostNames) {
      this.unverified_host_names = null;
      return;
    }
    this.unverified_host_names = new ArrayList<>();
    for (String hostName : unverifiedHostNames) {
      this.unverified_host_names.add(escapeJson(hostName));
    }
  }

  /**
   * Creates a copy of the list with each string having its escaping for JSON
   * compatibility reversed and returns the copy, unless the held reference was
   * null in which case null is returned.
   */
  public List<String> getUnverifiedHostNames() {
    if (null == this.unverified_host_names) {
      return null;
    }
    List<String> unverifiedHostNames = new ArrayList<>();
    for (String escapedHostName : this.unverified_host_names) {
      unverifiedHostNames.add(unescapeJson(escapedHostName));
    }
    return unverifiedHostNames;
  }

  private String last_restarted;

  public void setLastRestarted(Long lastRestarted) {
    this.last_restarted = (lastRestarted == null ? null
        : DateTimeHelper.format(lastRestarted));
  }

  public Long getLastRestarted() {
    return this.last_restarted == null ? null :
        DateTimeHelper.parse(this.last_restarted);
  }

  private Integer bandwidth_rate;

  public void setBandwidthRate(Integer bandwidthRate) {
    this.bandwidth_rate = bandwidthRate;
  }

  public Integer getBandwidthRate() {
    return this.bandwidth_rate;
  }

  private Integer bandwidth_burst;

  public void setBandwidthBurst(Integer bandwidthBurst) {
    this.bandwidth_burst = bandwidthBurst;
  }

  public Integer getBandwidthBurst() {
    return this.bandwidth_burst;
  }

  private Integer observed_bandwidth;

  public void setObservedBandwidth(Integer observedBandwidth) {
    this.observed_bandwidth = observedBandwidth;
  }

  public Integer getObservedBandwidth() {
    return this.observed_bandwidth;
  }

  private Integer advertised_bandwidth;

  public void setAdvertisedBandwidth(Integer advertisedBandwidth) {
    this.advertised_bandwidth = advertisedBandwidth;
  }

  public Integer getAdvertisedBandwidth() {
    return this.advertised_bandwidth;
  }

  private List<String> exit_policy;

  public void setExitPolicy(List<String> exitPolicy) {
    this.exit_policy = exitPolicy;
  }

  public List<String> getExitPolicy() {
    return this.exit_policy;
  }

  private Map<String, List<String>> exit_policy_summary;

  public void setExitPolicySummary(
      Map<String, List<String>> exitPolicySummary) {
    this.exit_policy_summary = exitPolicySummary;
  }

  public Map<String, List<String>> getExitPolicySummary() {
    return this.exit_policy_summary;
  }

  private Map<String, List<String>> exit_policy_v6_summary;

  public void setExitPolicyV6Summary(
      Map<String, List<String>> exitPolicyV6Summary) {
    this.exit_policy_v6_summary = exitPolicyV6Summary;
  }

  public Map<String, List<String>> getExitPolicyV6Summary() {
    return this.exit_policy_v6_summary;
  }

  private String contact;

  public void setContact(String contact) {
    this.contact = escapeJson(contact);
  }

  public String getContact() {
    return unescapeJson(this.contact);
  }

  private String platform;

  public void setPlatform(String platform) {
    this.platform = escapeJson(platform);
  }

  public String getPlatform() {
    return unescapeJson(this.platform);
  }

  private String version;

  public void setVersion(String version) {
    this.version = version;
  }

  public String getVersion() {
    return this.version;
  }

  private String version_status;

  public void setVersionStatus(String versionStatus) {
    this.version_status = versionStatus;
  }

  public String getVersionStatus() {
    return this.version_status;
  }

  private SortedSet<String> alleged_family;

  public void setAllegedFamily(SortedSet<String> allegedFamily) {
    this.alleged_family = allegedFamily;
  }

  public SortedSet<String> getAllegedFamily() {
    return this.alleged_family;
  }

  private SortedSet<String> effective_family;

  public void setEffectiveFamily(SortedSet<String> effectiveFamily) {
    this.effective_family = effectiveFamily;
  }

  public SortedSet<String> getEffectiveFamily() {
    return this.effective_family;
  }

  private SortedSet<String> indirect_family;

  public void setIndirectFamily(SortedSet<String> indirectFamily) {
    this.indirect_family = indirectFamily;
  }

  public SortedSet<String> getIndirectFamily() {
    return this.indirect_family;
  }

  private Float consensus_weight_fraction;

  /** Sets the consensus weight fraction to the given value, but only if
   * that value is neither null nor negative. */
  public void setConsensusWeightFraction(Float consensusWeightFraction) {
    if (consensusWeightFraction == null
        || consensusWeightFraction >= 0.0) {
      this.consensus_weight_fraction = consensusWeightFraction;
    }
  }

  public Float getConsensusWeightFraction() {
    return this.consensus_weight_fraction;
  }

  private Float guard_probability;

  /** Sets the guard probability to the given value, but only if that
   * value is neither null nor negative. */
  public void setGuardProbability(Float guardProbability) {
    if (guardProbability == null || guardProbability >= 0.0) {
      this.guard_probability = guardProbability;
    }
  }

  public Float getGuardProbability() {
    return this.guard_probability;
  }

  private Float middle_probability;

  /** Sets the middle probability to the given value, but only if that
   * value is neither null nor negative. */
  public void setMiddleProbability(Float middleProbability) {
    if (middleProbability == null || middleProbability >= 0.0) {
      this.middle_probability = middleProbability;
    }
  }

  public Float getMiddleProbability() {
    return this.middle_probability;
  }

  private Float exit_probability;

  /** Sets the exit probability to the given value, but only if that
   * value is neither null nor negative. */
  public void setExitProbability(Float exitProbability) {
    if (exitProbability == null || exitProbability >= 0.0) {
      this.exit_probability = exitProbability;
    }
  }

  public Float getExitProbability() {
    return this.exit_probability;
  }

  private Boolean recommended_version;

  public void setRecommendedVersion(Boolean recommendedVersion) {
    this.recommended_version = recommendedVersion;
  }

  public Boolean getRecommendedVersion() {
    return this.recommended_version;
  }

  private Boolean hibernating;

  public void setHibernating(Boolean hibernating) {
    this.hibernating = hibernating;
  }

  public Boolean getHibernating() {
    return this.hibernating;
  }

  private List<String> transports;

  public void setTransports(List<String> transports) {
    this.transports = (transports != null && !transports.isEmpty())
        ? transports : null;
  }

  public List<String> getTransports() {
    return this.transports;
  }

  private Boolean measured;

  public void setMeasured(Boolean measured) {
    this.measured = measured;
  }

  public Boolean getMeasured() {
    return this.measured;
  }

  private List<String> unreachable_or_addresses;

  public void setUnreachableOrAddresses(List<String> unreachableOrAddresses) {
    this.unreachable_or_addresses = unreachableOrAddresses;
  }

  public List<String> getUnreachableOrAddresses() {
    return this.unreachable_or_addresses;
  }
}

