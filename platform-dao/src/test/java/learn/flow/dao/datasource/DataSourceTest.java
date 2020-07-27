package learn.flow.dao.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DataSourceTest {
    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    private DruidDataSource dataSource;

    @Test
    public void testDataSource() throws Exception {
        // 获取配置的数据源
//        DruidDataSource dataSource = (DruidDataSource) applicationContext.getBean("dataSource");
        // 查看配置数据源信息
        System.out.println(dataSource.getClass().getName());
        System.out.println(dataSource);
        System.out.println(dataSource.getFilterClassNames());
//        System.out.println(JSON.toJSONString(druidDataSourceProperty));
    }
}
