/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.reactive.result.method.annotation;

import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringValueResolver;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.accept.RequestedContentTypeResolverBuilder;
import org.springframework.web.reactive.result.condition.RequestCondition;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.RequestMappingInfoHandlerMapping;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * An extension of {@link RequestMappingInfoHandlerMapping} that creates
 * {@link RequestMappingInfo} instances from class-level and method-level
 * {@link RequestMapping @RequestMapping} annotations.
 *
 * @author Rossen Stoyanchev
 * @since 5.0
 */
public class RequestMappingHandlerMapping extends RequestMappingInfoHandlerMapping
		implements EmbeddedValueResolverAware {

	private RequestedContentTypeResolver contentTypeResolver = new RequestedContentTypeResolverBuilder().build();

	@Nullable
	private StringValueResolver embeddedValueResolver;

	private RequestMappingInfo.BuilderConfiguration config = new RequestMappingInfo.BuilderConfiguration();

	/**
	 * Return the configured {@link RequestedContentTypeResolver}.
	 */
	public RequestedContentTypeResolver getContentTypeResolver() {
		return this.contentTypeResolver;
	}

	/**
	 * Set the {@link RequestedContentTypeResolver} to use to determine requested
	 * media types. If not set, the default constructor is used.
	 */
	public void setContentTypeResolver(RequestedContentTypeResolver contentTypeResolver) {
		Assert.notNull(contentTypeResolver, "'contentTypeResolver' must not be null");
		this.contentTypeResolver = contentTypeResolver;
	}

	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		this.embeddedValueResolver = resolver;
	}

	@Override
	public void afterPropertiesSet() {
		this.config = new RequestMappingInfo.BuilderConfiguration();
		this.config.setPatternParser(getPathPatternParser());
		this.config.setContentTypeResolver(getContentTypeResolver());

		super.afterPropertiesSet();
	}


	/**
	 * {@inheritDoc}
	 * Expects a handler to have a type-level @{@link Controller} annotation.
	 */
	// 看类上是否有 Controller/RequestMapping 注解
	@Override
	protected boolean isHandler(Class<?> beanType) {
		return (AnnotatedElementUtils.hasAnnotation(beanType, Controller.class) ||
				AnnotatedElementUtils.hasAnnotation(beanType, RequestMapping.class));
	}

	/**
	 * Uses method and type-level @{@link RequestMapping} annotations to create
	 * the RequestMappingInfo.
	 *
	 * @return the created RequestMappingInfo, or {@code null} if the method
	 * does not have a {@code @RequestMapping} annotation.
	 * @see #getCustomMethodCondition(Method)
	 * @see #getCustomTypeCondition(Class)
	 */
	@Override
	protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
		// 创建个 Method 的
		RequestMappingInfo info = createRequestMappingInfo(method);
		if (info != null) {
			// 创建个 Class 的
			RequestMappingInfo typeInfo = createRequestMappingInfo(handlerType);
			if (typeInfo != null) {
				info = typeInfo.combine(info);// 条件 &&
			}
		}
		// 返回
		return info;
	}

	/**
	 * Delegates to {@link #createRequestMappingInfo(RequestMapping, RequestCondition)},
	 * supplying the appropriate custom {@link RequestCondition} depending on whether
	 * the supplied {@code annotatedElement} is a class or method.
	 *
	 * @see #getCustomTypeCondition(Class)
	 * @see #getCustomMethodCondition(Method)
	 */
	@Nullable
	private RequestMappingInfo createRequestMappingInfo(AnnotatedElement element) {
		// 拿到 RequestMapping 打标
		RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
		// 两个空实现，分别是针对类/方法的，方便子类扩展
		RequestCondition<?> condition = (element instanceof Class ?
				getCustomTypeCondition((Class<?>) element) : getCustomMethodCondition((Method) element));
		return (requestMapping != null ? createRequestMappingInfo(requestMapping, condition) : null);
	}

	/**
	 * Provide a custom type-level request condition.
	 * The custom {@link RequestCondition} can be of any type so long as the
	 * same condition type is returned from all calls to this method in order
	 * to ensure custom request conditions can be combined and compared.
	 * <p>Consider extending
	 * {@link org.springframework.web.reactive.result.condition.AbstractRequestCondition
	 * AbstractRequestCondition} for custom condition types and using
	 * {@link org.springframework.web.reactive.result.condition.CompositeRequestCondition
	 * CompositeRequestCondition} to provide multiple custom conditions.
	 *
	 * @param handlerType the handler type for which to create the condition
	 * @return the condition, or {@code null}
	 */
	@SuppressWarnings("UnusedParameters")
	@Nullable
	protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
		return null;
	}

	/**
	 * Provide a custom method-level request condition.
	 * The custom {@link RequestCondition} can be of any type so long as the
	 * same condition type is returned from all calls to this method in order
	 * to ensure custom request conditions can be combined and compared.
	 * <p>Consider extending
	 * {@link org.springframework.web.reactive.result.condition.AbstractRequestCondition
	 * AbstractRequestCondition} for custom condition types and using
	 * {@link org.springframework.web.reactive.result.condition.CompositeRequestCondition
	 * CompositeRequestCondition} to provide multiple custom conditions.
	 *
	 * @param method the handler method for which to create the condition
	 * @return the condition, or {@code null}
	 */
	@SuppressWarnings("UnusedParameters")
	@Nullable
	protected RequestCondition<?> getCustomMethodCondition(Method method) {
		return null;
	}

	/**
	 * Create a {@link RequestMappingInfo} from the supplied
	 * {@link RequestMapping @RequestMapping} annotation, which is either
	 * a directly declared annotation, a meta-annotation, or the synthesized
	 * result of merging annotation attributes within an annotation hierarchy.
	 */
	protected RequestMappingInfo createRequestMappingInfo(
			RequestMapping requestMapping, @Nullable RequestCondition<?> customCondition) {

		RequestMappingInfo.Builder builder = RequestMappingInfo
				// 设置注解中的 path 限制（先把 Spring 变量替换掉然后再设置进来）
				.paths(resolveEmbeddedValuesInPatterns(requestMapping.path()))
				// 设置注解中的 http 方法名称限制
				.methods(requestMapping.method())
				// 设置注解中配置的参数限制（参数值设置/参数是否必传/参数是否必不传）
				.params(requestMapping.params())
				// 设置注解中配置的 header 限制
				.headers(requestMapping.headers())
				// 设置注解中配置的可消费请求类型限制
				.consumes(requestMapping.consumes())
				// 设置注解中配置的返回类型限制
				.produces(requestMapping.produces())
				// 设个名字呗
				.mappingName(requestMapping.name());
		if (customCondition != null) {// 这里一般都是 null，先不管
			builder.customCondition(customCondition);
		}
		// TODO 看看 RequestMappingInfo 的相关业务逻辑吧
		return builder.options(this.config).build();
	}

	/**
	 * Resolve placeholder values in the given array of patterns.
	 *
	 * @return a new array with updated patterns
	 */
	protected String[] resolveEmbeddedValuesInPatterns(String[] patterns) {
		if (this.embeddedValueResolver == null) {
			return patterns;
		} else {
			String[] resolvedPatterns = new String[patterns.length];
			for (int i = 0; i < patterns.length; i++) {
				resolvedPatterns[i] = this.embeddedValueResolver.resolveStringValue(patterns[i]);
			}
			return resolvedPatterns;
		}
	}

	@Override
	protected CorsConfiguration initCorsConfiguration(Object handler, Method method, RequestMappingInfo mappingInfo) {
		HandlerMethod handlerMethod = createHandlerMethod(handler, method);
		Class<?> beanType = handlerMethod.getBeanType();
		CrossOrigin typeAnnotation = AnnotatedElementUtils.findMergedAnnotation(beanType, CrossOrigin.class);
		CrossOrigin methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, CrossOrigin.class);

		if (typeAnnotation == null && methodAnnotation == null) {
			return null;
		}

		CorsConfiguration config = new CorsConfiguration();
		updateCorsConfig(config, typeAnnotation);
		updateCorsConfig(config, methodAnnotation);

		if (CollectionUtils.isEmpty(config.getAllowedMethods())) {
			for (RequestMethod allowedMethod : mappingInfo.getMethodsCondition().getMethods()) {
				config.addAllowedMethod(allowedMethod.name());
			}
		}
		return config.applyPermitDefaultValues();
	}

	private void updateCorsConfig(CorsConfiguration config, @Nullable CrossOrigin annotation) {
		if (annotation == null) {
			return;
		}
		for (String origin : annotation.origins()) {
			config.addAllowedOrigin(resolveCorsAnnotationValue(origin));
		}
		for (RequestMethod method : annotation.methods()) {
			config.addAllowedMethod(method.name());
		}
		for (String header : annotation.allowedHeaders()) {
			config.addAllowedHeader(resolveCorsAnnotationValue(header));
		}
		for (String header : annotation.exposedHeaders()) {
			config.addExposedHeader(resolveCorsAnnotationValue(header));
		}

		String allowCredentials = resolveCorsAnnotationValue(annotation.allowCredentials());
		if ("true".equalsIgnoreCase(allowCredentials)) {
			config.setAllowCredentials(true);
		} else if ("false".equalsIgnoreCase(allowCredentials)) {
			config.setAllowCredentials(false);
		} else if (!allowCredentials.isEmpty()) {
			throw new IllegalStateException("@CrossOrigin's allowCredentials value must be \"true\", \"false\", " +
					"or an empty string (\"\"): current value is [" + allowCredentials + "]");
		}

		if (annotation.maxAge() >= 0 && config.getMaxAge() == null) {
			config.setMaxAge(annotation.maxAge());
		}
	}

	private String resolveCorsAnnotationValue(String value) {
		if (this.embeddedValueResolver != null) {
			String resolved = this.embeddedValueResolver.resolveStringValue(value);
			return (resolved != null ? resolved : "");
		} else {
			return value;
		}
	}

}
