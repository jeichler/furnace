/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.furnace.proxy.classloader;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.arquillian.services.LocalServices;
import org.jboss.forge.classloader.mock.IterableFactory;
import org.jboss.forge.classloader.mock.MockResult;
import org.jboss.forge.classloader.mock.Result;
import org.jboss.forge.classloader.mock.collisions.ClassWithClassAsParameter;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.proxy.ClassLoaderAdapterBuilder;
import org.jboss.forge.furnace.proxy.ClassLoaderAdapterBuilderDelegateLoader;
import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ClassLoaderParameterUnwrappedTest
{
   @Deployment(order = 3)
   public static AddonArchive getDeployment()
   {
      AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
               .addBeansXML()
               .addClasses(IterableFactory.class, ClassWithClassAsParameter.class)
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("dep2", "2")
               )
               .addAsLocalServices(ClassLoaderParameterUnwrappedTest.class);

      return archive;
   }

   @Deployment(name = "dep1,1", testable = false, order = 2)
   public static AddonArchive getDeploymentDep1()
   {
      AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
               .addClasses(IterableFactory.class, ClassWithClassAsParameter.class)
               .addBeansXML();

      return archive;
   }

   @Deployment(name = "dep2,2", testable = false, order = 1)
   public static AddonArchive getDeploymentDep2()
   {
      AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
               .addClasses(MockResult.class, Result.class)
               .addBeansXML();

      return archive;
   }

   @Test
   public void testUnwrapClassParameter() throws Exception
   {
      AddonRegistry registry = LocalServices.getFurnace(getClass().getClassLoader())
               .getAddonRegistry();
      ClassLoader thisLoader = ClassLoaderParameterUnwrappedTest.class.getClassLoader();
      ClassLoader dep1Loader = registry.getAddon(AddonId.from("dep1", "1")).getClassLoader();

      Class<?> foreignType = dep1Loader.loadClass(IterableFactory.class.getName());
      Object delegate = foreignType.newInstance();
      IterableFactory enhancedFactory = (IterableFactory) ClassLoaderAdapterBuilder.callingLoader(thisLoader)
               .delegateLoader(dep1Loader).enhance(delegate);
      Assert.assertTrue(Proxies.isForgeProxy(enhancedFactory));

      Object foreignInstance = dep1Loader
               .loadClass(ClassWithClassAsParameter.class.getName())
               .getConstructor(Class.class)
               .newInstance(foreignType);

      ClassLoaderAdapterBuilderDelegateLoader builder = ClassLoaderAdapterBuilder
               .callingLoader(thisLoader)
               .delegateLoader(dep1Loader);

      Object enhancedFilter = builder.enhance(foreignInstance);

      ClassWithClassAsParameter classFilter = (ClassWithClassAsParameter) enhancedFilter;

      Assert.assertTrue(Proxies.isForgeProxy(classFilter));

      Assert.assertTrue(classFilter.verify(foreignType));
      Assert.assertTrue(classFilter.verify(delegate.getClass()));
      Assert.assertTrue(classFilter.verify(enhancedFactory.getClass()));

   }

   @Test
   public void testUnwrapUnknownClassParameter() throws Exception
   {
      AddonRegistry registry = LocalServices.getFurnace(getClass().getClassLoader())
               .getAddonRegistry();
      ClassLoader thisLoader = ClassLoaderParameterUnwrappedTest.class.getClassLoader();
      ClassLoader dep1Loader = registry.getAddon(AddonId.from("dep1", "1")).getClassLoader();
      ClassLoader dep2Loader = registry.getAddon(AddonId.from("dep2", "2")).getClassLoader();

      Class<?> foreignType = dep2Loader.loadClass(MockResult.class.getName());
      Object delegate = foreignType.newInstance();
      MockResult enhancedResult = (MockResult) ClassLoaderAdapterBuilder.callingLoader(thisLoader)
               .delegateLoader(dep2Loader).enhance(delegate);
      Assert.assertTrue(Proxies.isForgeProxy(enhancedResult));

      Object foreignInstance = dep1Loader
               .loadClass(ClassWithClassAsParameter.class.getName())
               .getConstructor(Class.class)
               .newInstance(foreignType);

      ClassLoaderAdapterBuilderDelegateLoader builder = ClassLoaderAdapterBuilder
               .callingLoader(thisLoader)
               .delegateLoader(dep1Loader);

      Object enhancedFilter = builder.enhance(foreignInstance);

      ClassWithClassAsParameter classFilter = (ClassWithClassAsParameter) enhancedFilter;

      Assert.assertTrue(Proxies.isForgeProxy(classFilter));

      Class<? extends MockResult> enhancedResultType = enhancedResult.getClass();
      Assert.assertTrue(classFilter.verify(enhancedResultType));
      Assert.assertFalse(classFilter.isProxyType(enhancedResultType));
      Assert.assertTrue(classFilter.verify(delegate.getClass()));
      Assert.assertFalse(classFilter.isProxyType(delegate.getClass()));
      Assert.assertTrue(classFilter.verify(foreignType));
      Assert.assertFalse(classFilter.isProxyType(foreignType));

   }

   @Test
   public void testUnwrapParameterIfDelegateClassLoaderIsShared() throws Exception
   {
      AddonRegistry registry = LocalServices.getFurnace(getClass().getClassLoader())
               .getAddonRegistry();
      ClassLoader thisLoader = ClassLoaderParameterUnwrappedTest.class.getClassLoader();
      ClassLoader dep1Loader = registry.getAddon(AddonId.from("dep1", "1")).getClassLoader();
      ClassLoader dep2Loader = registry.getAddon(AddonId.from("dep2", "2")).getClassLoader();

      Class<?> foreignType = dep2Loader.loadClass(MockResult.class.getName());
      Object delegate = foreignType.newInstance();
      MockResult enhancedValue = (MockResult) ClassLoaderAdapterBuilder.callingLoader(thisLoader)
               .delegateLoader(dep2Loader).enhance(delegate);
      Assert.assertTrue(Proxies.isForgeProxy(enhancedValue));

      Object foreignInstance = dep1Loader
               .loadClass(ClassWithClassAsParameter.class.getName())
               .getConstructor(Class.class)
               .newInstance(foreignType);

      ClassLoaderAdapterBuilderDelegateLoader builder = ClassLoaderAdapterBuilder
               .callingLoader(thisLoader)
               .delegateLoader(dep1Loader);

      Object enhancedFilter = builder.enhance(foreignInstance);

      ClassWithClassAsParameter classFilter = (ClassWithClassAsParameter) enhancedFilter;

      Assert.assertTrue(Proxies.isForgeProxy(classFilter));

      Assert.assertEquals(foreignInstance.hashCode(), classFilter.hashCode());
      Assert.assertTrue(Proxies.isForgeProxy(enhancedValue));
      Assert.assertFalse(classFilter.isProxyObject(enhancedValue));
      Assert.assertTrue(classFilter.equals(classFilter));
      Assert.assertTrue(Proxies.isForgeProxy(classFilter));
      Assert.assertFalse(classFilter.isProxyObject(classFilter));
   }
}
