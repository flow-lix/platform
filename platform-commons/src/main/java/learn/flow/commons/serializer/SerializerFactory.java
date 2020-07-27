package learn.flow.commons.serializer;

import learn.flow.commons.repository.Repository;
import learn.flow.commons.repository.RepositoryFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

public class SerializerFactory {

   public Serializer getSerializer(SerializerType type) {
       switch (type) {
           case JAVA:
               return new JdkSerializationRedisSerializer();
       }
   }
}
