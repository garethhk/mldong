package com.mldong.common.tk;

import com.mldong.common.base.OperateTypeEnum;
import com.mldong.common.base.PageParam;
import com.mldong.common.base.WhereParam;
import com.mldong.common.base.constant.GlobalErrEnum;
import com.mldong.common.exception.BizException;
import com.mldong.common.tool.StringTool;
import tk.mybatis.mapper.entity.Condition;
import tk.mybatis.mapper.entity.Example.Criteria;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * tk条件工具封装
 * @author mldong
 *
 */
public class ConditionUtil {
	private ConditionUtil() {}
	/**
	 * 通过查询构型构造tk查询条件
	 * @param clazz
	 * @param list
	 * @return
	 */
	public static Condition buildCondition(Class<?> clazz,List<WhereParam> list) {
		Condition condition = new Condition(clazz);
		Criteria criteria = condition.createCriteria();
		for(WhereParam model: list) {
			switch (model.getOperateType()) {
			case EQ:
				criteria.andEqualTo(model.getPropertyName(), model.getPropertyValue());
				break;
			case NE:
				criteria.andNotEqualTo(model.getPropertyName(), model.getPropertyValue());
				break;
			case GT:
				criteria.andGreaterThan(model.getPropertyName(), model.getPropertyValue());
				break;
			case GE:
				criteria.andGreaterThanOrEqualTo(model.getPropertyName(), model.getPropertyValue());
				break;
			case LT:
				criteria.andLessThan(model.getPropertyName(), model.getPropertyValue());
				break;
			case LE:
				criteria.andLessThanOrEqualTo(model.getPropertyName(), model.getPropertyValue());
				break;
			case BT:
				List<Object> listObject = (List<Object>) model.getPropertyValue();
				criteria.andBetween(model.getPropertyName(), listObject.get(0),listObject.get(1));
				break;
			case NBT:
				listObject = (List<Object>) model.getPropertyValue();
				criteria.andNotBetween(model.getPropertyName(), listObject.get(0),listObject.get(1));
				break;
			case LIKE:
				criteria.andLike(model.getPropertyName(), "%"+model.getPropertyValue()+"%");
				break;
			case LLIKE:
				criteria.andLike(model.getPropertyName(), "%"+model.getPropertyValue());
				break;
			case RLIKE:
				criteria.andLike(model.getPropertyName(), model.getPropertyValue()+"%");
				break;
			case IN:
				listObject = (List<Object>) model.getPropertyValue();
				if(!listObject.isEmpty()) {
					criteria.andIn(model.getPropertyName(), listObject );
				}
				break;
			case NIN:
				listObject = (List<Object>) model.getPropertyValue();
				if(!listObject.isEmpty()) {
					criteria.andNotIn(model.getPropertyName(), listObject );
				}
				break;
			case OR:
				Map<String,Object> map = (Map<String, Object>) model.getPropertyValue();
				WhereParam param = new WhereParam();
				param.setOperateType(OperateTypeEnum.valueOf(map.get("operateType").toString()));
				param.setPropertyName(map.get("propertyName").toString());
				param.setPropertyValue(map.get("propertyValue"));
				switch (param.getOperateType()) {
				case EQ:
					criteria.orEqualTo(param.getPropertyName(), param.getPropertyValue());
					break;
				case NE:
					criteria.orNotEqualTo(param.getPropertyName(), param.getPropertyValue());
					break;
				case GT:
					criteria.orGreaterThan(param.getPropertyName(), param.getPropertyValue());
					break;
				case GE:
					criteria.orGreaterThanOrEqualTo(param.getPropertyName(), param.getPropertyValue());
					break;
				case LT:
					criteria.orLessThan(param.getPropertyName(), param.getPropertyValue());
					break;
				case LE:
					criteria.orLessThanOrEqualTo(param.getPropertyName(), param.getPropertyValue());
					break;
				case BT:
					listObject = (List<Object>) param.getPropertyValue();
					criteria.andBetween(param.getPropertyName(), listObject.get(0),listObject.get(1));
					break;
				case NBT:
					listObject = (List<Object>) param.getPropertyValue();
					criteria.orNotBetween(param.getPropertyName(), listObject.get(0),listObject.get(1));
					break;
				case LIKE:
					criteria.orLike(param.getPropertyName(), "%"+param.getPropertyValue()+"%");
					break;
				case LLIKE:
					criteria.orLike(param.getPropertyName(), "%"+param.getPropertyValue());
					break;
				case RLIKE:
					criteria.orLike(model.getPropertyName(), param.getPropertyValue()+"%");
					break;
				case IN:
					listObject = (List<Object>) param.getPropertyValue();
					if(!listObject.isEmpty()) {
						criteria.orIn(param.getPropertyName(), listObject );
					}
					break;
				case NIN:
					listObject = (List<Object>) param.getPropertyValue();
					if(!listObject.isEmpty()) {
						criteria.orNotIn(param.getPropertyName(), listObject );
					}
					break;
				default:
					break;
				}
			default:
				break;
			}
		}
		if(condition.getOredCriteria().get(0).getCriteria().size() == 0) {
			// 如果没有加入任何条件，需要置空
			condition = null;
		}
		return condition;
	}

