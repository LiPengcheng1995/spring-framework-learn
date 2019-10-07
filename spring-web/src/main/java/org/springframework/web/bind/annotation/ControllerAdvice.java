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

package org.springframework.web.bind.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Specialization of {@link Component @Component} for classes that declare
 * {@link ExceptionHandler @ExceptionHandler}, {@link InitBinder @InitBinder}, or
 * {@link ModelAttribute @ModelAttribute} methods to be shared across
 * multiple {@code @Controller} classes.
 *
 * <p>Classes with {@code @ControllerAdvice} can be declared explicitly as Spring
 * beans or auto-detected via classpath scanning. All such beans are sorted via
 * {@link org.springframework.core.annotation.AnnotationAwareOrderComparator
 * AnnotationAwareOrderComparator}, i.e. based on
 * {@link org.springframework.core.annotation.Order @Order} and
 * {@link org.springframework.core.Ordered Ordered}, and applied in that order
 * at runtime. For handling exceptions, an {@code @ExceptionHandler} will be
 * picked on the first advice with a matching exception handler method. For
 * model attributes and {@code InitBinder} initialization, {@code @ModelAttribute}
 * and {@code @InitBinder} methods will also follow {@code @ControllerAdvice} order.
 *
 * <p>Note: For {@code @ExceptionHandler} methods, a root exception match will be
 * preferred to just matching a cause of the current exception, among the handler
 * methods of a particular advice bean. However, a cause match on a higher-priority
 * advice will still be preferred to a any match (whether root or cause level)
 * on a lower-priority advice bean. As a consequence, please declare your primary
 * root exception mappings on a prioritized advice bean with a corresponding order!
 *
 * <p>By default the methods in an {@code @ControllerAdvice} apply globally to
 * all Controllers. Use selectors {@link #annotations()},
 * {@link #basePackageClasses()}, and {@link #basePackages()} (or its alias
 * {@link #value()}) to define a more narrow subset of targeted Controllers.
 * If multiple selectors are declared, OR logic is applied, meaning selected
 * Controllers should match at least one selector. Note that selector checks
 * are performed at runtime and so adding many selectors may negatively impact
 * performance and add complexity.
 *
 * @author Rossen Stoyanchev
 * @author Brian Clozel
 * @author Sam Brannen
 * @see org.springframework.stereotype.Controller
 * @see RestControllerAdvice
 * @since 3.2
 */
// 控制器增强，主要和 ExceptionHandler、InitBinder、ModelAttribute 三个注解配合使用
// 可以对比我们学习 AOP 时接触的 Advice ，只不过这个是专门针对  Controller 的。
// 这个用来找切点，剩下的三个标签用来构建切面
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ControllerAdvice {

	/**
	 * Alias for the {@link #basePackages} attribute.
	 * <p>Allows for more concise annotation declarations e.g.:
	 * {@code @ControllerAdvice("org.my.pkg")} is equivalent to
	 * {@code @ControllerAdvice(basePackages="org.my.pkg")}.
	 *
	 * @see #basePackages()
	 * @since 4.0
	 */
	@AliasFor("basePackages")
	String[] value() default {};

	/**
	 * Array of base packages.
	 * <p>Controllers that belong to those base packages or sub-packages thereof
	 * will be included, e.g.: {@code @ControllerAdvice(basePackages="org.my.pkg")}
	 * or {@code @ControllerAdvice(basePackages={"org.my.pkg", "org.my.other.pkg"})}.
	 * <p>{@link #value} is an alias for this attribute, simply allowing for
	 * more concise use of the annotation.
	 * <p>Also consider using {@link #basePackageClasses()} as a type-safe
	 * alternative to String-based package names.
	 *
	 * @since 4.0
	 */
	@AliasFor("value")
	String[] basePackages() default {};

	/**
	 * Type-safe alternative to {@link #value()} for specifying the packages
	 * to select Controllers to be assisted by the {@code @ControllerAdvice}
	 * annotated class.
	 * <p>Consider creating a special no-op marker class or interface in each package
	 * that serves no purpose other than being referenced by this attribute.
	 *
	 * @since 4.0
	 */
	Class<?>[] basePackageClasses() default {};

	/**
	 * Array of classes.
	 * <p>Controllers that are assignable to at least one of the given types
	 * will be assisted by the {@code @ControllerAdvice} annotated class.
	 *
	 * @since 4.0
	 */
	Class<?>[] assignableTypes() default {};

	/**
	 * Array of annotations.
	 * <p>Controllers that are annotated with this/one of those annotation(s)
	 * will be assisted by the {@code @ControllerAdvice} annotated class.
	 * <p>Consider creating a special annotation or use a predefined one,
	 * like {@link RestController @RestController}.
	 *
	 * @since 4.0
	 */
	Class<? extends Annotation>[] annotations() default {};

}
