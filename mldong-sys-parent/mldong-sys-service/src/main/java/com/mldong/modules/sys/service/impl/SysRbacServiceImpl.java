package com.mldong.modules.sys.service.impl;

import java.util.*;
import java.util.stream.Collectors;


import com.mldong.common.base.PageParam;
import com.mldong.common.base.YesNoEnum;
import com.mldong.common.tool.StringTool;
import com.mldong.modules.sys.vo.MetaVo;
import com.mldong.modules.sys.vo.RouterVo;
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
import com.mldong.modules.sys.dao.SysUserDao;
import com.mldong.modules.sys.dto.SysUserWithRoleIdPageParam;
import com.mldong.modules.sys.entity.SysMenu;
import com.mldong.modules.sys.entity.SysRoleAccess;
import com.mldong.modules.sys.entity.SysRoleMenu;
import com.mldong.modules.sys.entity.SysUser;
import com.mldong.modules.sys.entity.SysUserRole;
import com.mldong.modules.sys.mapper.SysMenuMapper;
import com.mldong.modules.sys.mapper.SysRoleAccessMapper;
import com.mldong.modules.sys.mapper.SysRoleMenuMapper;
import com.mldong.modules.sys.mapper.SysUserRoleMapper;
import com.mldong.modules.sys.service.SysRbacService;
import com.mldong.modules.sys.vo.SysAccessTreeVo;
import com.mldong.modules.sys.vo.SysMenuTreeVo;

