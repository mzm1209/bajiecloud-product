package com.bajiezu.cloud.product.dal.mapper;
import com.bajiezu.cloud.product.dal.entity.AssetResidualYearConfig;import com.baomidou.mybatisplus.core.mapper.BaseMapper;import org.apache.ibatis.annotations.Mapper;import org.apache.ibatis.annotations.Param;import java.util.Collection;import java.util.Date;import java.util.List;
@Mapper
public interface AssetResidualYearConfigMapper extends BaseMapper<AssetResidualYearConfig>{ List<AssetResidualYearConfig> selectByConfigId(@Param("configId") Long configId,@Param("partnerId") Long partnerId); void insertBatch(@Param("list") Collection<AssetResidualYearConfig> list); void logicDelByConfigId(@Param("configId") Long configId,@Param("partnerId") Long partnerId,@Param("updateBy") Long updateBy,@Param("updateTime") Date updateTime);}
