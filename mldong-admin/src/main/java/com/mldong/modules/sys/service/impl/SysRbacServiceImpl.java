package com.mldong.modules.sys.service.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import tk.mybatis.mapper.entity.Condition;

import com.github.pagehelper.Page;
import com.mldong.common.access.AccessInitProcessor;
import com.mldong.common.access.model.SysAccessModel;
import com.mldong.common.base.CommonPage;
import com.mldong.common.base.IdAndIdsParam;
import com.mldong.common.config.GlobalProperties;
import com.mldong.common.interceptor.AuthInterceptorService;
import com.mldong.common.token.TokenStrategy;
import com.mldong.modules.sys.dao.SysUserDao;
import com.mldong.modules.sys.dto.SysUserWithRoleIdPageParam;
import com.mldong.modules.sys.entity.SysMenu;
import com.mldong.modules.sys.entity.SysRoleAccess;
import com.mldong.modules.sys.entity.SysRoleMenu;
import com.mldong.modules.sys.entity.SysUser;
import com.mldong.modules.sys.entity.SysUserRole;
import com.mldong.modules.sys.mapper.SysRoleAccessMapper;
import com.mldong.modules.sys.mapper.SysRoleMenuMapper;
import com.mldong.modules.sys.mapper.SysUserRoleMapper;
import com.mldong.modules.sys.service.SysRbacService;

@Service
public class SysRbacServiceImpl implements SysRbacService, AuthInterceptorService{
	@Autowired
	private AccessInitProcessor accessInitProcessor;
	@Autowired
	private SysUserDao sysUserDao;
	@Autowired
	private SysUserRoleMapper sysUserRoleMapper;
	@Autowired
	private SysRoleAccessMapper sysRoleAccessMapper;
	@Autowired
	private SysRoleMenuMapper sysRoleMenuMapper;
	@Autowired
	private GlobalProperties globalProperties;
	@Autowired
	private TokenStrategy tokenStrategy;
	@Override
	public List<SysAccessModel> listAccessTree() {
		return accessInitProcessor.getAccessList();
	}
	@Override
	public CommonPage<SysUser> listUserByRoleId(SysUserWithRoleIdPageParam param) {
		Page<SysUser> page = param.buildPage();
		sysUserDao.selectUserByRoleId(param);
		return CommonPage.toPage(page);
	}
	@Caching(evict={
			@CacheEvict(value="access_user_id")
	})
	@Override
	public int saveUserRole(IdAndIdsParam param) {
		Date now = new Date();
		Long roleId = Long.parseLong(param.getId());
		param.getIds().forEach(id->{
			Long userId = Long.parseLong(id);
			SysUserRole q = new SysUserRole();
			q.setUserId(userId);
			q.setRoleId(roleId);
			int count = sysUserRoleMapper.selectCount(q);
			if(count == 0) {
				q.setCreateTime(now);
				q.setUpdateTime(now);
				sysUserRoleMapper.insertSelective(q);
			}
		});
		return 1;
	}
	@Caching(evict={
			@CacheEvict(value="access_user_id")
	})
	@Override
	public int deleteUserRole(IdAndIdsParam param) {
		Condition condition = new Condition(SysUserRole.class);
    	condition.createCriteria().andEqualTo("roleId", param.getId())
    	.andIn("userId", param.getIds());
		return sysUserRoleMapper.deleteByCondition(condition);
	}
	@Override
	public CommonPage<SysUser> listUserNoInRole(SysUserWithRoleIdPageParam param) {
		Page<SysUser> page = param.buildPage();
		sysUserDao.selectUserNoInRole(param);
		return CommonPage.toPage(page);
	}
	@Caching(evict={
			@CacheEvict(value="access_user_id")
	})
	@Override
	public int saveRoleAccess(IdAndIdsParam param) {
		Date now = new Date();
		Long roleId = Long.parseLong(param.getId());
		param.getIds().forEach(access->{
			SysRoleAccess q = new SysRoleAccess();
			q.setAccess(access);
			q.setRoleId(roleId);
			int count = sysRoleAccessMapper.selectCount(q);
			if(count == 0) {
				q.setCreateTime(now);
				q.setUpdateTime(now);
				sysRoleAccessMapper.insertSelective(q);
			}
		});
		return 1;
	}
	@Caching(evict={
			@CacheEvict(value="access_user_id")
	})
	@Override
	public int deleteRoleAccess(IdAndIdsParam param) {
		Condition condition = new Condition(SysRoleAccess.class);
    	condition.createCriteria().andEqualTo("roleId", param.getId())
    	.andIn("access", param.getIds());
		return sysRoleAccessMapper.deleteByCondition(condition);
	}
	@Caching(evict={
			@CacheEvict(value="menu_user_id")
	})
	@Override
	public int saveRoleMenu(IdAndIdsParam param) {
		Date now = new Date();
		Long roleId = Long.parseLong(param.getId());
		param.getIds().forEach(id->{
			Long menuId = Long.parseLong(id);
			SysRoleMenu q = new SysRoleMenu();
			q.setMenuId(menuId);
			q.setRoleId(roleId);
			int count = sysRoleMenuMapper.selectCount(q);
			if(count == 0) {
				q.setCreateTime(now);
				q.setUpdateTime(now);
				sysRoleMenuMapper.insertSelective(q);
			}
		});
		return 1;
	}
	@Caching(evict={
			@CacheEvict(value="menu_user_id")
	})
	@Override
	public int deleteRoleMenu(IdAndIdsParam param) {
		Condition condition = new Condition(SysRoleMenu.class);
    	condition.createCriteria().andEqualTo("roleId", param.getId())
    	.andIn("menuId", param.getIds());
		return sysRoleMenuMapper.deleteByCondition(condition);
	}
	@Override
	public boolean hasAccess(Long userId, String access) {
		if(userId.equals(globalProperties.getSuperAdminId())) {
			return true;
		}
 		return loadUserAccessList(userId).contains(access);
	}
	@Override
	@Cacheable(value = "access_user_id",key="#userId")
	public List<String> loadUserAccessList(Long userId) {
		return sysUserDao.selectUserAccess(userId).stream()
				.map(item->{
					return item.getAccess();
				}).collect(Collectors.toList());
	}
	@Override
	@Cacheable(value = "menu_user_id",key="#userId")
	public List<SysMenu> loadUserMenuList(Long userId) {
		return sysUserDao.selectUserMenu(userId);
	}
	@Override
	public boolean verifyToken(String token) {
		return tokenStrategy.verifyToken(token);
	}
	@Override
	public boolean hasAuth(String token, String access) {
		Long userId = tokenStrategy.getUserId(token);
		return hasAccess(userId, access);
	}

}