package com.hp.mwtests.ts.arjuna.performance;

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
    private static final int BLACKHOLE_TOKENS = 1000; 
    
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
    public boolean twoPhaseCommit() {
        try {
            tm.begin();
            tm.getTransaction().enlistResource(new DummyXAResource("demo1"));
            Blackhole.consumeCPU(BLACKHOLE_TOKENS);
            tm.getTransaction().enlistResource(new DummyXAResource("demo2"));
            Blackhole.consumeCPU(BLACKHOLE_TOKENS);
            tm.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Benchmark
    public boolean rollback() {
        try {
            tm.begin();
            tm.getTransaction().enlistResource(new DummyXAResource("demo1"));
            Blackhole.consumeCPU(BLACKHOLE_TOKENS);
            tm.rollback();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Benchmark
    public boolean simple(Blackhole bh) {
        try {
            tm.begin();
            tm.getTransaction().enlistResource(new DummyXAResource("demo1"));
            Blackhole.consumeCPU(BLACKHOLE_TOKENS);
            tm.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }
    
    @Benchmark
    public boolean timeout(Blackhole bh) {
        try {
            tm.setTransactionTimeout(1);
            tm.begin();
            tm.getTransaction().enlistResource(new DummyXAResource("demo1"));
            Blackhole.consumeCPU(1_000_000);
            throw new RuntimeException("Exceeded transaction timeout but still running.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
