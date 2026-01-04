package com.bajiezu.cloud.product.service;

import com.bajiezu.cloud.common.web.pojo.PageResult;
import com.bajiezu.cloud.product.controller.vo.request.*;
import com.bajiezu.cloud.product.controller.vo.response.PBRespVO;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ProductBrandService {

    void add(PBAddReqVO reqVO);

    void mod(PBModReqVO reqVO);

    void del(PBDelReqVO reqVO);

    PageResult<PBRespVO> list(PBListReqVO reqVO);

    void statusChange(PBStatusChangeVO reqVO);

    List<PBRespVO> simpleList(PBListReqVO reqVO);
}
