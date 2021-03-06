package com.lei.wc;

/**
 * @Author: Lei
 * @E-mail: 843291011@qq.com
 * @Date: Created in 8:58 下午 2020/5/14
 * @Version: 1.0
 * @Modified By:
 * @Description:
 */

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * Flink 入门程序 WordCount（实时）
 *
 * 对word状态进行实时统计，包含状态监控
 *
 */
public class J02_StreamWordCount {
    public static void main(String[] args) throws Exception {
        /*
            注意：需要在服务器对环境变量新增HADOOP_CONF_DIR路径，具体如下：
                1. vi /etc/profile
                2. 添加：export HADOOP_CONF_DIR=/etc/hadoop/conf
         */

        // 启动Flink集群：/usr/local/flink_learn/flink-1.7.2/bin/start-cluster.sh
        // 使用WebUI查看Flink集群启动情况：http://node-01:8081/#/overview

        // --host localhost --port 7777
        // standalone提交方式：（含后台运行）
        // ./bin/flink run -c com.lei.wc.J02_StreamWordCount -p 2 /usr/local/spark-study/FlinkTutorial-1.0.jar --host localhost --port 7777
        // ./bin/flink run -c com.lei.wc.J02_StreamWordCount -p 2 /usr/local/spark-study/FlinkTutorial-1.0-jar-with-dependencies.jar --host localhost --port 7777

        // 列出正在运行的flink作业:
        // ./bin/flink list
        // ./bin/flink cancel xxxx_id

        ParameterTool params = ParameterTool.fromArgs(args);
        String host = params.get("host");
        int port = params.getInt("port");

        // 创建一个流处理的执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.disableOperatorChaining(); // 禁用任务链划分

        // 接收socket数据流
        DataStream<String> textDataStream = env.socketTextStream(host, port);
        SingleOutputStreamOperator<Tuple2> wordCountStream = textDataStream.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public void flatMap(String line, Collector<String> collector) throws Exception {
                String[] split = line.split("\\s");
                for (String s : split) {
                    collector.collect(s);
                }
            }
        })
                .filter(word -> !word.equals(""))
                //.filter(word -> word.equals("")).disableChaining() // 禁用任务链划分
                //.filter(_.nonEmpty).startNewChain()     // 开始新的任务链
                .map(word -> new Tuple2(word, 1))
                .returns(Types.TUPLE(Types.STRING, Types.INT))
                .keyBy(0)
                .sum(1);

        // 打印输出，流处理到这里才只是定义了流处理流程
        wordCountStream.print().setParallelism(1); // 设置并行度，如果没有指定默认是电脑CPU核心数
        env.execute("stream word count job");

        // 启动一个socket输入
        // nc -lk 7777

    }
}
