/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the 
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package narayana.performance.ejb;

import narayana.performance.util.DummyXAResource;
import narayana.performance.util.ResourceEnlister;
import narayana.performance.util.Result;

import java.rmi.RemoteException;

import javax.annotation.Resource;
import javax.ejb.RemoteHome;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

@RemoteHome(WorkerEJBHome.class)
@Stateless
public class WorkerEJBImpl {
    @Resource(lookup = "java:jboss/TransactionManager")
    private TransactionManager transactionManager;

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Result doWork(Result opts) throws RemoteException {
        ResourceEnlister.enlistResources(transactionManager, opts, "subordinate");

//        opts.setInfo(this.getClass().getName());
//        System.out.println("request");
        return opts;
    }
}
