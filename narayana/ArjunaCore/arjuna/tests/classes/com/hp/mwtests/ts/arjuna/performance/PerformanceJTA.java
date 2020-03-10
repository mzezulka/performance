package com.hp.mwtests.ts.arjuna.performance;


import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Performance1.java 2342 2006-03-30 13:06:17Z  $
 */
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.VolatileStore;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

/*
 * We need to rapidly decrease number of perftest iterations. That way,
 * we approach the way Narayana will behave in a "regular" environment - 
 * that is, Narayana does not deal with hundreds of thousands of transactions per second.
 * 
 * To achieve the intentional perf tests degradation, we will use the JMH blackhole,
 * especially Blackhole.consumeCPU which tries to be as machine-independent as possible.
 */
@State(Scope.Thread)
public class PerformanceJTA {
    
    private TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
    private static final int BLACKHOLE_TOKENS = 1_000_000; 
    
    static {
        TracingHelper.registerTracer();
        try {
            BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class)
                    .setObjectStoreType(VolatileStore.class.getName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public boolean twoPhaseCommit(Blackhole bh) throws IllegalStateException, RollbackException, SystemException,
    NotSupportedException, SecurityException, HeuristicMixedException, HeuristicRollbackException {
        bh.consume(twoPhaseCommitImple());
        return true;
    }

    public boolean twoPhaseCommitImple() throws IllegalStateException, RollbackException, SystemException,
    NotSupportedException, SecurityException, HeuristicMixedException, HeuristicRollbackException {
        tm.begin();
        tm.getTransaction().enlistResource(new DummyXAResource("demo1"));
        Blackhole.consumeCPU(BLACKHOLE_TOKENS);
        tm.getTransaction().enlistResource(new DummyXAResource("demo2"));
        Blackhole.consumeCPU(BLACKHOLE_TOKENS);
        tm.commit();
        return true;
    }

    @Benchmark
    public boolean rollback(Blackhole bh) throws NotSupportedException, SystemException, IllegalStateException, RollbackException {
        bh.consume(rollbackImple());
        return true;
    }
    
    public boolean rollbackImple() throws NotSupportedException, SystemException, IllegalStateException, RollbackException {
        tm.begin();
        tm.getTransaction().enlistResource(new DummyXAResource("demo1"));
        Blackhole.consumeCPU(BLACKHOLE_TOKENS);
        tm.rollback();
        return true;
    }

    @Benchmark 
    public boolean onePhaseCommit(Blackhole bh) throws NotSupportedException, SystemException, IllegalStateException, RollbackException, SecurityException, HeuristicMixedException, HeuristicRollbackException {
        bh.consume(onePhaseCommitImple());
        return true;
    }

    public boolean onePhaseCommitImple() throws NotSupportedException, SystemException, IllegalStateException, RollbackException, SecurityException, HeuristicMixedException, HeuristicRollbackException {
        tm.begin();
        tm.getTransaction().enlistResource(new DummyXAResource("demo1"));
        Blackhole.consumeCPU(BLACKHOLE_TOKENS);
        tm.commit();
        return true;
    }

    @Benchmark
    public boolean resourceFailsToPrepare(Blackhole bh) throws NotSupportedException, SystemException, IllegalStateException, RollbackException, SecurityException, HeuristicMixedException, HeuristicRollbackException {
        bh.consume(resourceFailsToPrepareImple());
        return true;
    }    


    public boolean resourceFailsToPrepareImple() throws NotSupportedException, SystemException, IllegalStateException, RollbackException, SecurityException, HeuristicMixedException, HeuristicRollbackException {
        tm.begin();
        tm.getTransaction().enlistResource(new DummyXAResource("demo1"));
        XAResource prepareFail = new DummyXAResource("fail", DummyXAResource.FaultType.PREPARE_FAIL);
        tm.getTransaction().enlistResource(prepareFail);
        try {
            tm.commit();    
        } catch(RollbackException re) {
            // ok, this is what we expect
            return true;
        }
        throw new RuntimeException(String.format("The second resource %s should have failed to prepare but "
                + "TransactionManager.commit did not throw RollbackException.", prepareFail));
    }
}
