/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.planner.core.heuristic.selector.entity;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.drools.planner.core.domain.entity.PlanningEntityDescriptor;
import org.drools.planner.core.heuristic.selector.common.SelectionCacheLifecycleBridge;
import org.drools.planner.core.heuristic.selector.common.SelectionCacheLifecycleListener;
import org.drools.planner.core.heuristic.selector.common.SelectionCacheType;
import org.drools.planner.core.heuristic.selector.common.iterator.CachedListRandomIterator;
import org.drools.planner.core.solver.DefaultSolverScope;

/**
 * This is the common {@link EntitySelector} implementation.
 */
public class FromSolutionEntitySelector extends AbstractEntitySelector implements SelectionCacheLifecycleListener {

    protected final PlanningEntityDescriptor entityDescriptor;
    protected final SelectionCacheType cacheType;
    protected final boolean randomSelection;

    protected List<Object> cachedEntityList = null;

    public FromSolutionEntitySelector(PlanningEntityDescriptor entityDescriptor,
            SelectionCacheType cacheType, boolean randomSelection) {
        this.entityDescriptor = entityDescriptor;
        this.cacheType = cacheType;
        this.randomSelection = randomSelection;
        if (cacheType.isNotCached()) {
            throw new IllegalArgumentException("The cacheType (" + cacheType
                    + ") is not supported on the class (" + getClass().getName() + ").");
        }
        solverPhaseLifecycleSupport.addEventListener(new SelectionCacheLifecycleBridge(cacheType, this));
    }

    public PlanningEntityDescriptor getEntityDescriptor() {
        return entityDescriptor;
    }

    // ************************************************************************
    // Cache lifecycle methods
    // ************************************************************************

    public void constructCache(DefaultSolverScope solverScope) {
        cachedEntityList = entityDescriptor.extractEntities(solverScope.getWorkingSolution());
    }

    public void disposeCache(DefaultSolverScope solverScope) {
        cachedEntityList = null;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public boolean isContinuous() {
        return false;
    }

    public boolean isNeverEnding() {
        // CachedListRandomIterator is neverEnding
        return randomSelection;
    }

    public long getSize() {
        return (long) cachedEntityList.size();
    }

    public Iterator<Object> iterator() {
        if (!randomSelection) {
            return cachedEntityList.iterator();
        } else {
            return new CachedListRandomIterator<Object>(cachedEntityList, workingRandom);
        }
    }

    public ListIterator<Object> listIterator() {
        if (!randomSelection) {
            return cachedEntityList.listIterator();
        } else {
            throw new IllegalStateException("ListIterator is not supported with randomSelection ("
                    + randomSelection + ").");
        }
    }

    public ListIterator<Object> listIterator(int index) {
        if (!randomSelection) {
            return cachedEntityList.listIterator(index);
        } else {
            throw new IllegalStateException("ListIterator is not supported with randomSelection ("
                    + randomSelection + ").");
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + entityDescriptor.getPlanningEntityClass().getSimpleName() + ")";
    }

}
