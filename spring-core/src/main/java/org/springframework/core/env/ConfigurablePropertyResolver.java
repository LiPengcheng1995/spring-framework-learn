/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.core.env;

import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.lang.Nullable;

/**
 * Configuration interface to be implemented by most if not all {@link PropertyResolver}
 * types. Provides facilities for accessing and customizing the
 * {@link org.springframework.core.convert.ConversionService ConversionService}
 * used when converting property values from one type to another.
 *
 * @author Chris Beams
 * @since 3.1
 */
/*
 这个接口在父类的基础上提供了更多的功能，主要是两个方面：
 1. 对父类功能的增强，主要体现在支持功能的定制化、配置化上
	 我们说了 PropertyResolver 主要有两个核心功能：
	 1. 拿到属性
	 2. 类型转换

	 此类提供的功能配置主要如下：
	 1. 类型转换
	 		a). 类型转换类的存取【定制类型转换策略】
	 2. 拿到属性
			a). String 属性替换时允许定制占位符、分隔符【定制变量替换策略】
 2. 新增功能
 	此类新提供了"必要参数验证"功能，可以指定必须定义的参数，并调用验证方法看是否符合要求。
*/

public interface ConfigurablePropertyResolver extends PropertyResolver {

	/**
	 * Return the {@link ConfigurableConversionService} used when performing type
	 * conversions on properties.
	 * <p>The configurable nature of the returned conversion service allows for
	 * the convenient addition and removal of individual {@code Converter} instances:
	 * <pre class="code">
	 * ConfigurableConversionService cs = env.getConversionService();
	 * cs.addConverter(new FooConverter());
	 * </pre>
	 *
	 * @see PropertyResolver#getProperty(String, Class)
	 * @see org.springframework.core.convert.converter.ConverterRegistry#addConverter
	 */
	ConfigurableConversionService getConversionService();

	/**
	 * Set the {@link ConfigurableConversionService} to be used when performing type
	 * conversions on properties.
	 * <p><strong>Note:</strong> as an alternative to fully replacing the
	 * {@code ConversionService}, consider adding or removing individual
	 * {@code Converter} instances by drilling into {@link #getConversionService()}
	 * and calling methods such as {@code #addConverter}.
	 *
	 * @see PropertyResolver#getProperty(String, Class)
	 * @see #getConversionService()
	 * @see org.springframework.core.convert.converter.ConverterRegistry#addConverter
	 */
	void setConversionService(ConfigurableConversionService conversionService);

	/**
	 * Set the prefix that placeholders replaced by this resolver must begin with.
	 */
	void setPlaceholderPrefix(String placeholderPrefix);

	/**
	 * Set the suffix that placeholders replaced by this resolver must end with.
	 */
	void setPlaceholderSuffix(String placeholderSuffix);

	/**
	 * Specify the separating character between the placeholders replaced by this
	 * resolver and their associated default value, or {@code null} if no such
	 * special character should be processed as a value separator.
	 */
	void setValueSeparator(@Nullable String valueSeparator);

	/**
	 * Set whether to throw an exception when encountering an unresolvable placeholder
	 * nested within the value of a given property. A {@code false} value indicates strict
	 * resolution, i.e. that an exception will be thrown. A {@code true} value indicates
	 * that unresolvable nested placeholders should be passed through in their unresolved
	 * ${...} form.
	 * <p>Implementations of {@link #getProperty(String)} and its variants must inspect
	 * the value set here to determine correct behavior when property values contain
	 * unresolvable placeholders.
	 *
	 * @since 3.2
	 */
	void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders);

	/**
	 * Specify which properties must be present, to be verified by
	 * {@link #validateRequiredProperties()}.
	 */
	void setRequiredProperties(String... requiredProperties);

	/**
	 * Validate that each of the properties specified by
	 * {@link #setRequiredProperties} is present and resolves to a
	 * non-{@code null} value.
	 *
	 * @throws MissingRequiredPropertiesException if any of the required
	 *                                            properties are not resolvable.
	 */
	void validateRequiredProperties() throws MissingRequiredPropertiesException;

}
