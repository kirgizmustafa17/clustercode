package net.chrigel.clustercode.cluster.impl;

import com.google.inject.Singleton;
import net.chrigel.clustercode.cluster.ClusterService;
import net.chrigel.clustercode.cluster.ClusterSettings;
import net.chrigel.clustercode.util.AbstractPropertiesModule;

public class ClusterModule extends AbstractPropertiesModule {

    public static final String CLUSTER_NAME_KEY ="CC_CLUSTER_NAME";
    public static final String CLUSTER_JGROUPS_CONFIG_KEY = "CC_CLUSTER_JGROUPS_CONFIG";
    public static final String CLUSTER_PREFER_IPV4_KEY = "CC_CLUSTER_JGROUPS_PREFER_IPV4";
    public static final String CLUSTER_JGROUPS_BIND_PORT_KEY = "CC_CLUSTER_JGROUPS_BIND_PORT";
    public static final String CLUSTER_JGROUPS_BIND_ADDR_KEY = "CC_CLUSTER_JGROUPS_BIND_ADDR";
    public static final String CLUSTER_JGROUPS_HOSTNAME_KEY = "CC_CLUSTER_JGROUPS_HOSTNAME";
    public static final String CLUSTER_TASK_TIMEOUT_KEY = "CC_CLUSTER_TASK_TIMEOUT";

    @Override
    protected void configure() {
        bind(ClusterService.class).to(JgroupsClusterImpl.class).in(Singleton.class);
        bind(ClusterSettings.class).to(JgroupClusterSettingsImpl.class).in(Singleton.class);
        bind(JgroupsClusterSettings.class).to(JgroupClusterSettingsImpl.class).in(Singleton.class);
    }
}