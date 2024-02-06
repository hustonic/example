package io.github.hustonic.script;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hustonic.script.vpn.VpnServerProperties;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2AsyncClient;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.ecs.EcsAsyncClient;
import software.amazon.awssdk.services.ecs.model.DesiredStatus;

/**
 * 启动VPN服务端，修改安全组白名单IP，打印VPN的IP地址和端口号。 TODO 优化为spring-shell程序
 *
 * @author Huston
 */
public class StartVpnServerApplication {

  public static final String CLUSTER = "Default";
  public static final String SERVICE = "shadowsocks";
  public static final String SECURITY_GROUP_ID = "sg-07ef1c3c94eb3e3ee";
  public static final int VPN_PORT = 8388;
  public static final int DESIRED_COUNT = 1;
  public static final Region REGION = Region.US_WEST_1;

  public static void main(String[] args) throws Exception {
    VpnServerProperties properties =
        new ObjectMapper().readValue(new File(args[0]), VpnServerProperties.class);
    System.out.println(properties);

    Region region = Region.of(properties.getRegion());

    try (Ec2AsyncClient ec2 =
            Ec2AsyncClient.builder()
                .region(region)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
        EcsAsyncClient ecs =
            EcsAsyncClient.builder()
                .region(region)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build()) {

      start(ecs, ec2, properties);
    }
  }

  private static void start(EcsAsyncClient ecs, Ec2AsyncClient ec2, VpnServerProperties properties)
      throws Exception {

    String cluster = properties.getCluster();
    String securityGroupId = properties.getSecurityGroupId();

    WebClient webClient = WebClient.create();

    CompletableFuture<DescribeNetworkInterfacesResponse> startServerFuture =
        startServer(ecs, ec2, cluster);

    CompletableFuture<AuthorizeSecurityGroupIngressResponse> updateSecurityGroupFuture =
        updateSecurityGroup(ec2, securityGroupId, webClient);

    CompletableFuture.allOf(startServerFuture, updateSecurityGroupFuture).get();
  }

  private static CompletableFuture<DescribeNetworkInterfacesResponse> startServer(
      EcsAsyncClient ecs, Ec2AsyncClient ec2, String cluster) {
    return ecs.updateService(
            builder -> builder.cluster(cluster).service(SERVICE).desiredCount(DESIRED_COUNT))
        .thenComposeAsync(
            updateServiceResponse ->
                ecs.waiter()
                    .waitUntilServicesStable(builder -> builder.cluster(cluster).services(SERVICE)))
        .thenComposeAsync(
            describeServicesResponseWaiterResponse ->
                ecs.listTasks(
                    builder ->
                        builder
                            .cluster(cluster)
                            .serviceName(SERVICE)
                            .desiredStatus(DesiredStatus.RUNNING)))
        .thenComposeAsync(
            taskArns ->
                ecs.describeTasks(builder -> builder.cluster(cluster).tasks(taskArns.taskArns())))
        .thenComposeAsync(
            tasks ->
                ec2.describeNetworkInterfaces(
                    builder ->
                        builder.filters(
                            Filter.builder()
                                .name("network-interface-id")
                                .values(
                                    tasks.tasks().get(0).attachments().get(0).details().stream()
                                        .filter(
                                            keyValuePair ->
                                                Objects.equals(
                                                    keyValuePair.name(), "networkInterfaceId"))
                                        .toList()
                                        .get(0)
                                        .value())
                                .build())))
        .whenCompleteAsync(
            (networkInterfaces, throwable) ->
                System.out.println(
                    "vpn ip = "
                        + networkInterfaces.networkInterfaces().get(0).association().publicIp()));
  }

  private static CompletableFuture<AuthorizeSecurityGroupIngressResponse> updateSecurityGroup(
      Ec2AsyncClient ec2, String securityGroupId, WebClient webClient) {
    return webClient
        .get()
        .uri("https://checkip.amazonaws.com")
        .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class))
        .toFuture()
        .thenComposeAsync(
            ip ->
                ec2.describeSecurityGroupRules(
                        builder ->
                            builder.filters(
                                Filter.builder().name("group-id").values(securityGroupId).build()))
                    .thenComposeAsync(
                        describeSecurityGroupRulesResponse ->
                            ec2.revokeSecurityGroupIngress(
                                builder ->
                                    builder
                                        .groupId(securityGroupId)
                                        .securityGroupRuleIds(
                                            describeSecurityGroupRulesResponse
                                                .securityGroupRules()
                                                .stream()
                                                .filter(
                                                    securityGroupRule ->
                                                        !securityGroupRule.isEgress())
                                                .map(SecurityGroupRule::securityGroupRuleId)
                                                .toList())))
                    .thenComposeAsync(
                        (revokeSecurityGroupIngressResponse) ->
                            ec2.authorizeSecurityGroupIngress(
                                builder ->
                                    builder
                                        .groupId(securityGroupId)
                                        .ipPermissions(
                                            IpPermission.builder()
                                                .ipProtocol("tcp")
                                                .fromPort(VPN_PORT)
                                                .toPort(VPN_PORT)
                                                .ipRanges(
                                                    IpRange.builder()
                                                        .cidrIp(ip.trim() + "/32")
                                                        .build())
                                                .build(),
                                            IpPermission.builder()
                                                .ipProtocol("udp")
                                                .fromPort(VPN_PORT)
                                                .toPort(VPN_PORT)
                                                .ipRanges(
                                                    IpRange.builder()
                                                        .cidrIp(ip.trim() + "/32")
                                                        .build())
                                                .build()))));
  }
}
