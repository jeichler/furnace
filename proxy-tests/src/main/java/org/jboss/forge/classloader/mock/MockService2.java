/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.classloader.mock;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * 
 */
public class MockService2 implements MockParentInterface2
{
   public MockService2(Object nonDefaultConstructor)
   {
      /* force interface proxying */
   }

   @Override
   public String getResult()
   {
      return "Lincoln";
   }
}
