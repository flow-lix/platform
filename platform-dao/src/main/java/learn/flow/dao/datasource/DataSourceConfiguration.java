package learn.flow.dao.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;


//@Profile("multi-datasource") 对应multi-datasource.properties
@Configuration
@PropertySource("classpath:/datasource/data_source.yml")
@MapperScan("learn.flow.dao.repository")
public class DataSourceConfiguration {

    @Primary
    @Bean(name = "dataSource")
    @ConfigurationProperties("spring.datasource.druid")
    public DruidDataSource dataSource(){
        return DruidDataSourceBuilder.create().build();
    }
}
