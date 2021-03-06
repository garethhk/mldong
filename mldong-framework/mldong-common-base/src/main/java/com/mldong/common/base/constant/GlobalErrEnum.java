package com.mldong.common.base.constant;

import com.mldong.common.annotation.ErrEnum;
import com.mldong.common.base.CommonError;
/**
 * 全局错误码
 * @author mldong
 *
 */
@ErrEnum(name="global",value="全局错误码",bizCode=9999,min=0,max=1000)
public enum GlobalErrEnum implements CommonError {
	/**
	 * 参数异常
	 */
	GL99990100(99990100, "参数异常"),
	/**
	 * 未授权
	 */
	GL99990401(99990401, "未授权"),
	/**
	 * 未知异常
	 */
	GL99990500(99990500, "未知异常"),
	/**
	 * 无权限访问
	 */
	GL99990403(99990403, "无权限访问"),
	/**
	 * 找不到指定资源
	 */
	GL99990404(99990404, "找不到指定资源"),
	/**
	 * 注解使用错误
	 */
	GL99990001(99990001, "注解使用错误"),
	/**
	 * 微服务不在线,或者网络超时
	 */
	GL99990002(99990002, "微服务不在线,或者网络超时"),
	/**
	 * 没有数据
	 */
	GL99990003(99990003, "没有数据"),
	/**
	 * 演示账号，无写权限
	 */
	GL99990004(99990004, "演示账号，无写权限"),
	/**
	 * 数据库插入异常
	 */
	GL99990005(99990005, "数据库插入异常"),
	/**
	 * 文件后辍不允许
	 */
	GL99990006(99990006, "文件后辍不允许"),
	/**
	 * 文件上传异常
	 */
	GL99990007(99990007, "文件上传异常"),
	/**
	 * 文件上传配置不存在
	 */
	GL99990008(99990008, "文件上传配置不存在"),
	/**
	 * 文件超过上传最大值
	 */
	GL99990009(99990009, "文件超过上传最大值"),
	/**
	 * 审核不通过原因不能为空
	 */
	GL99990010(99990010, "审核不通过原因不能为空"),
	/**
	 * 图片验证码生成异常
	 */
	GL99990011(99990011, "图片验证码生成异常"),
	/**
	 * 图片验证码错误或不存在
	 */
	GL99990012(99990012, "图片验证码错误或不存在"),
	/**
	 * 演示站无访问权限
	 */
	GL99990013(99990013, "演示站无访问权限"),
	;
	
	private GlobalErrEnum(int value, String name){
		this.value = value;
		this.name = name;
	} 
	private String name;

	private int value;

	public int getValue() {
		return value;
	}
	public String getName() {
		return name;
	}

}
