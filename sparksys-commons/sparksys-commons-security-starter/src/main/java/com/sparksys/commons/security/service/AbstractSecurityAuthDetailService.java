package com.sparksys.commons.security.service;

import com.sparksys.commons.core.constant.AuthConstant;
import com.sparksys.commons.core.entity.GlobalAuthUser;
import com.sparksys.commons.core.context.BaseContextHandler;
import com.sparksys.commons.security.entity.AuthUserDetail;
import com.sparksys.commons.security.event.LoginEvent;
import com.sparksys.commons.security.entity.LoginStatus;
import com.sparksys.commons.web.service.AbstractAuthUserRequest;
import com.sparksys.commons.core.support.ResponseResultStatus;
import com.sparksys.commons.web.component.SpringContextUtils;
import com.sparksys.commons.core.constant.CoreConstant;
import com.sparksys.commons.core.support.BusinessException;
import com.sparksys.commons.core.utils.crypto.MD5Utils;
import com.sparksys.commons.redis.cache.CacheProviderService;
import com.sparksys.commons.redis.cache.RedisCacheProviderImpl;
import com.sparksys.commons.security.entity.AuthToken;
import com.sparksys.commons.security.request.AuthRequest;
import com.sparksys.commons.core.utils.jwt.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * description: 登录授权Service
 *
 * @author zhouxinlei
 * @date 2020-05-24 13:39:06
 */
@Slf4j
public abstract class AbstractSecurityAuthDetailService extends AbstractAuthUserRequest {

    /**
     * 登录
     *
     * @param authRequest 登录认证
     * @return java.lang.String
     * @throws Exception 异常
     */
    public AuthToken login(AuthRequest authRequest){
        String account = authRequest.getAccount();
        String password = authRequest.getPassword();
        String token;
        AuthUserDetail adminUserDetails = getAuthUserDetail(account);
        ResponseResultStatus.ACCOUNT_EMPTY.assertNotNull(adminUserDetails);
        GlobalAuthUser authUser = adminUserDetails.getAuthUser();
        //校验密码输入是否正确
        checkPasswordError(authRequest, password, authUser);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(adminUserDetails,
                null, adminUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        token = JwtTokenUtil.generateToken(account);
        authUser.setPassword(null);
        AuthToken authToken = new AuthToken();
        authToken.setToken(token);
        authToken.setExpiration(CoreConstant.JwtTokenConstant.JWT_EXPIRATION);
        authToken.setAuthUser(authUser);
        //设置accessToken缓存
        accessToken(authToken, authUser);
        SpringContextUtils.publishEvent(new LoginEvent(LoginStatus.success(authUser.getId())));
        return authToken;
    }

    private void checkPasswordError(AuthRequest authRequest, String password, GlobalAuthUser authUser) {
        String encryptPassword = MD5Utils.encrypt(authRequest.getPassword());
        log.info("密码加密 = {}，数据库密码={}", password, encryptPassword);
        //数据库密码比对
        if (!StringUtils.equals(encryptPassword, authUser.getPassword())) {
            SpringContextUtils.publishEvent(new LoginEvent(LoginStatus.pwdError(authUser.getId(),
                    ResponseResultStatus.PASSWORD_ERROR.getMessage())));
            ResponseResultStatus.PASSWORD_ERROR.newException(password);
        }
    }

    /**
     * 设置accessToken缓存
     *
     * @param authToken 用户token
     * @param authUser  认证用户
     * @return void
     */
    private void accessToken(AuthToken authToken, GlobalAuthUser authUser) {
        CacheProviderService cacheProviderService = SpringContextUtils.getBean(RedisCacheProviderImpl.class);
        String token = authToken.getToken();
        BaseContextHandler.setAccount(authUser.getAccount());
        BaseContextHandler.setUserId(authUser.getId());
        BaseContextHandler.setName(authUser.getName());
        cacheProviderService.set(AuthConstant.AUTH_USER + token, authUser,
                authToken.getExpiration());
    }

    /**
     * 根据用户名获取用户信息
     *
     * @param account 用户名
     * @return AdminUserDetails
     * @throws BusinessException 异常
     */
    public abstract AuthUserDetail getAuthUserDetail(String account);

    /**
     * 获取用户权限
     *
     * @param adminId 用户id
     * @return List<String>
     * @author zhouxinlei
     * @date 2019-09-11 17:46:56
     */
    public abstract List<String> listOauthPermission(Long adminId);

}
