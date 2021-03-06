package com.sparksys.commons.redis.components;

import cn.hutool.core.util.IdUtil;
import com.sparksys.commons.redis.cache.CacheProviderService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * description: redis token生成组件
 *
 * @author zhouxinlei
 * @date 2020-05-24 13:29:13
 */
@Component
public class RedisTokenUtil {

    private final CacheProviderService cacheProviderService;

    public RedisTokenUtil(CacheProviderService cacheProviderService) {
        this.cacheProviderService = cacheProviderService;
    }

    public String getToken() {
        String token = "token".concat(IdUtil.simpleUUID());
        long expire = 60 * 60;
        cacheProviderService.set(token, token, expire);
        return token;
    }


    public boolean findToken(String token) {
        String value = cacheProviderService.get(token);
        if (!StringUtils.isEmpty(value)) {
            cacheProviderService.remove(token);
            return true;
        }
        return false;
    }
}
