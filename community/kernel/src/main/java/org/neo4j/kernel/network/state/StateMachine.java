package org.neo4j.kernel.network.state;

import org.neo4j.kernel.impl.transaction.log.TransactionIdStore;
import org.neo4j.kernel.network.message.HeartBeatMessage;
import org.neo4j.kernel.network.message.Message;

import java.util.function.Supplier;

/**
 * Created by Think on 2018/6/25.
 */
public class StateMachine {
    public StoreState getStoreState() {
        return storeState;
    }

    public void setStoreState(StoreState storeState) {
        this.storeState = storeState;
    }

    private StoreState storeState;

    public TransactionIdStore getTransactionIdStore() {
        return transactionIdStoreSupplier.get();
    }

    private Supplier<TransactionIdStore> transactionIdStoreSupplier;

    public StateMachine(StoreState storeState, Supplier<TransactionIdStore> transactionIdStoreSupplier) {
        this.storeState = storeState;
        this.transactionIdStoreSupplier = transactionIdStoreSupplier;
    }

    public synchronized void handle(HeartBeatMessage message){
        storeState = handle(message,transactionIdStoreSupplier.get());
    }

    public StoreState handle(HeartBeatMessage message,TransactionIdStore transactionIdStore){
        if(message != null){
            if (message.getStoreId()>transactionIdStore.getLastCommittedTransactionId()){
                this.storeState = StoreState.newer;
                return StoreState.newer;
            }else if(message.getStoreId()==transactionIdStore.getLastCommittedTransactionId()){
                this.storeState = StoreState.same;
                return StoreState.same;
            }else{
                this.storeState = StoreState.older;
                return StoreState.older;
            }
        }
        return this.getStoreState();
    }
}
