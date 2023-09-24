package io.github.hustonic.script.vpn;

import lombok.Data;

/**
 * @author Huston
 */
@Data
public class VpnServerProperties {
  private String cluster;
  private String region;
  private String securityGroupId;
}
