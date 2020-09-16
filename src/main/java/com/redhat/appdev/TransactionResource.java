package com.redhat.appdev;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.quarkus.runtime.StartupEvent;
import java.util.Properties;
import java.io.*;
import javax.enterprise.event.Observes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.csv.*;
import com.google.gson.Gson;

@Path("/file")
public class TransactionResource {
    private static final Logger log = LoggerFactory.getLogger(TransactionResource.class);

    
    @ConfigProperty(name = "mp.messaging.outgoing.transactions.bootstrap.servers")
    public String bootstrapServers;

    @ConfigProperty(name = "mp.messaging.outgoing.transactions.topic")
    public String transactionsTopic;

    @ConfigProperty(name = "mp.messaging.outgoing.transactions.value.serializer")
    public String transactionsTopicValueSerializer;

    @ConfigProperty(name = "mp.messaging.outgoing.transactions.key.serializer")
    public String transactionsTopicKeySerializer;

    private Producer<String, String> producer;
    
    @POST
    @Path("/upload")    
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({ MediaType.TEXT_PLAIN })
    public String generate(MultipartFormDataInput input) {

        Integer counter = 0;

        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("uploadedFile");

        for (InputPart inputPart : inputParts) {

            try {

               //convert the uploaded file to inputstream
               InputStream inputStream = inputPart.getBody(InputStream.class,null);

               CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
               CsvMapper csvMapper = new CsvMapper();
        
               // Read data from CSV file
               MappingIterator<Map<?, ?>> mappingIterator = csvMapper.readerFor(Map.class).with(csvSchema).readValues(inputStream);
               List<Map<?, ?>> list = mappingIterator.readAll();
       
               ListIterator<Map<?, ?>> listIterator = list.listIterator();
       
               log.info("Sending data ...");
        
               while(listIterator.hasNext()) {
                   producer.send(new ProducerRecord<>(transactionsTopic, new Gson().toJson(listIterator.next(), LinkedHashMap.class)));
                   counter++;
               }
       
               log.info("All data sent ...");
   
             } catch (IOException e) {
               e.printStackTrace();
             }
   
           }

           return "Sucessfully parsed, converted the CSV to Json and Streamed to Kafka: Number of transactions sent : " + counter;

    }
    
    public void init(@Observes StartupEvent ev) {
        Properties props = new Properties();

        props.put("bootstrap.servers", bootstrapServers);
        props.put("value.serializer", transactionsTopicValueSerializer);
        props.put("key.serializer", transactionsTopicKeySerializer);
        producer = new KafkaProducer<String,  String>(props);
    }
    
}