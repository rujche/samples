package rujche.sample;

import com.azure.core.models.MessageContent;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryClientBuilder;
import com.azure.data.schemaregistry.apacheavro.SchemaRegistryApacheAvroSerializer;
import com.azure.data.schemaregistry.apacheavro.SchemaRegistryApacheAvroSerializerBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

/**
 * Just copied from com.microsoft.azure.schemaregistry.kafka.avro.KafkaAvroSerializer
 * Changed the logic of DefaultAzureCredential.
 * Use KafkaAvroDeserializer and KafkaAvroSerializer in azure-schemaregistry-kafka-avro after this PR merged:
 *  <a href="https://github.com/Azure/azure-schema-registry-for-kafka/pull/57">...</a>
 */
public class SampleKafkaAvroSerializer<T> implements Serializer<T> {
    private SchemaRegistryApacheAvroSerializer serializer;

    /**
     * Empty constructor for Kafka producer
     */
    public SampleKafkaAvroSerializer() {
        super();
    }

    /**
     * Configures serializer instance.
     *
     * @param props Map of properties used to configure instance.
     * @param isKey Indicates if serializing record key or value.  Required by Kafka serializer interface,
     *              no specific functionality implemented for key use.
     *
     */
    @Override
    public void configure(Map<String, ?> props, boolean isKey) {
        this.serializer = new SchemaRegistryApacheAvroSerializerBuilder()
                .schemaRegistryClient(new SchemaRegistryClientBuilder()
                        .fullyQualifiedNamespace(props.get("schema.registry.url").toString())
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .buildAsyncClient())
                .schemaGroup(props.get("schema.group").toString())
                .autoRegisterSchemas(true)
                .buildSerializer();
    }


    /**
     * Serializes GenericRecord or SpecificRecord into a byte array, containing a GUID reference to schema
     * and the encoded payload.
     *
     * Null behavior matches Kafka treatment of null values.
     *
     * @param topic Topic destination for record. Required by Kafka serializer interface, currently not used.
     * @param record Object to be serialized, may be null
     * @return byte[] payload for sending to EH Kafka service, may be null
     * @throws SerializationException Exception catchable by core Kafka producer code
     */
    @Override
    public byte[] serialize(String topic, T record) {
        return null;
    }

    /**
     * Serializes GenericRecord or SpecificRecord into a byte array, containing a GUID reference to schema
     * and the encoded payload.
     *
     * Null behavior matches Kafka treatment of null values.
     *
     * @param topic Topic destination for record. Required by Kafka serializer interface, currently not used.
     * @param record Object to be serialized, may be null
     * @param headers Record headers, may be null
     * @return byte[] payload for sending to EH Kafka service, may be null
     * @throws SerializationException Exception catchable by core Kafka producer code
     */
    @Override
    public byte[] serialize(String topic, Headers headers, T record) {
        // null needs to treated specially since the client most likely just wants to send
        // an individual null value instead of making the subject a null type. Also, null in
        // Kafka has a special meaning for deletion in a topic with the compact retention policy.
        // Therefore, we will bypass schema registration and return a null value in Kafka, instead
        // of an Avro encoded null.
        if (record == null) {
            return null;
        }

        MessageContent message = this.serializer.serialize(record, TypeReference.createInstance(MessageContent.class));
        byte[] contentTypeHeaderBytes = message.getContentType().getBytes();
        headers.add("content-type", contentTypeHeaderBytes);
        return message.getBodyAsBinaryData().toBytes();
    }

    @Override
    public void close() { }
}