@Service
public class SysRbacServiceImpl implements SysRbacService{
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
	private SysMenuMapper sysMenuMapper;
	@Override
	public SysAccessTreeVo listAccessTree(Long userId, Long roleId) {
		SysAccessTreeVo vo = new SysAccessTreeVo();
		vo.setDefaultCheckedKeys(new ArrayList<>());
		vo.setDefaultExpandedKeys(new ArrayList<>());
		SysRoleAccess q = new SysRoleAccess();
		q.setRoleId(roleId);
		List<SysRoleAccess> roleAccessList = sysRoleAccessMapper.select(q);
		roleAccessList.forEach(item->{
			vo.getDefaultCheckedKeys().add(item.getAccess());
		});
		List<SysAccessModel> allAccess = accessInitProcessor.getAccessList();
		vo.setRows(allAccess);
		// ?????????????????????
		allAccess.forEach(item->{
			vo.getDefaultExpandedKeys().add(item.getAccess());
		});
		return vo;
	}
	@Override
	public SysMenuTreeVo listMenuByRoleId(Long userId, Long roleId) {
		SysMenuTreeVo vo = new SysMenuTreeVo();
		vo.setDefaultCheckedKeys(new ArrayList<>());
		vo.setDefaultExpandedKeys(new ArrayList<>());
		SysRoleMenu q = new SysRoleMenu();
		q.setRoleId(roleId);
		List<SysRoleMenu> roleMenuList = sysRoleMenuMapper.select(q);
		roleMenuList.forEach(item->{
			vo.getDefaultCheckedKeys().add(item.getMenuId().toString());
		});
		List<SysMenu> allMenu = sysMenuMapper.selectAll();
		vo.setRows(allMenu);
		// ?????????????????????
		allMenu.stream().filter(item->{
			return new Long(0L).equals(item.getParentId());
		}).forEach(item->{
			vo.getDefaultExpandedKeys().add(item.getId().toString());
		});
		return vo;
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
		List<SysRoleAccess> insertRoleAccessList = new ArrayList<>();
		param.getIds().forEach(access->{
			SysRoleAccess q = new SysRoleAccess();
			q.setAccess(access);
			q.setRoleId(roleId);
			int count = sysRoleAccessMapper.selectCount(q);
			if(count == 0) {
				q.setCreateTime(now);
				q.setUpdateTime(now);
				q.setIsDeleted(YesNoEnum.NO);
				insertRoleAccessList.add(q);
			}
		});
		if(!insertRoleAccessList.isEmpty()){
			sysRoleAccessMapper.insertList(insertRoleAccessList);
		}
		
		Condition delConditin = new Condition(SysRoleAccess.class);
		if(param.getIds().isEmpty()) {
			delConditin.createCriteria().andEqualTo("roleId", roleId);
			// ????????????????????????????????????
			sysRoleAccessMapper.deleteByCondition(delConditin);
		} else {
			delConditin.createCriteria().andEqualTo("roleId", roleId)
			.andNotIn("access", param.getIds());
			// ????????????????????????????????????
			sysRoleAccessMapper.deleteByCondition(delConditin);
		}
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
		List<SysRoleMenu> insertRoleMenuList = new ArrayList<>();
		param.getIds().forEach(id->{
			Long menuId = Long.parseLong(id);
			SysRoleMenu q = new SysRoleMenu();
			q.setMenuId(menuId);
			q.setRoleId(roleId);
			int count = sysRoleMenuMapper.selectCount(q);
			if(count == 0) {
				q.setCreateTime(now);
				q.setUpdateTime(now);
				q.setIsDeleted(YesNoEnum.NO);
				insertRoleMenuList.add(q);
			}
		});
		if(!insertRoleMenuList.isEmpty()) {
			sysRoleMenuMapper.insertList(insertRoleMenuList);
		}
		Condition delConditin = new Condition(SysRoleMenu.class);
		if(param.getIds().isEmpty()) {
			delConditin.createCriteria().andEqualTo("roleId", roleId);
			// ????????????????????????????????????
			sysRoleMenuMapper.deleteByCondition(delConditin);
		} else {
			delConditin.createCriteria().andEqualTo("roleId", roleId)
			.andNotIn("menuId", param.getIds());
			// ????????????????????????????????????
			sysRoleMenuMapper.deleteByCondition(delConditin);
		}
		return 1;
	}
	@Caching(evict={
			@CacheEvict(value="menu_user_id"),
			@CacheEvict(value="router_user_id")
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
		if(userId.equals(globalProperties.getSuperAdminId())) {
			return Arrays.asList("admin");
		}
		return sysUserDao.selectUserAccess(userId).stream()
				.map(item->{
					return item.getAccess();
				}).collect(Collectors.toList());
	}
	@Override
	@Cacheable(value = "menu_user_id",key="#userId")
	public List<SysMenu> loadUserMenuList(Long userId) {
		PageParam<SysMenu> pageParam = new PageParam<>();
		pageParam.setPageSize(10000);
		Page<SysMenu> page = pageParam.buildPage(false);
		page.setOrderBy("sort asc");
		if(userId.equals(globalProperties.getSuperAdminId())) {
			return sysMenuMapper.selectAll();
		}
		List<SysMenu> userMenuList =  sysUserDao.selectUserMenu(userId);
		List<SysMenu> noParentList = userMenuList.stream().filter(item->{
			return userMenuList.stream().filter(itemm-> {
				return itemm.getId().equals(item.getParentId());
			}).count() == 0 && (item.getParentId() !=null || !new Long(0).equals(item.getParentId()));
		}).collect(Collectors.toList());
		tranfer(userMenuList, noParentList,1);
		return userMenuList;
	}

	@Override
	@Cacheable(value = "router_user_id",key="#userId")
	public List<RouterVo> getRouters(Long userId) {
		List<SysMenu> menus = loadUserMenuList(userId);
		return buildTree(menus, 0L);
	}
	private List<RouterVo> buildTree(List<SysMenu> menus, Long id) {
		List<RouterVo> routers = new ArrayList<>();
		menus.forEach(menu -> {
			if(id.equals(menu.getParentId())) {
				List<RouterVo> children = buildTree(menus, menu.getId());
				RouterVo router = new RouterVo();
				router.setAlwayShow(true);
				if(StringTool.isEmpty(menu.getPath())) {
					router.setComponent(menu.getRouteName().replaceAll(":","/"));
				} else {
					if(menu.getPath().startsWith("/")) {
						router.setComponent(menu.getPath().replaceFirst("/",""));
					} else {
						router.setComponent(menu.getPath());
					}
				}
				router.setHidden(YesNoEnum.NO.equals(menu.getIsShow()));
				// ???????????????????????????????????????????????????
				router.setName(StringTool.upperCase(StringTool.lineToHump(menu.getRouteName().replaceAll(":","_"))));
				router.setPath("/" + router.getComponent());
				router.setRedirect(null);
				router.setChildren(children);
				MetaVo meta = new MetaVo();
				meta.setActiveMenu(null);
				meta.setAffix(false);
				meta.setBreadcrumb(true);
				meta.setIcon(menu.getIcon());
				meta.setNoCache(YesNoEnum.NO.equals(menu.getIsCache()));
				meta.setTagName(menu.getRemark());
				meta.setTitle(menu.getName());
				router.setMeta(meta);
				if(children.size() > 0) {
					router.setChildren(children);
				}
				if(!menu.getRouteName().contains(":")) {
					router.setComponent("Layout");
				}
				routers.add(router);
			}
		});
		return routers;
	}
	/**
	 * ?????????????????????????????????
	 * @param userMenuList ??????????????????
	 * @param noParentList ????????????????????????
	 * @param level ??????????????????5???
	 */
	private void tranfer(List<SysMenu> userMenuList, List<SysMenu> noParentList, int level){
		if(noParentList.isEmpty() || level > 5) {
			return;
		}
		Condition condition = new Condition(SysMenu.class);
		condition.createCriteria().andIn("id", noParentList.stream().map(item->{
			return item.getParentId();
		}).collect(Collectors.toList()));
		List<SysMenu> newList = sysMenuMapper.selectByCondition(condition);
		if(newList.isEmpty()) {
			return;
		}
		userMenuList.addAll(newList);
		List<SysMenu> newNoParentList = userMenuList.stream().filter(item->{
			return userMenuList.stream().filter(itemm-> {
				return itemm.getId().equals(item.getParentId());
			}).count() == 0 && (item.getParentId() !=null || !new Long(0).equals(item.getParentId()));
		}).collect(Collectors.toList());
		tranfer(userMenuList, newNoParentList,level+1);
	}
}
