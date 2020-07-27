package learn.flow.dao.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import learn.flow.dao.entity.SysUserInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserInfoMapper extends BaseMapper<SysUserInfo> {
    int deleteByPrimaryKey(Integer userId);

    SysUserInfo selectByPrimaryKey(Integer userId);

    List<SysUserInfo> selectAll();

    int updateByPrimaryKey(SysUserInfo record);
}