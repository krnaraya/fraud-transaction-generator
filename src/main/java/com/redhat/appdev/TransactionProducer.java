package com.redhat.appdev;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;

import io.cloudevents.json.Json;
import io.cloudevents.v1.CloudEventBuilder;
import io.cloudevents.v1.CloudEventImpl;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Path("/file")
public class TransactionProducer {

    Random rand = new Random();

    @Inject
    @Channel("out-transactions")
    Emitter<String> newTransactionEmitter;


    @POST
    @Path("/upload")    
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({ MediaType.TEXT_PLAIN })
    public String submitTransaction(MultipartFormDataInput input) {


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
               while(listIterator.hasNext()) {
                    ObjectMapper mapper = new ObjectMapper();

                    CloudEventImpl<JsonNode> transactionEvent =
                    CloudEventBuilder.<JsonNode>builder()
                            .withId(UUID.randomUUID().toString())
                            .withType("newTransactionEvent")
                            .withSource(URI.create("http://localhost:8080"))
                            .withData(mapper.convertValue(listIterator.next(), JsonNode.class))
                            .build();

                        System.out.println("transactions being produced : " + Json.encode(transactionEvent));

                        newTransactionEmitter.send(Json.encode(transactionEvent));
                        counter++;
                }
       
             
   
             } catch (Exception e) {
               e.printStackTrace();
             }
   
           }

           return "Sucessfully parsed, converted the CSV to Json and Streamed to Kafka: Number of transactions sent : " + counter;




    }


}
