/**
 * 
 */
package com.stanzaliving.user.kafka.producer;

import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stanzaliving.core.kafka.producer.impl.BasePartitionProducer;
import com.stanzaliving.core.kafka.utils.KafkaUtil;

import lombok.extern.log4j.Log4j;

/**
 * @author naveen
 *
 * @date 12-Oct-2019
 */
@Log4j
@Service
public class KafkaUserProducer extends BasePartitionProducer<String, String> {

	@Autowired
	private Environment environment;

	@Autowired
	private ObjectMapper objectMapper;

	@PostConstruct
	public void configureProducer() {
		this.configure(KafkaUtil.getProducerProperties(environment));
	}

	@Override
	public void publish(String topic, Object payload) {
		log.debug("Publishing user data record on topic: " + topic);

		try {
			ProducerRecord<String, String> data = new ProducerRecord<>(topic, objectMapper.writeValueAsString(payload));
			Future<RecordMetadata> kafkaResponse = producer.send(data);

			if (log.isTraceEnabled()) {
				log.trace(Thread.currentThread().getName() + ": User Data Publish on topic " + topic + " Response On Kafka: " + objectMapper.writeValueAsString(kafkaResponse.get()));
			}

		} catch (Exception e) {
			log.error("Error while publishing User Data on Kafka: ", e);
		}
		producer.flush();

	}

	@Override
	public void publish(String topic, Object payload, int partition) {
		log.debug(Thread.currentThread().getName() + ": Publishing user data record on topic: " + topic + " and partition: " + partition);

		try {

			ProducerRecord<String, String> data = new ProducerRecord<>(topic, partition, null, null, objectMapper.writeValueAsString(payload));
			Future<RecordMetadata> kafkaResponse = producer.send(data);

			if (log.isTraceEnabled()) {
				log.trace(
						Thread.currentThread().getName() + ": User Data Publish on topic " + topic + " and partition: " + partition + " Response On Kafka: "
								+ objectMapper.writeValueAsString(kafkaResponse.get()));
			}
		} catch (Exception e) {
			log.error("Error while publishing User Data on Kafka on topic: " + topic + " and partition: " + partition, e);
		}

		producer.flush();

	}
}