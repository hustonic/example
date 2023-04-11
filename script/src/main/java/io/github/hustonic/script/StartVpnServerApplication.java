package io.github.hustonic.script;

import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2AsyncClient;
import software.amazon.awssdk.services.ec2.model.DescribeNetworkInterfacesResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ecs.EcsAsyncClient;
import software.amazon.awssdk.services.ecs.model.DesiredStatus;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 启动VPN服务端，并打印VPN的IP地址
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

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Ec2AsyncClient ec2 = Ec2AsyncClient.builder()
                .region(REGION)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();

        EcsAsyncClient ecs = EcsAsyncClient.builder()
                .region(REGION)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();

        WebClient webClient = WebClient.create();

        CompletableFuture<DescribeNetworkInterfacesResponse> networkInterfacesFuture = ecs.updateService(builder ->
                        builder.cluster(CLUSTER)
                                .service(SERVICE)
                                .desiredCount(DESIRED_COUNT))
                .thenComposeAsync(updateServiceResponse ->
                        ecs.waiter().waitUntilServicesStable(builder ->
                                builder.cluster(CLUSTER)
                                        .services(SERVICE)))
                .thenComposeAsync(describeServicesResponseWaiterResponse ->
                        ecs.listTasks(builder ->
                                builder.cluster(CLUSTER)
                                        .serviceName(SERVICE)
                                        .desiredStatus(DesiredStatus.RUNNING)))
                .thenComposeAsync(taskArns ->
                        ecs.describeTasks(builder ->
                                builder.cluster(CLUSTER)
                                        .tasks(taskArns.taskArns())))
                .thenComposeAsync(tasks ->
                        ec2.describeNetworkInterfaces(builder ->
                                builder.filters(Filter.builder()
                                        .name("network-interface-id")
                                        .values(tasks.tasks().get(0)
                                                .attachments().get(0)
                                                .details()
                                                .stream().filter(keyValuePair -> Objects.equals(keyValuePair.name(), "networkInterfaceId"))
                                                .toList().get(0)
                                                .value())
                                        .build())))
                .whenCompleteAsync((networkInterfaces, throwable) ->
                        System.out.println("vpn ip = " +
                                networkInterfaces.networkInterfaces().get(0)
                                        .association()
                                        .publicIp()));

        CompletableFuture<Void> updateSecurityGroupFuture = webClient.get()
                .uri("https://checkip.amazonaws.com")
                .exchangeToMono(clientResponse ->
                        clientResponse.bodyToMono(String.class))
                .toFuture()
                .thenAcceptAsync(ip -> CompletableFuture.allOf(
                        ec2.authorizeSecurityGroupIngress(builder ->
                                builder.groupId(SECURITY_GROUP_ID)
                                        .ipProtocol("tcp")
                                        .fromPort(VPN_PORT)
                                        .toPort(VPN_PORT)
                                        .cidrIp(ip.trim() + "/32")),
                        ec2.authorizeSecurityGroupIngress(builder ->
                                builder.groupId(SECURITY_GROUP_ID)
                                        .ipProtocol("udp")
                                        .fromPort(VPN_PORT)
                                        .toPort(VPN_PORT)
                                        .cidrIp(ip.trim() + "/32")))
                );

        CompletableFuture.allOf(networkInterfacesFuture, updateSecurityGroupFuture).get();
    }
}
