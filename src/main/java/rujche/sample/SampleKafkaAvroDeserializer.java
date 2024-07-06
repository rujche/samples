// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package rujche.sample;

import com.azure.core.models.MessageContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.TypeReference;
import com.azure.data.schemaregistry.SchemaRegistryClientBuilder;
import com.azure.data.schemaregistry.apacheavro.SchemaRegistryApacheAvroSerializer;
import com.azure.data.schemaregistry.apacheavro.SchemaRegistryApacheAvroSerializerBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import java.util.Map;

/**
 * Copied from com.microsoft.azure.schemaregistry.kafka.avro.KafkaAvroDeserializer.
 * Change credential to DefaultAzureCredential.
 * Use KafkaAvroDeserializer and KafkaAvroSerializer in azure-schemaregistry-kafka-avro after this PR merged:
 *  *  <a href="https://github.com/Azure/azure-schema-registry-for-kafka/pull/57">...</a>
 */
public class SampleKafkaAvroDeserializer<T extends IndexedRecord> implements Deserializer<T> {
    private SchemaRegistryApacheAvroSerializer serializer;

    public SampleKafkaAvroDeserializer() {
        super();
    }

    public void configure(Map<String, ?> props, boolean isKey) {
        this.serializer = new SchemaRegistryApacheAvroSerializerBuilder()
                .schemaRegistryClient(
                        new SchemaRegistryClientBuilder()
                                .fullyQualifiedNamespace(props.get("schema.registry.url").toString())
                                .credential(new DefaultAzureCredentialBuilder().build())
                                .buildAsyncClient())
                .avroSpecificReader(true)
                .buildSerializer();
    }

    @Override
    public T deserialize(String topic, byte[] bytes) {
        return null;
    }

    @Override
    public T deserialize(String topic, Headers headers, byte[] bytes) {
        MessageContent message = new MessageContent();
        message.setBodyAsBinaryData(BinaryData.fromBytes(bytes));

        Header contentTypeHeader = headers.lastHeader("content-type");
        if (contentTypeHeader != null) {
            message.setContentType(new String(contentTypeHeader.value()));
        } else {
            message.setContentType("");
        }

        return (T) this.serializer.deserialize(message, TypeReference.createInstance(Object.class));
    }

    @Override
    public void close() { }
}
