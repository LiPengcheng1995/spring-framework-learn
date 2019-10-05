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

package org.springframework.web.servlet.mvc.condition;

import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;

/**
 * Contract for request mapping conditions.
 *
 * <p>Request conditions can be combined via {@link #combine(Object)}, matched to
 * a request via {@link #getMatchingCondition(HttpServletRequest)}, and compared
 * to each other via {@link #compareTo(Object, HttpServletRequest)} to determine
 * which is a closer match for a given request.
 *
 * @param <T> the type of objects that this RequestCondition can be combined
 *            with and compared to
 * @author Rossen Stoyanchev
 * @author Arjen Poutsma
 * @since 3.1
 */
//TODO  这个概念模型有点像 Java8 的流操作、函数式编程那种感觉。具体叫啥一下子想不出来
public interface RequestCondition<T> {

	/**
	 * Combine this condition with another such as conditions from a
	 * type-level and method-level {@code @RequestMapping} annotation.
	 *
	 * @param other the condition to combine with.
	 * @return a request condition instance that is the result of combining
	 * the two condition instances.
	 */
	// 和另外一个请求匹配条件合并，具体合并逻辑由实现类提供
	T combine(T other);

	/**
	 * Check if the condition matches the request returning a potentially new
	 * instance created for the current request. For example a condition with
	 * multiple URL patterns may return a new instance only with those patterns
	 * that match the request.
	 * <p>For CORS pre-flight requests, conditions should match to the would-be,
	 * actual request (e.g. URL pattern, query parameters, and the HTTP method
	 * from the "Access-Control-Request-Method" header). If a condition cannot
	 * be matched to a pre-flight request it should return an instance with
	 * empty content thus not causing a failure to match.
	 *
	 * @return a condition instance in case of a match or {@code null} otherwise.
	 */
	// 检查当前请求匹配条件和指定请求request是否匹配，如果不匹配返回null，
	// 如果匹配，生成一个新的请求匹配条件，该新的请求匹配条件是当前请求匹配条件
	// 针对指定请求request的剪裁。
	// 举个例子来讲，如果当前请求匹配条件是一个路径匹配条件，包含多个路径匹配模板，
	// 并且其中有些模板和指定请求request匹配，那么返回的新建的请求匹配条件将仅仅
	// 包含和指定请求request匹配的那些路径模板。
	@Nullable
	T getMatchingCondition(HttpServletRequest request);

	/**
	 * Compare this condition to another condition in the context of
	 * a specific request. This method assumes both instances have
	 * been obtained via {@link #getMatchingCondition(HttpServletRequest)}
	 * to ensure they have content relevant to current request only.
	 */
	// 针对指定的请求对象request比较两个请求匹配条件。
	// 该方法假定被比较的两个请求匹配条件都是针对该请求对象request调用了
	// #getMatchingCondition方法得到的，这样才能确保对它们的比较
	// 是针对同一个请求对象request，这样的比较才有意义(最终用来确定谁是
	// 更匹配的条件)。
	int compareTo(T other, HttpServletRequest request);

}
