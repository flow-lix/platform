package learn.flow.dao.mapepr;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import learn.flow.dao.entity.SysUserInfo;
import learn.flow.dao.mapper.SysUserInfoMapper;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SimpleMapperTest {

    @Autowired
    private SysUserInfoMapper userMapper;
    @Autowired
    private SqlSession sqlSession;

    @Test
    public void aInsert() {
//        User user = new User();
//        user.setName("小羊");
//        user.setAge(3);
//        user.setEmail("abc@mp.com");
//        assertThat(userMapper.insert(user)).isGreaterThan(0);
//        // 成功直接拿会写的 ID
//        assertThat(user.getId()).isNotNull();
    }


    @Test
    public void bDelete() {
//        assertThat(userMapper.deleteById(3L)).isGreaterThan(0);
//        assertThat(userMapper.delete(new QueryWrapper<User>()
//                .lambda().eq(User::getName, "Sandy"))).isGreaterThan(0);
    }


    @Test
    public void cUpdate() {
//        assertThat(userMapper.updateById(new User().setId(1L).setEmail("ab@c.c"))).isGreaterThan(0);
//        assertThat(
//                userMapper.update(
//                        new User().setName("mp"),
//                        Wrappers.<User>lambdaUpdate()
//                                .set(User::getAge, 3)
//                                .eq(User::getId, 2)
//                )
//        ).isGreaterThan(0);
//        User user = userMapper.selectById(2);
//        assertThat(user.getAge()).isEqualTo(3);
//        assertThat(user.getName()).isEqualTo("mp");
//
//        userMapper.update(
//                null,
//                Wrappers.<User>lambdaUpdate().set(User::getEmail, null).eq(User::getId, 2)
//        );
//        assertThat(userMapper.selectById(1).getEmail()).isEqualTo("ab@c.c");
//        user = userMapper.selectById(2);
//        assertThat(user.getEmail()).isNull();
//        assertThat(user.getName()).isEqualTo("mp");
//
//        userMapper.update(
//                new User().setEmail("miemie@baomidou.com"),
//                new QueryWrapper<User>()
//                        .lambda().eq(User::getId, 2)
//        );
//        user = userMapper.selectById(2);
//        assertThat(user.getEmail()).isEqualTo("miemie@baomidou.com");
//
//        userMapper.update(
//                new User().setEmail("miemie2@baomidou.com"),
//                Wrappers.<User>lambdaUpdate()
//                        .set(User::getAge, null)
//                        .eq(User::getId, 2)
//        );
//        user = userMapper.selectById(2);
//        assertThat(user.getEmail()).isEqualTo("miemie2@baomidou.com");
//        assertThat(user.getAge()).isNull();
    }


    @Test
    public void dSelect() {
        userMapper.selectList(Wrappers.<SysUserInfo>lambdaQuery().select(SysUserInfo::getUserName))
                .forEach(x -> assertNotNull(x.getUserName()));

        userMapper.selectList(new QueryWrapper<SysUserInfo>().select("user_id", "user_name"))
                .forEach(x -> {
                    assertNotNull(x.getUserId());
                    assertNotNull(x.getUserName());
                    assertNull(x.getEmail());
                });
    }
//
//    @Test
//    public void orderBy() {
//        List<User> users = userMapper.selectList(Wrappers.<User>query().orderByAsc("age"));
//        assertThat(users).isNotEmpty();
//    }
//
//    @Test
//    public void selectMaps() {
//        List<Map<String, Object>> mapList = userMapper.selectMaps(Wrappers.<User>query().orderByAsc("age"));
//        assertThat(mapList).isNotEmpty();
//        assertThat(mapList.get(0)).isNotEmpty();
//        System.out.println(mapList.get(0));
//    }
//
//    @Test
//    public void selectMapsPage() {
//        IPage<Map<String, Object>> page = userMapper.selectMapsPage(new Page<>(1, 5), Wrappers.<User>query().orderByAsc("age"));
//        assertThat(page).isNotNull();
//        assertThat(page.getRecords()).isNotEmpty();
//        assertThat(page.getRecords().get(0)).isNotEmpty();
//        System.out.println(page.getRecords().get(0));
//    }
//
//    @Test
//    public void orderByLambda() {
//        List<User> users = userMapper.selectList(Wrappers.<User>lambdaQuery().orderByAsc(User::getAge));
//        assertThat(users).isNotEmpty();
//    }
//
//    @Test
//    public void testSelectMaxId() {
//        QueryWrapper<User> wrapper = new QueryWrapper<>();
//        wrapper.select("max(id) as id");
//        User user = userMapper.selectOne(wrapper);
//        System.out.println("maxId=" + user.getId());
//        List<User> users = userMapper.selectList(Wrappers.<User>lambdaQuery().orderByDesc(User::getId));
//        Assert.assertEquals(user.getId().longValue(), users.get(0).getId().longValue());
//    }
//
//    @Test
//    public void testGroup() {
//        QueryWrapper<User> wrapper = new QueryWrapper<>();
//        wrapper.select("age, count(*)")
//                .groupBy("age");
//        List<Map<String, Object>> maplist = userMapper.selectMaps(wrapper);
//        for (Map<String, Object> mp : maplist) {
//            System.out.println(mp);
//        }
//        /**
//         * lambdaQueryWrapper groupBy orderBy
//         */
//        LambdaQueryWrapper<User> lambdaQueryWrapper = new QueryWrapper<User>().lambda()
//                .select(User::getAge)
//                .groupBy(User::getAge)
//                .orderByAsc(User::getAge);
//        for (User user : userMapper.selectList(lambdaQueryWrapper)) {
//            System.out.println(user);
//        }
//    }
//
//    @Test
//    public void testTableFieldExistFalse(){
//        QueryWrapper<User> wrapper = new QueryWrapper<>();
//        wrapper.select("age, count(age) as count")
//                .groupBy("age");
//        List<User> list = userMapper.selectList(wrapper);
//        list.forEach(System.out::println);
//        list.forEach(x->{
//            Assert.assertNull(x.getId());
//            Assert.assertNotNull(x.getAge());
//            Assert.assertNotNull(x.getCount());
//        });
//        userMapper.insert(
//                new User().setId(10088L)
//                        .setName("miemie")
//                        .setEmail("miemie@baomidou.com")
//                        .setAge(3));
//        User miemie = userMapper.selectById(10088L);
//        Assert.assertNotNull(miemie);
//
//    }
}
