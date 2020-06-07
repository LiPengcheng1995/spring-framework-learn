/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.remoting.rmi;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.Assert;

/**
 * {@link FactoryBean} for RMI proxies, supporting both conventional RMI services
 * and RMI invokers. Exposes the proxied service for use as a bean reference,
 * using the specified service interface. Proxies will throw Spring's unchecked
 * RemoteAccessException on remote invocation failure instead of RMI's RemoteException.
 *
 * <p>The service URL must be a valid RMI URL like "rmi://localhost:1099/myservice".
 * RMI invokers work at the RmiInvocationHandler level, using the same invoker stub
 * for any service. Service interfaces do not have to extend {@code java.rmi.Remote}
 * or throw {@code java.rmi.RemoteException}. Of course, in and out parameters
 * have to be serializable.
 *
 * <p>With conventional RMI services, this proxy factory is typically used with the
 * RMI service interface. Alternatively, this factory can also proxy a remote RMI
 * service with a matching non-RMI business interface, i.e. an interface that mirrors
 * the RMI service methods but does not declare RemoteExceptions. In the latter case,
 * RemoteExceptions thrown by the RMI stub will automatically get converted to
 * Spring's unchecked RemoteAccessException.
 *
 * <p>The major advantage of RMI, compared to Hessian, is serialization.
 * Effectively, any serializable Java object can be transported without hassle.
 * Hessian has its own (de-)serialization mechanisms, but is HTTP-based and thus
 * much easier to setup than RMI. Alternatively, consider Spring's HTTP invoker
 * to combine Java serialization with HTTP-based transport.
 *
 * @author Juergen Hoeller
 * @see #setServiceInterface
 * @see #setServiceUrl
 * @see RmiClientInterceptor
 * @see RmiServiceExporter
 * @see java.rmi.Remote
 * @see java.rmi.RemoteException
 * @see org.springframework.remoting.RemoteAccessException
 * @see org.springframework.remoting.caucho.HessianProxyFactoryBean
 * @see org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean
 * @since 13.05.2003
 */
// 这个类是 RMI consumer 用来提供服务提供者实例的。
// 这里提供的实例，可以和 RMI 那些必须实现的接口/类说拜拜，因为 Spring 会帮助我们进行封装，将对应的接口实现
// 并将那些必须捕获的声明式异常转化成 Spring 的运行时异常。
//
// RMI 相对于 Hessian 的优点是 Java 的序列化，【可以双向带值、可以带 Class】，可以用 Http 但是可能会慢
// 但是 Hessian 也有起优点，它用 http ，更容易构建。
public class RmiProxyFactoryBean extends RmiClientInterceptor implements FactoryBean<Object>, BeanClassLoaderAware {

	private Object serviceProxy;


	// 这里是：
	// 1. 先调用父类的钩子，把所有东西弄好
	// 2. 创建一层增强，把自己当作一个拦截器丢进去
	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		Class<?> ifc = getServiceInterface();
		Assert.notNull(ifc, "Property 'serviceInterface' is required");
		// TODO 这里很重要 ，这里 ProxyFactory 里面只加入了本拦截器，这层代理的拦截器调用链只有本类
		// TODO 然后本类在 invoke 里面实现了远程对象的调用【更直白一些，这里的代理并不像我们之前理解的 AOP 那样有个被代理的对象】
		//
		// TODO 为什么要在拦截器中实现这个呢？
		// 因为这是一个通用方法封装，用反射的那种（兼容 Spring 那个 RmiInvocationHandler ），所以这里必须得通用化，只能用反射进行调用
		// 传递。如果你把本类设置成 TargetSource ，就没法做通用化编码了。
		//
		// TODO 这个代理好怪
		// 理论上，真正的代理，或者说增强，只是创建出一个糅合了 目标实例功能、拦截器功能 的新实例。具体要糅什么，根据需求而定。
		//
		// TODO 来认识一个新的工具类 ProxyFactory ，后面有需要，可以自己塞拦截器，定制代理
		this.serviceProxy = new ProxyFactory(ifc, this).getProxy(getBeanClassLoader());
	}


	@Override
	public Object getObject() {
		return this.serviceProxy;
	}

	@Override
	public Class<?> getObjectType() {
		return getServiceInterface();
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
