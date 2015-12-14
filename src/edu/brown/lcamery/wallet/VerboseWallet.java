package edu.brown.lcamery.wallet;

import java.util.List;

import org.bitcoinj.core.AbstractWalletEventListener;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.script.Script;

// The Wallet event listener its implementations get called on wallet changes.
public class VerboseWallet extends AbstractWalletEventListener {

    @Override
    public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
        System.out.println("-----> coins resceived: " + tx.getHashAsString());
        System.out.println("-----> received: " + tx.getValue(wallet).value*-1);
    }

    @Override
    public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
        System.out.println("-----> confidence changed: " + tx.getHashAsString());
        TransactionConfidence confidence = tx.getConfidence();
        System.out.println("-----> new block depth: " + confidence.getDepthInBlocks());
    }

    @Override
    public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
        System.out.println("-----> coins sent" + newBalance.subtract(prevBalance).subtract(Coin.valueOf(1000)) + " + 1000 fee");
    }

    @Override
    public void onReorganize(Wallet wallet) {
    }

    @Override
    public void onWalletChanged(Wallet wallet) {
    	System.out.println("-----> changed");
    }

    @Override
    public void onKeysAdded(List<ECKey> keys) {
        System.out.println("-----> new key added");
    }

    @Override
    public void onScriptsChanged(Wallet wallet, List<Script> scripts, boolean isAddingScripts) {
        System.out.println("-----> new script added");
    }
}