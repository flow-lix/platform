package learn.flow.commons.connector;

import com.google.common.base.Joiner;
import learn.flow.config.ZookeeperConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;
import org.apache.kafka.common.TopicPartition;
import org.apache.zookeeper.CreateMode;

import java.util.*;

/**
 * consumer: group/topic/partition
 */
@Slf4j
public class ZookeeperConnector {
    private ZookeeperConfig zookeeperConfig;

    private CuratorFramework client;

    private Map<String, InterProcessMutex> distributedLock;

    private String committedOffsetGroupPath;

    public ZookeeperConnector(ZookeeperConfig zConfig) {
        this.zookeeperConfig = zConfig;
        this.client = CuratorFrameworkFactory.newClient(zConfig.connectString(),
                new BoundedExponentialBackoffRetry(1000, 5000, 3));
        this.distributedLock = new HashMap<>();
    }

    private void lock(String lockPath) throws Exception {
        Objects.requireNonNull(lockPath, "lockPath must be not null");
        InterProcessMutex mutex = new InterProcessMutex(this.client, lockPath);
        distributedLock.put(lockPath, mutex);
        mutex.acquire();
    }

    public void unlock(String lockPath) throws Exception {
        InterProcessMutex mutex = distributedLock.get(lockPath);
        Objects.requireNonNull(mutex, "lockPath must be not null");
        mutex.release();
        distributedLock.remove(lockPath);
    }

    public String getCommittedOffsetGroupPath() {
        if (StringUtils.isEmpty(committedOffsetGroupPath)) {
            String stripped = StringUtils.strip(zookeeperConfig.kafkaPath(), "/");
            committedOffsetGroupPath = Joiner.on("/").skipNulls().join(
                    "",
                    stripped.equals("") ? null : stripped,
                    "consumers",
                    zookeeperConfig.kafkaGroup(),
                    "offsets");
            if (log.isInfoEnabled()) {
                log.info("New committed offset group path: {}", committedOffsetGroupPath);
            }
        }
        return committedOffsetGroupPath;
    }

    private String getCommittedOffsetTopicPath(String topic) {
        return getCommittedOffsetGroupPath() + "/" + topic;
    }

    private String getCommittedOffsetPartitionPath(TopicPartition topicPartition) {
        return getCommittedOffsetTopicPath(topicPartition.topic() + "/" +
                topicPartition.partition());
    }

    public long getCommittedOffsetCount(TopicPartition topicPartition) {
        String offsetPath = getCommittedOffsetPartitionPath(topicPartition);
        try {
            byte[] data = client.getData()
                    .inBackground()
                    .forPath(offsetPath);
            return Long.parseLong(new String(data));
        } catch (Exception e) {
            log.warn("path {} does not exist in zookeeper", offsetPath);
            return -1;
        }
    }

    /**
     * 得到Topic下的所有分区
     */
    public List<Integer> getCommittedOffsetPartitions(String topic) throws Exception {
        String partitionPath = getCommittedOffsetTopicPath(topic);
        Iterator<String> partitions = client.getChildren()
                .inBackground()
                .forPath(partitionPath)
                .iterator();

        List<Integer> partitionList = new LinkedList<>();
        partitions.forEachRemaining(p ->
                partitionList.add( Integer.valueOf(p.substring(p.lastIndexOf("/"))) )
        );
        return partitionList;
    }

    /**
     * 得到所有Topic
     */
    public List<String> getCommittedOffsetTopics() throws Exception {
        String topicPath = getCommittedOffsetGroupPath();
        Iterator<String> partitions = client.getChildren()
                .inBackground()
                .forPath(topicPath)
                .iterator();

        List<String> topicList = new LinkedList<>();
        partitions.forEachRemaining(p ->
                topicList.add( p.substring(p.lastIndexOf("/")) )
        );
        return topicList;
    }

    private void create(String path) {
        if (!Objects.equals(path.charAt(0), '/')) {
            throw new AssertionError("path: " + path + ".chatAt(0) != '/'");
        }
        String[] elements = path.split("/");
        StringBuilder prefix = new StringBuilder("/");
        for (String e : elements) {
            prefix.append(e);
            try {
                client.create()
                        .withMode(CreateMode.PERSISTENT)
//                    .withACL()
                        .forPath(prefix.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
//
//    public void setCommittedOffsetCount(TopicPartition topicPartition, long count)
//            throws Exception {
//        ZooKeeper zookeeper = mZookeeperClient.get();
//        String offsetPath = getCommittedOffsetPartitionPath(topicPartition);
//        LOG.info("creating missing parents for zookeeper path {}", offsetPath);
//        createMissingParents(offsetPath);
//        byte[] data = Long.toString(count).getBytes();
//        try {
//            LOG.info("setting zookeeper path {} value {}", offsetPath, count);
//            // -1 matches any version
//            zookeeper.setData(offsetPath, data, -1);
//        } catch (KeeperException.NoNodeException exception) {
//            zookeeper.create(offsetPath, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//        }
//    }
//
//    public void deleteCommittedOffsetTopicCount(String topic) throws Exception {
//        ZooKeeper zookeeper = mZookeeperClient.get();
//        List<Integer> partitions = getCommittedOffsetPartitions(topic);
//        for (Integer partition : partitions) {
//            TopicPartition topicPartition = new TopicPartition(topic, partition);
//            String offsetPath = getCommittedOffsetPartitionPath(topicPartition);
//            LOG.info("deleting path {}", offsetPath);
//            zookeeper.delete(offsetPath, -1);
//        }
//    }
//
//    public void deleteCommittedOffsetPartitionCount(TopicPartition topicPartition)
//            throws Exception {
//        String offsetPath = getCommittedOffsetPartitionPath(topicPartition);
//        ZooKeeper zookeeper = mZookeeperClient.get();
//        LOG.info("deleting path {}", offsetPath);
//        zookeeper.delete(offsetPath, -1);
//    }
//
//    protected void setConfig(SecorConfig config) {
//        this.mConfig = config;
//    }
}
