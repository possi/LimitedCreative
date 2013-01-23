/**
 * This package contains Hookers and Hooks to integrate with other Plugins.
 * 
 * Hookers are base classes to describe functions to test again. They are simple event manager, which hooks can register
 * thru. So they should be only instantiated once in a global class.
 * de.jaschastarke.minecraft.limitedcreative.Hooks in this case.
 * 
 * Hooks are the implemented event listeners which will be registered to the static hooker-instances. The first hook
 * sorted by priority which gives an answer will be returned. The exact behavior of multiple hooks are described by
 * the hooker.
 * 
 * As the plugins itself doesn't register the hooks it is done by the static
 * de.jaschastarke.minecraft.limitedcreative.Hooks-initializer.
 */
package de.jaschastarke.minecraft.limitedcreative.hooks;