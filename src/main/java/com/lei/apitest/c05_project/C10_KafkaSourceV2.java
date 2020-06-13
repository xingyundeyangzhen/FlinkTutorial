package com.lei.apitest.c05_project;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Properties;

/**
 * @Author: Lei
 * @E-mail: 843291011@qq.com
 * @Date: Created in 10:01 上午 2020/6/13
 * @Version: 1.0
 * @Modified By:
 * @Description:
 */

// 该节内容存在缺失
public class C10_KafkaSourceV2 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 开启CheckPointing，同时开启重启策略
        env.enableCheckpointing(5000);
        // 设置StateBackend
        env.setStateBackend(new FsStateBackend("ile:\\\\lei_test_project\\idea_workspace\\FlinkTutorial\\check_point_dir"));
        // 取消任务checkPoint不删除
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        // 设置checkPoint的模式
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);

        Properties props = new Properties();

        // activity10 group_id_flink node-01:9092,node-02:9092,node-03:9092
        // 指定Kafka的Broker地址
        props.setProperty("bootstrap.servers", "node-01:9092,node-02:9092,node-03:9092");
        // 提定组ID
        props.setProperty("group.id", "group_id_flink");
        // 如果没有记录偏移量，第一次从开始消费
        props.setProperty("auto.offset.reset", "earliest");
        //props.setProperty("key,deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        //props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        // kafka的消费者不自动提交偏移量，默认kafka自动提交offset,且保存在__consumer_offsets
        props.setProperty("enable.auto.commit", "false");

        // KafkaSource
        FlinkKafkaConsumer<String> kafkaSource = new FlinkKafkaConsumer<>(
                "topic",
                new SimpleStringSchema(),
                props);

        // Flink CheckPoint成功后还要向Kafka特殊的topic中写入偏移量
        kafkaSource.setCommitOffsetsOnCheckpoints(false);

        env.execute("C10_KafkaSourceV2");
    }
}
