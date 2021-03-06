/*
 * ============================================================================
 * GNU General Public License
 * ============================================================================
 *
 * Copyright (C) 2006-2011 Serotonin Software Technologies Inc. http://serotoninsoftware.com
 * @author Matthew Lohbihler
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.bacnet4j.type;

import java.util.ArrayList;
import java.util.List;

import com.serotonin.bacnet4j.type.enumerated.ObjectType;

public class ThreadLocalObjectTypeStack {
    private static ThreadLocal<List<ObjectType>> objType = new ThreadLocal<List<ObjectType>>();

    public static void set(ObjectType objectType) {
        List<ObjectType> stack = objType.get();

        if (stack == null) {
            stack = new ArrayList<ObjectType>();
            objType.set(stack);
        }

        stack.add(objectType);
    }

    public static ObjectType get() {
        List<ObjectType> stack = objType.get();
        if (stack == null)
            return null;
        return stack.get(stack.size() - 1);
    }

    public static void remove() {
        List<ObjectType> stack = objType.get();
        if (stack == null)
            return;

        if (stack.size() <= 1)
            objType.remove();
        else
            stack.remove(stack.size() - 1);
    }
}
