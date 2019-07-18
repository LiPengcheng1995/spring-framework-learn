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

package org.springframework.aop.framework;

import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

import java.io.Closeable;

/**
 * Base class with common functionality for proxy processors, in particular
 * ClassLoader management and the {@link #evaluateProxyInterfaces} algorithm.
 *
 * @author Juergen Hoeller
 * @see AbstractAdvisingBeanPostProcessor
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator
 * @since 4.1
 */
@SuppressWarnings("serial")
public class ProxyProcessorSupport extends ProxyConfig implements Ordered, BeanClassLoaderAware, AopInfrastructureBean {

	/**
	 * This should run after all other processors, so that it can just add
	 * an advisor to existing proxies rather than double-proxy.
	 */
	// 在完成对代理对象的所有包装后，再包一层用户定义的切面逻辑
	private int order = Ordered.LOWEST_PRECEDENCE;

	@Nullable
	// DefaultClassLoader 是兜底的，默认的应该是通过 aware 丢进来的
	private ClassLoader proxyClassLoader = ClassUtils.getDefaultClassLoader();

	private boolean classLoaderConfigured = false;

	@Override
	public int getOrder() {
		return this.order;
	}

	/**
	 * Set the ordering which will apply to this processor's implementation
	 * of {@link Ordered}, used when applying multiple processors.
	 * <p>The default value is {@code Ordered.LOWEST_PRECEDENCE}, meaning non-ordered.
	 *
	 * @param order the ordering value
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * Return the configured proxy ClassLoader for this processor.
	 */
	@Nullable
	protected ClassLoader getProxyClassLoader() {
		return this.proxyClassLoader;
	}

	/**
	 * Set the ClassLoader to generate the proxy class in.
	 * <p>Default is the bean ClassLoader, i.e. the ClassLoader used by the containing
	 * {@link org.springframework.beans.factory.BeanFactory} for loading all bean classes.
	 * This can be overridden here for specific proxies.
	 */
	public void setProxyClassLoader(@Nullable ClassLoader classLoader) {
		this.proxyClassLoader = classLoader;
		this.classLoaderConfigured = (classLoader != null);
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		if (!this.classLoaderConfigured) {
			// 注意，这里没有设置 classLoaderConfigured ，说明默认的、在没设置的情况下，用的是 aware 丢进来的
			this.proxyClassLoader = classLoader;
		}
	}


	/**
	 * Check the interfaces on the given bean class and apply them to the {@link ProxyFactory},
	 * if appropriate.
	 * <p>Calls {@link #isConfigurationCallbackInterface} and {@link #isInternalLanguageInterface}
	 * to filter for reasonable proxy interfaces, falling back to a target-class proxy otherwise.
	 *
	 * @param beanClass    the class of the bean
	 * @param proxyFactory the ProxyFactory for the bean
	 */
	protected void evaluateProxyInterfaces(Class<?> beanClass, ProxyFactory proxyFactory) {
		// 得到此类实现的所有接口
		Class<?>[] targetInterfaces = ClassUtils.getAllInterfacesForClass(beanClass, getProxyClassLoader());
		boolean hasReasonableProxyInterface = false;
		// 筛选一下接口，看有没有可以用来代理的，如果有就可以考虑 JDK 动态代理了。如果没有只能走 CGLIB
		for (Class<?> ifc : targetInterfaces) {

			// 1. 不能是那些 Spring 容器回调的接口
			// 2. 不能是那些本地链接库的
			// 3. 接口不能是空的
			if (!isConfigurationCallbackInterface(ifc) && !isInternalLanguageInterface(ifc) &&
					ifc.getMethods().length > 0) {
				hasReasonableProxyInterface = true;
				break;
			}
		}
		// 如果有符合条件的，就都存起来
		// TODO 这里没有专门筛选那些，看看后面根据存的接口使用时有没有过滤
		// TODO 感觉这里做的不是特别好
		if (hasReasonableProxyInterface) {
			// Must allow for introductions; can't just set interfaces to the target's interfaces only.
			for (Class<?> ifc : targetInterfaces) {
				proxyFactory.addInterface(ifc);
			}
		} else {
			// 没有符合条件的接口，直接设置用 CGLIB 生成代理
			proxyFactory.setProxyTargetClass(true);
		}
	}

	/**
	 * Determine whether the given interface is just a container callback and
	 * therefore not to be considered as a reasonable proxy interface.
	 * <p>If no reasonable proxy interface is found for a given bean, it will get
	 * proxied with its full target class, assuming that as the user's intention.
	 *
	 * @param ifc the interface to check
	 * @return whether the given interface is just a container callback
	 */
	// 判断这个接口是不是那种不能代理的，这些接口后面需要容器进行回调
	protected boolean isConfigurationCallbackInterface(Class<?> ifc) {
		return (InitializingBean.class == ifc || DisposableBean.class == ifc || Closeable.class == ifc ||
				AutoCloseable.class == ifc || ObjectUtils.containsElement(ifc.getInterfaces(), Aware.class));
	}

	/**
	 * Determine whether the given interface is a well-known internal language interface
	 * and therefore not to be considered as a reasonable proxy interface.
	 * <p>If no reasonable proxy interface is found for a given bean, it will get
	 * proxied with its full target class, assuming that as the user's intention.
	 *
	 * @param ifc the interface to check
	 * @return whether the given interface is an internal language interface
	 */
	// 判断这种接口是不是那些已知的内部语言接口【就是非普通的接口。。。。本地链接库那种吧】
	protected boolean isInternalLanguageInterface(Class<?> ifc) {
		return (ifc.getName().equals("groovy.lang.GroovyObject") ||
				ifc.getName().endsWith(".cglib.proxy.Factory") ||
				ifc.getName().endsWith(".bytebuddy.MockAccess"));
	}

}
