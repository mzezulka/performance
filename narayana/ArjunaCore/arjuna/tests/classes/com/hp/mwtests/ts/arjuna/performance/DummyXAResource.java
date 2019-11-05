package com.hp.mwtests.ts.arjuna.performance;

import java.io.Serializable;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class DummyXAResource implements XAResource, Serializable {

    public enum FaultType {TIMEOUT, PREPARE_FAIL, NONE}

    ;

    private String name;

    private FaultType fault = FaultType.NONE;

    public DummyXAResource(String name) {

        this(name, FaultType.NONE);
    }

    public DummyXAResource(String name, FaultType fault) {

        this.name = name;
        this.fault = fault;
    }

    @Override
    public void commit(Xid xid, boolean b) throws XAException {

        if (fault == FaultType.TIMEOUT) throw new XAException(XAException.XA_RBTIMEOUT);
    }

    @Override
    public void end(Xid xid, int i) throws XAException {

    }

    @Override
    public void forget(Xid xid) throws XAException {

    }

    @Override
    public int getTransactionTimeout() throws XAException {

        return 0;
    }

    @Override
    public boolean isSameRM(XAResource xaResource) throws XAException {

        return this.equals(xaResource);
    }

    @Override
    public int prepare(Xid xid) throws XAException {

        if (fault == FaultType.PREPARE_FAIL) {
            throw new XAException(XAException.XAER_RMFAIL);
        }
        return XAResource.XA_OK;
    }

    @Override
    public Xid[] recover(int i) throws XAException {

        return null;
    }

    @Override
    public void rollback(Xid xid) throws XAException {

    }

    @Override
    public boolean setTransactionTimeout(int timeout) throws XAException {

        return false;
    }

    @Override
    public void start(Xid xid, int i) throws XAException {

    }

    @Override
    public String toString() {

        return "XAResourceWrapperImpl@[xaResource=" + super.toString() + " pad=false overrideRmValue=null productName=" + name + " productVersion=1.0 jndiName=java:jboss/" + name + "]";
    }
}