	/**
	 * 将属性值转成whereParams对象
	 * @param param
	 * @return
	 */
	public static List<WhereParam> propertyConvertWhereParams(Map<String,Object> param) {
		List<WhereParam> res = new ArrayList<>();
		param.forEach((k,v) -> {
			String [] arr = k.split("_");
			String tableAlias = null;
			String operateType;
			String propertyName;
			if(arr.length ==3) {
				operateType = arr[1];
				propertyName = arr[2];
			} else if(arr.length == 4) {
				tableAlias = arr[1];
				operateType = arr[2];
				propertyName = arr[3];
			} else {
				return;
			}
			if(!"m".equals(arr[0])) {
				return;
			}
			OperateTypeEnum ops [] = OperateTypeEnum.values();
			// 默认等值
			OperateTypeEnum operateTypeEnum = OperateTypeEnum.EQ;
			for(OperateTypeEnum item : ops) {
				if(item.name().equals(operateType.toUpperCase())) {
					operateTypeEnum = item;
					break;
				}
			}
			if(!StringTool.checkColumn(propertyName) || (StringTool.isNotEmpty(tableAlias) && !StringTool.checkColumn(tableAlias))) {
				return;
			}
			Object value = null;
			//获取属性值
			value = v;
			if(value != null) {
				if(value instanceof String) {
					if (StringTool.isNotEmpty(value.toString())) {
						WhereParam whereParam = new WhereParam();
						whereParam.setTableAlias(tableAlias);
						whereParam.setOperateType(operateTypeEnum);
						whereParam.setPropertyName(propertyName);
						if (operateTypeEnum.equals(OperateTypeEnum.IN) || operateTypeEnum.equals(OperateTypeEnum.NIN)) {
							String values[] = value.toString().split(",");
							whereParam.setPropertyValue(Arrays.asList(values));
						} else if (operateTypeEnum.equals(OperateTypeEnum.BT) || operateTypeEnum.equals(OperateTypeEnum.NBT)) {
							String values[] = value.toString().split(",");
							if (values.length != 2) {
								throw new BizException(GlobalErrEnum.GL99990100);
							}
							whereParam.setPropertyValue(Arrays.asList(values));
						} else {
							whereParam.setPropertyValue(value);
						}
						res.add(whereParam);
					}
				} else {
					WhereParam whereParam = new WhereParam();
					whereParam.setTableAlias(tableAlias);
					whereParam.setOperateType(operateTypeEnum);
					whereParam.setPropertyName(propertyName);
					whereParam.setPropertyValue(value);
					res.add(whereParam);
				}
			}
		});
		return res;
	}
	/**
	 * 将属性值转成whereParams对象
	 * @param pageParam
	 * @return
	 */
	public static List<WhereParam> propertyConvertWhereParams(PageParam pageParam) {
		List<WhereParam> res = new ArrayList<>();
		Field fields [] = pageParam.getClass().getDeclaredFields();
		for(Field field: fields) {
			String [] arr = field.getName().split("_");
			String tableAlias = null;
			String operateType;
			String propertyName;
			if(arr.length ==3) {
				operateType = arr[1];
				propertyName = arr[2];
			} else if(arr.length == 4) {
				tableAlias = arr[1];
				operateType = arr[2];
				propertyName = arr[3];
			} else {
				continue;
			}
			if(!"m".equals(arr[0])) {
				return res;
			}
			OperateTypeEnum ops [] = OperateTypeEnum.values();
			// 默认等值
			OperateTypeEnum operateTypeEnum = OperateTypeEnum.EQ;
			for(OperateTypeEnum item : ops) {
				if(item.name().equals(operateType.toUpperCase())) {
					operateTypeEnum = item;
					break;
				}
			}
			//打开私有访问
			field.setAccessible(true);
			//获取属性
			String name = field.getName();
			Object value = null;
			//获取属性值
			try {
				value = field.get(pageParam);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			if(value != null) {
				if(value instanceof String) {
					if (StringTool.isNotEmpty(value.toString())) {
						WhereParam whereParam = new WhereParam();
						whereParam.setTableAlias(tableAlias);
						whereParam.setOperateType(operateTypeEnum);
						whereParam.setPropertyName(propertyName);
						if (operateTypeEnum.equals(OperateTypeEnum.IN) || operateTypeEnum.equals(OperateTypeEnum.NIN)) {
							String values[] = value.toString().split(",");
							whereParam.setPropertyValue(Arrays.asList(values));
						} else if (operateTypeEnum.equals(OperateTypeEnum.BT) || operateTypeEnum.equals(OperateTypeEnum.NBT)) {
							String values[] = value.toString().split(",");
							if (values.length != 2) {
								throw new BizException(GlobalErrEnum.GL99990100);
							}
							whereParam.setPropertyValue(Arrays.asList(values));
						} else {
							whereParam.setPropertyValue(value);
						}
						res.add(whereParam);
					}
				} else {
					WhereParam whereParam = new WhereParam();
					whereParam.setTableAlias(tableAlias);
					whereParam.setOperateType(operateTypeEnum);
					whereParam.setPropertyName(propertyName);
					whereParam.setPropertyValue(value);
					res.add(whereParam);
				}
			}
		}
		pageParam.setWhereParams(res);
		return res;
	}
}
