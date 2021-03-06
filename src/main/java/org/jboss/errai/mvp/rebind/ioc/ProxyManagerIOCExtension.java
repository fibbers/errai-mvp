/*
 * Copyright 2012 Cedric Hauber
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.jboss.errai.mvp.rebind.ioc;


import org.jboss.errai.codegen.BlockStatement;
import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.builder.ClassDefinitionBuilderAbstractOption;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.ioc.client.api.IOCExtension;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessorFactory;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCExtensionConfigurator;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.mvp.client.annotations.*;
import org.jboss.errai.mvp.client.events.NotifyingAsyncCallback;
import org.jboss.errai.mvp.client.places.Gatekeeper;
import org.jboss.errai.mvp.client.proxy.ProxyImpl;
import org.jboss.errai.mvp.client.proxy.ProxyManager;

import java.util.Collection;

import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;

@SuppressWarnings("UnusedDeclaration")
@IOCExtension
public class ProxyManagerIOCExtension implements IOCExtensionConfigurator {

    public ProxyManagerIOCExtension() {
    }

    @Override
    public void configure(IOCProcessingContext context, InjectionContext injectionContext, IOCProcessorFactory procFactory) {
    }

    @Override
    public void afterInitialization(IOCProcessingContext context, InjectionContext injectionContext, IOCProcessorFactory procFactory) {
        final BlockStatement instanceInitializer = context.getBootstrapClass().getInstanceInitializer();
        for (MetaClass klass : ClassScanner.getTypesAnnotatedWith(ProxyClass.class)) {
            ClassDefinitionBuilderAbstractOption<? extends ClassStructureBuilder<?>> proxy = createProxy(klass);
            for (MetaMethod method : klass.getMethodsAnnotatedWith(ProxyEvent.class)) {
                MetaParameter event = method.getParameters()[0];
                createMethod(injectionContext, getHandler(klass, method.getName(), event.getType()), klass, proxy, method.getReturnType(), method.getName(), event);
                MetaMethod staticMethod = event.getType().getBestMatchingStaticMethod("getType", new Class[]{});
                if (staticMethod == null)
                    instanceInitializer.addStatement(Stmt.invokeStatic(ProxyManager.class, "registerEvent", InjectUtil.invokePublicOrPrivateMethod(injectionContext, Stmt.newObject(event.getType()), event.getType().getBestMatchingMethod("getAssociatedType", new Class[]{})), klass));
                else
                    instanceInitializer.addStatement(Stmt.invokeStatic(ProxyManager.class, "registerEvent", Stmt.invokeStatic(event.getType(), staticMethod.getName()), klass));
            }
            context.getBootstrapClass().addInnerClass(new InnerClass(proxy.body().getClassDefinition()));
            instanceInitializer.addStatement(Stmt.invokeStatic(ProxyManager.class, "registerProxy", Stmt.newObject(proxy.body().getClassDefinition(), klass)));
            for (MetaMethod method : klass.getMethodsAnnotatedWith(ContentSlot.class)) {
                if (!method.isStatic())
                    continue;
                instanceInitializer.addStatement(Stmt.invokeStatic(ProxyManager.class, "registerHandler", Stmt.invokeStatic(klass, method.getName()), klass));
            }
        }

        Class<? extends Gatekeeper> defaultGateKeeper = null;
        Collection<MetaClass> defaultGatekeeperClasses = ClassScanner.getTypesAnnotatedWith(DefaultGatekeeper.class);
        if (defaultGatekeeperClasses.size() > 0) {
            Class<? extends Gatekeeper> aClass = (Class<? extends Gatekeeper>) defaultGatekeeperClasses.iterator().next().asClass();
            defaultGateKeeper = aClass;
        }

        for (MetaClass klass : ClassScanner.getTypesAnnotatedWith(NameToken.class)) {
            boolean useGateKeeper = klass.isAnnotationPresent(UseGatekeeper.class);
            if (useGateKeeper || (defaultGateKeeper != null && !klass.isAnnotationPresent(NoGatekeeper.class))) {
                Class<? extends Gatekeeper> gateKeeper = defaultGateKeeper;
                if (useGateKeeper) {
                    Class<? extends Gatekeeper> value = klass.getAnnotation(UseGatekeeper.class).value();
                    gateKeeper = (value != null) ? value : gateKeeper;
                }
                instanceInitializer.addStatement(Stmt.invokeStatic(ProxyManager.class, "registerPlace", klass.getAnnotation(NameToken.class).value(), klass, gateKeeper));
            } else
                instanceInitializer.addStatement(Stmt.invokeStatic(ProxyManager.class, "registerPlace", klass.getAnnotation(NameToken.class).value(), klass));
        }
    }

    private MetaClass getHandler(MetaClass klass, String name, MetaClass parameter) {
        for (MetaClass handler : klass.getInterfaces()) {
            if (handler.getMethod(name, parameter) != null)
                return handler;
        }
        return null;
    }

    /*        new NotifyingAsyncCallback(){

                        @Override
                        protected void success(final Presenter presenter) {
                            Scheduler.get().scheduleDeferred( new Command() {

                                @Override
                                public void execute() {
                                    presenter.On...(event);
                                }
                            });
                        }
                    })*/
    private ClassStructureBuilder<?> createMethod(InjectionContext injectionContext, MetaClass handler, MetaClass klass, ClassDefinitionBuilderAbstractOption<? extends ClassStructureBuilder<?>> proxy, MetaClass returnType, String name, MetaParameter event) {
        Parameter parameter = Parameter.of(event.getType(), "event", true);
        MetaClass metaClass = parameterizedAs(NotifyingAsyncCallback.class, typeParametersOf(klass));
        proxy.implementsInterface(handler);
        return proxy.body().publicMethod(returnType, name, parameter).body()
                .append(
                        InjectUtil.invokePublicOrPrivateMethod(injectionContext, Stmt.loadVariable("this"),
                                proxy.body().getClassDefinition().getBestMatchingMethod("getPresenter", metaClass),
                                createCallback(metaClass, klass, proxy, name, parameter))
                ).finish();
    }

    private ObjectBuilder createCallback(MetaClass callbackClass, MetaClass presenterKlass, ClassDefinitionBuilderAbstractOption<? extends ClassStructureBuilder<?>> proxy, String name, Parameter parameter) {
        Parameter presenter = Parameter.of(presenterKlass, "presenter", true);
        return Stmt.newObject(callbackClass).extend(Stmt.loadVariable("this").invoke("getEventBus")).publicOverridesMethod("success", presenter).append(Stmt.loadVariable(presenter.getName()).invoke(name, Refs.get(parameter.getName()))).finish().finish();
    }

    private ClassDefinitionBuilderAbstractOption<? extends ClassStructureBuilder<?>> createProxy(MetaClass presenterKlass) {
        MetaClass proxyClass =
                parameterizedAs(ProxyImpl.class, typeParametersOf(presenterKlass));
        ClassDefinitionBuilderAbstractOption<? extends ClassStructureBuilder<?>> definitionStaticOption = ClassBuilder.define("org.jboss.errai.ioc.client.BootstrapperImpl." + presenterKlass.getName() + "Proxy", proxyClass).publicScope().staticClass();
        definitionStaticOption.body().publicConstructor(Parameter.of(parameterizedAs(Class.class, typeParametersOf(presenterKlass)), "presenterClass")).callSuper(Refs.get("presenterClass")).finish();
        return definitionStaticOption;
    }
}