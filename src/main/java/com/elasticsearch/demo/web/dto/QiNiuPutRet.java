package com.elasticsearch.demo.web.dto;

import lombok.Data;
import lombok.ToString;

/**
 * @author zhumingli
 * @create 2018-08-27 下午11:42
 * @desc
 **/
@Data
@ToString
public final class QiNiuPutRet  {

    public String key;

    public String hash;

    public String bucket;

    public int width;

    public int height;
}
