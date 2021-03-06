package com.mldong.common.web;

import com.mldong.common.base.PageParam;
import com.mldong.common.logger.LoggerModel;
import com.mldong.common.tk.ConditionUtil;
import com.mldong.common.tool.JsonTool;
import org.apache.commons.io.IOUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
/**
 * 全局的请求处理，可以在这里对原始的参数进行解密，或者请求参数日志记录
 * @author mldong
 *
 */
@ControllerAdvice
public class GlobalRequestBodyAdvice implements RequestBodyAdvice{
	private final static String charset = "UTF-8";
	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		return converterType.isAssignableFrom(MappingJackson2HttpMessageConverter.class);
	}
 
	@Override
	public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
			Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
		return body;
	}
 
	@Override
	public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
		LoggerModel loggerModel = RequestHolder.getLoggerModel();
		if(null != loggerModel) {
			String body = IOUtils.toString(inputMessage.getBody(), charset);
			// 设置请求正文 这里拿到的是InputStream的内容
			loggerModel.setBody(body);
			// InputStream只能读一次，这里读了，得重新返回一个新的
			return new HttpInputMessage() {
				@Override
				public HttpHeaders getHeaders() {
					return inputMessage.getHeaders();
				}
				
				@Override
				public InputStream getBody() throws IOException {
					return new ByteArrayInputStream(body.getBytes());
				}
			};
		}
		return inputMessage;
	}
 
	@Override
	public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		if(body != null) {
			if(body instanceof PageParam) {
				PageParam pageParam = (PageParam)body ;
				LoggerModel loggerModel = RequestHolder.getLoggerModel();
				if(pageParam.getWhereParams() == null || pageParam.getWhereParams().isEmpty()) {
					pageParam.setWhereParams(ConditionUtil.propertyConvertWhereParams(JsonTool.jsonToMap(loggerModel.getBody())));
				}
//				if(pageParam.getWhereParams() == null || pageParam.getWhereParams().isEmpty()) {
//					ConditionUtil.propertyConvertWhereParams((PageParam)pageParam);
//				} else {
//					// 这里要过滤 表别名与字段属性
//					Iterator<WhereParam> iterator = pageParam.getWhereParams().iterator();
//					while (iterator.hasNext()) {
//						WhereParam param = iterator.next();
//						if(!StringTool.checkColumn(param.getPropertyName())) {
//							iterator.remove();
//							continue;
//						}
//						if(StringTool.isNotEmpty(param.getTableAlias())) {
//							if(!StringTool.checkColumn(param.getTableAlias())) {
//								iterator.remove();
//								continue;
//							}
//						}
//					}
//				}
			}
		}
		return body;
	}

}
