package com.dayu.lotto.algorithm.spark.streaming;
import org.apache.spark.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.*;
import org.apache.spark.streaming.api.java.*;
import scala.Tuple2;


public class SparkStreaming {

	public void init() {
		
		// Create a local StreamingContext with two working thread and batch interval of 1 second
		SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("NetworkWordCount");
		JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(1));
		
		// Create a DStream that will connect to hostname:port, like localhost:9999
		JavaReceiverInputDStream<String> lines = jssc.socketTextStream("localhost", 9999);
		
	}
	
	

}
