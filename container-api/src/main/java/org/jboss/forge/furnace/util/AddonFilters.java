/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.furnace.util;

import org.jboss.forge.furnace.addons.AddonFilter;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class AddonFilters
{
   public static AddonFilter allLoaded()
   {
      return (addon) -> addon.getStatus().isLoaded();
   }

   public static AddonFilter allStarting()
   {
      return (addon) -> !addon.getFuture().isDone();
   }

   public static AddonFilter allStarted()
   {
      return (addon) -> addon.getStatus().isStarted();
   }

   public static AddonFilter allNotStarted()
   {
      return (addon) -> !addon.getStatus().isStarted();
   }

   public static AddonFilter all()
   {
      return (addon) -> true;
   }
}
