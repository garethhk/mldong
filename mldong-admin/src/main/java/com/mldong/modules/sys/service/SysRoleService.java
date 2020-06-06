package com.mldong.modules.sys.service;

import java.util.List;

import com.mldong.common.base.CommonPage;
import com.mldong.modules.sys.dto.SysRoleParam;
import com.mldong.modules.sys.entity.SysRole;
/**
 * <p>业务接口层</p>
 * <p>角色</p>
 *
 * @since 2020-06-06 09:10:26
 */
public interface SysRoleService {
	/**
	 * 添加角色
	 * @param param
	 * @return
	 */
	public int save(SysRoleParam param);
	/**
	 * 更新角色
	 * @param param
	 * @return
	 */
	public int update(SysRoleParam param);
	/**
	 * 删除角色
	 * @param ids
	 * @return
	 */
	public int remove(List<Long> ids);
	/**
	 * 查询角色
	 * @param id
	 * @return
	 */
	public SysRole get(Long id);
	/**
	 * 分页查询角色列表
	 * @param t
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public CommonPage<SysRole> list(SysRoleParam t, int pageNum, int pageSize);

}