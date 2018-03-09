package com.xrtb.jmq;

import java.util.Properties;

/**
 * A common configuration handler for both kafka consumers and subscribers.
 * Created by ben on 12/23/17.
 */
public class KafkaConfig {

    /**
     * Holds the peoperties for use with kafka
     */
    Properties props = new Properties();
    /**
     * The default partition
     */
    int partition = 0;
    /**
     * The topic
     */
    String topic;
    /**
     * The bootstrap address
     */
    String address;
    /**
     * The groupid
     */
    String groupid;

    /**
     * Constructor for the configurator.
     *
     * @param saddress String. The configuration parameter, example: kafka://[localhost:9092]&topic=bids
     * @throws Exception on parsing errors.
     */
    public KafkaConfig(String saddress) throws Exception {
        int i = saddress.indexOf("]");
        if (i == -1)
            throw new Exception("Kafka broker definition is mangled");


        address = saddress.substring(0, i);
        address = address.replace("kafka://[", "");
        String[] parts = saddress.split("&");
        i = 0;

        //   props.put("group.id", "my-group");
        props.setProperty("auto.offset.reset", "latest");

        props.put("enable.auto.commit", true);
        props.put("request.timeout.ms", 50000);

        for (String part : parts) {
            if (i != 0) {
                String[] t = part.split("=");
                if (t.length != 2)
                    throw new Exception("Bad kafka option at " + part);
                switch (t[0]) {
                    case "topic":
                        topic = t[1];
                        break;
                    case "offset":
                        props.setProperty("auto.offset.reset", t[1]);
                        break;
                    case "autocommit":
                        props.put("auto.offset.reset", Boolean.parseBoolean(t[1]));
                        break;
                    case "acks":
                        props.put("acks", t[1]);
                        break;
                    case "retries":
                        props.put("retries", Integer.parseInt(t[1]));
                        break;
                    case "size":
                        props.put("batch.size", Integer.parseInt(t[1]));
                        break;
                    case "linger":
                        props.put("linger.ms", Integer.parseInt(t[1]));
                        break;
                    case "buffer":
                        props.put("buffer.memory", Integer.parseInt(t[1]));
                        break;
                    case "partition":
                        partition = Integer.parseInt(t[1]);
                        break;
                    case "groupid":
                        props.put("group.id", t[1]);
                        groupid = t[1];
                        break;
                    case "timeout":
                        props.put("request.timeout.ms", Integer.parseInt(t[1]));
                        break;
                    default:
                        throw new Exception("Unknown kafka option: " + part);
                }
            }
            i++;
        }

        props.put("bootstrap.servers", address);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    }

    public void setGroupId(String groupid) {
        this.groupid = "a" + groupid;
        props.put("group.id", this.groupid);
    }

    public String getGroupId() {
        return this.groupid;
    }

    /**
     * Return the topic
     *
     * @return String. The topic name subscribed to.
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Return the bootstrap address
     *
     * @return String. The bootstrap addresses.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Return the configuration  properties.
     *
     * @return Properties. The properties used by the kafka subscriber or publisher.
     */
    public Properties getProperties() {
        return props;
    }

    /**
     * Returns the partition.
     *
     * @return int. The partition used by this configuration.
     */
    public int getPartition() {
        return partition;
    }
}
