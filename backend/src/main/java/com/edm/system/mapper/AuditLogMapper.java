package com.edm.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edm.system.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
