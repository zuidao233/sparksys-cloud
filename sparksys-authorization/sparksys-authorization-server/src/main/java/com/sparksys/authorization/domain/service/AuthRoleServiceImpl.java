package com.sparksys.authorization.domain.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sparksys.authorization.application.service.IAuthRoleService;
import com.sparksys.authorization.domain.repository.IAuthRoleRepository;
import com.sparksys.authorization.infrastructure.convert.AuthRoleConvert;
import com.sparksys.authorization.infrastructure.entity.AuthRole;
import com.sparksys.authorization.interfaces.dto.role.AuthRoleDTO;
import com.sparksys.authorization.interfaces.dto.role.AuthRoleSaveDTO;
import com.sparksys.authorization.interfaces.dto.role.AuthRoleUpdateDTO;
import com.sparksys.commons.core.base.api.result.ApiPageResult;
import com.sparksys.commons.core.entity.GlobalAuthUser;
import com.sparksys.commons.mybatis.page.PageResult;
import org.springframework.stereotype.Service;

/**
 * description: 角色 服务实现类
 *
 * @author zhouxinlei
 * @date 2020-06-07 13:37:09
 */
@Service
public class AuthRoleServiceImpl implements IAuthRoleService {

    private final IAuthRoleRepository authRoleRepository;

    public AuthRoleServiceImpl(IAuthRoleRepository authRoleRepository) {
        this.authRoleRepository = authRoleRepository;
    }

    @Override
    public ApiPageResult listByPage(Integer pageNum, Integer pageSize, String name) {
        Page<AuthRole> roleDOPage = authRoleRepository.listByPage(new Page(pageNum, pageSize), name);
        return PageResult.resetPage(roleDOPage);
    }

    @Override
    public AuthRoleDTO getAuthRole(Long id) {
        AuthRole authRole = authRoleRepository.getAuthRole(id);
        return AuthRoleConvert.INSTANCE.convertAuthUserDTO(authRole);
    }

    @Override
    public boolean saveAuthRole(GlobalAuthUser authUser, AuthRoleSaveDTO authRoleSaveDTO) {
        AuthRole authRole = AuthRoleConvert.INSTANCE.convertAuthRoleDO(authRoleSaveDTO);
        authRole.setCreateUser(authUser.getId());
        authRole.setUpdateUser(authUser.getId());
        return authRoleRepository.saveAuthRole(authRole);
    }

    @Override
    public boolean updateAuthRole(GlobalAuthUser authUser, AuthRoleUpdateDTO authRoleUpdateDTO) {
        AuthRole authRole = AuthRoleConvert.INSTANCE.convertAuthRoleDO(authRoleUpdateDTO);
        authRole.setUpdateUser(authUser.getId());
        return authRoleRepository.updateAuthRole(authRole);
    }

    @Override
    public boolean deleteAuthRole(Long id) {
        return authRoleRepository.deleteAuthRole(id);
    }

    @Override
    public boolean updateAuthRoleStatus(Long userId, Long roleId, Boolean status) {
        AuthRole authRole = new AuthRole();
        authRole.setId(roleId);
        authRole.setStatus(status);
        authRole.setUpdateUser(userId);
        return authRoleRepository.updateAuthRole(authRole);
    }
}
