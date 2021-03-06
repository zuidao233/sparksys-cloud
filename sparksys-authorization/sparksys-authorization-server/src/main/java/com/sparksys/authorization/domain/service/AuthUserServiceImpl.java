package com.sparksys.authorization.domain.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Sets;
import com.sparksys.commons.core.base.api.result.ApiPageResult;
import com.sparksys.commons.core.entity.GlobalAuthUser;
import com.sparksys.commons.core.utils.collection.ListUtils;
import com.sparksys.commons.mybatis.page.PageResult;
import com.sparksys.authorization.application.service.IAuthUserService;
import com.sparksys.authorization.domain.constant.AuthorizationConstant;
import com.sparksys.authorization.domain.repository.IAuthUserRepository;
import com.sparksys.authorization.infrastructure.convert.AuthUserConvert;
import com.sparksys.authorization.infrastructure.entity.AuthUser;
import com.sparksys.authorization.interfaces.dto.user.AuthUserDTO;
import com.sparksys.authorization.interfaces.dto.user.AuthUserSaveDTO;
import com.sparksys.authorization.interfaces.dto.user.AuthUserStatusDTO;
import com.sparksys.authorization.interfaces.dto.user.AuthUserUpdateDTO;
import com.sparksys.commons.security.entity.AuthUserDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * description: 用户查询 服务实现类
 *
 * @author zhouxinlei
 * @date 2020-05-24 12:22:57
 */
@Service
@Slf4j
public class AuthUserServiceImpl implements IAuthUserService {

    private final IAuthUserRepository authUserRepository;

    public AuthUserServiceImpl(IAuthUserRepository authUserRepository) {
        this.authUserRepository = authUserRepository;
    }


    @Override
    public boolean saveAuthUser(GlobalAuthUser authUser, AuthUserSaveDTO authUserSaveDTO) {
        AuthUser authUserDO = AuthUserConvert.INSTANCE.convertAuthUserDO(authUserSaveDTO);
        return authUserRepository.saveAuthUser(authUserDO);
    }

    @Override
    public boolean updateAuthUser(GlobalAuthUser authUser, AuthUserUpdateDTO authUserUpdateDTO) {
        AuthUser authUserDO = AuthUserConvert.INSTANCE.convertAuthUserDO(authUserUpdateDTO);
        return authUserRepository.updateAuthUser(authUserDO);
    }

    @Override
    public boolean deleteAuthUser(Long id) {
        return authUserRepository.deleteAuthUser(id);
    }

    @Override
    public boolean updateAuthUserStatus(GlobalAuthUser authUser, AuthUserStatusDTO authUserStatusDTO) {
        authUserStatusDTO.setUpdateUser(authUser.getId());
        AuthUser authUserDO = AuthUserConvert.INSTANCE.convertAuthUserDO(authUserStatusDTO);
        return authUserRepository.updateAuthUser(authUserDO);
    }

    @Override
    public ApiPageResult listByPage(Integer pageNum, Integer pageSize, String name) {
        Page<AuthUser> userDOIPage = authUserRepository.listByPage(new Page(pageNum, pageSize), name);
        List<AuthUser> authUserList = userDOIPage.getRecords();
        List<AuthUserDTO> authUserDTOS =
                authUserList.stream().map(authUserDO -> {
                    AuthUserDTO authUserDTO = AuthUserConvert.INSTANCE.convertAuthUserDTO(authUserDO);
                    String sex = AuthorizationConstant.SEX_MAP.get(authUserDO.getSex());
                    authUserDTO.setSex(sex);
                    return authUserDTO;
                }).collect(Collectors.toList());
        return PageResult.resetPage(userDOIPage, authUserDTOS);
    }

    @Override
    public AuthUserDTO getAuthUser(Long id) {
        AuthUser authUser = authUserRepository.selectById(id);
        AuthUserDTO authUserDTO = AuthUserConvert.INSTANCE.convertAuthUserDTO(authUser);
        String sex = AuthorizationConstant.SEX_MAP.get(authUser.getSex());
        authUserDTO.setSex(sex);
        return authUserDTO;
    }

    @Override
    public boolean resetPassErrorNum(Long id) {
        AuthUser authUser = new AuthUser();
        authUser.setId(id);
        authUser.setPasswordErrorNum(0);
        authUser.setPasswordErrorLastTime(null);
        return authUserRepository.updateAuthUser(authUser);
    }

    @Override
    public boolean resetPassErrorNum(String account) {
        AuthUser authUser = new AuthUser();
        authUser.setAccount(account);
        authUser.setPasswordErrorNum(0);
        authUser.setPasswordErrorLastTime(null);
        return authUserRepository.updateAuthUser(authUser);
    }

    @Override
    public boolean incrPasswordErrorNum(Long id) {
        return authUserRepository.incrPasswordErrorNumById(id);
    }

    @Override
    public boolean incrPasswordErrorNum(String account) {
        return authUserRepository.incrPasswordErrorNumByAccount(account);
    }

    @Override
    public Set<String> getAuthUserPermissions(Long id) {
        List<String> userPermissions = authUserRepository.getAuthUserPermissions(id);
        if (ListUtils.isNotEmpty(userPermissions)) {
            return new HashSet<>(userPermissions);
        }
        return Sets.newHashSet();
    }

    @Override
    public AuthUserDetail getAuthUserDetail(String account) {
        AuthUser authUser = authUserRepository.selectByAccount(account);
        if (ObjectUtils.isNotEmpty(authUser)) {
            List<String> userPermissions = authUserRepository.getAuthUserPermissions(authUser.getId());
            GlobalAuthUser globalAuthUser = AuthUserConvert.INSTANCE.convertGlobalAuthUser(authUser);
            globalAuthUser.setPermissions(userPermissions);
            return new AuthUserDetail(globalAuthUser);
        }
        return null;
    }
}
